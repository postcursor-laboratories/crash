package network;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.BoundedInputStream;

public class NetworkManager implements Closeable {

	// ~14k
	private static final int MAX_PACKET_SIZE = Short.MAX_VALUE / 4;
	private static final int MAX_PACKETS_TO_PROCESS = Short.MAX_VALUE;
	private static final int NO_PACKET = Short.MIN_VALUE;
	private static final int NO_CONTENT_LENGTH = -1;
	private static final DataInputStream EMPTY_INPUT_STREAM =
			EmptyDataInputStream.INSTANCE;

	private static DataInputStream createLimitedDataStream(InputStream stream,
			int size) {
		return size == 0 ? EMPTY_INPUT_STREAM
				: new DataInputStream(new BoundedInputStream(stream, size));
	}

	private final ConcurrentLinkedQueue<Packet> packetQueue =
			new ConcurrentLinkedQueue<>();
	private final DataInputStream in;
	private final DataOutputStream out;
	private short packetId = NO_PACKET;
	private int contentLength = NO_CONTENT_LENGTH;

	public NetworkManager(DataInputStream in, DataOutputStream out) {
		this.in = in;
		this.out = out;
	}

	public void readDataForever() throws IOException {
		while (true) {
			// TODO let exceptions propagate?
			if (packetId == NO_PACKET) {
				packetId = in.readByte();
			} else if (packetId < 0) {
				// Short ID, already have byte needed
				packetId = (short) (((packetId & 0xFF) << 8)
						& (in.readByte() & 0xFF));
			} else if (contentLength == NO_CONTENT_LENGTH) {
				// Read int content length, not unsigned but must be positive
				int len = in.readInt();
				if (len < 0) {
					throw new InvalidDataException(
							"content length too small, < 0");
				}
				if (len > MAX_PACKET_SIZE) {
					throw new InvalidDataException(
							"content length too large, > " + MAX_PACKET_SIZE);
				}
				// Special 0-length case
				if (len == 0) {
					storePacket(EMPTY_INPUT_STREAM);
					continue;
				}
				contentLength = len;
			} else {
				// Have packet ID + content length, can read packet
				storePacket(createLimitedDataStream(in, contentLength));
			}
		}
	}

	private void storePacket(DataInputStream in) {
		Packet p = PacketRegistry.getPacketFactory(packetId).createPacket(in);
		packetQueue.add(p);
		// Reset
		packetId = NO_PACKET;
		contentLength = NO_CONTENT_LENGTH;
	}

	public Stream<Packet> getPacketsToProcess() {
		List<Packet> packets = new ArrayList<>();
		while (packets.size() <= MAX_PACKETS_TO_PROCESS
				&& !packetQueue.isEmpty()) {
			packets.add(packetQueue.poll());
		}
		int qSize = packetQueue.size();
		if (qSize > 0) {
			System.err.println("Saving " + qSize
					+ " packets for next round of packet processing");
		}
		return packets.stream();
	}

	@Override
	public void close() throws IOException {
		Stream.Builder<Exception> suppressions = Stream.builder();
		try {
			in.close();
		} catch (Exception e) {
			suppressions.add(e);
		}
		try {
			out.close();
		} catch (Exception e) {
			suppressions.add(e);
		}
		List<Exception> suppressed =
				suppressions.build().collect(Collectors.toList());
		if (!suppressed.isEmpty()) {
			IOException ex = new IOException("error closing network manager");
			suppressed.forEach(ex::addSuppressed);
			throw ex;
		}
	}

}
