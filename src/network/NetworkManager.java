package network;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

	private final BlockingQueue<Packet> readQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<Packet> writeQueue =
			new LinkedBlockingQueue<>();
	private final DataInputStream in;
	private final DataOutputStream out;
	private final Thread readThread;
	private final Thread writeThread;
	private volatile boolean closed = false;
	private volatile IOException readEx;
	private volatile IOException writeEx;
	private short packetId = NO_PACKET;
	private int contentLength = NO_CONTENT_LENGTH;

	public NetworkManager(DataInputStream in, DataOutputStream out) {
		this.in = in;
		this.out = out;
		readThread = new Thread(() -> {
			try {
				readDataForever();
			} catch (IOException e) {
				// oh well
				readEx = e;
			} finally {
				try {
					close();
				} catch (IOException e) {
					// whatever.
				}
			}
		}, "socket read thread");
		readThread.setDaemon(true);
		writeThread = new Thread(() -> {
			try {
				writeDataForever();
			} catch (IOException e) {
				// oh well
				writeEx = e;
			} finally {
				try {
					close();
				} catch (IOException e) {
					// whatever.
				}
			}
		}, "socket write thread");
		writeThread.setDaemon(true);
	}

	public void startThreads() {
		readThread.start();
		writeThread.start();
	}

	private void readDataForever() throws IOException {
		while (!closed) {
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

	private void storePacket(DataInputStream in) throws IOException {
		Packet p;
		p = PacketRegistry.getPacketFactory(packetId).createPacket(in);
		readQueue.add(p);
		// Reset
		packetId = NO_PACKET;
		contentLength = NO_CONTENT_LENGTH;
	}

	public Stream<Packet> getPacketsToProcess() throws IOException {
		if (readEx != null) {
			throw readEx;
		}
		List<Packet> packets = new ArrayList<>();
		while (packets.size() <= MAX_PACKETS_TO_PROCESS
				&& !readQueue.isEmpty()) {
			packets.add(readQueue.poll());
		}
		int qSize = readQueue.size();
		if (qSize > 0) {
			System.err.println("Saving " + qSize
					+ " packets for next round of packet processing");
		}
		return packets.stream();
	}

	private void writeDataForever() throws IOException {
		while (!closed) {
			Packet toSend;
			try {
				toSend = writeQueue.take();
			} catch (InterruptedException e) {
				// We should stop.
				return;
			}
			short id = toSend.getId();
			if (id <= Byte.MAX_VALUE) {
				// Single byte
				out.write(id);
			} else {
				// Short - two bytes
				byte val = (byte) ((id >> 8) & 0xFF);
				// writes high byte, negated
				out.write(-val);
				// writes lower byte
				out.write(id);
			}
			byte[] packet = serializePacket(toSend);
			out.writeInt(packet.length);
			out.write(packet);
		}
	}

	// Helper fields for serializePacket
	private transient final ByteArrayOutputStream collector =
			new ByteArrayOutputStream();
	private transient final DataOutputStream collectorView =
			new DataOutputStream(collector);

	private byte[] serializePacket(Packet toSend) {
		collector.reset();
		try {
			toSend.writeBytes(collectorView);
		} catch (IOException e) {
			// Probably impossible
			throw new IllegalStateException("I/O error on in-memory streams",
					e);
		}
		return collector.toByteArray();
	}

	public void addPacketToSend(Packet p) throws IOException {
		if (writeEx != null) {
			throw writeEx;
		}
		if (p.getId() < 0) {
			throw new IllegalArgumentException("packet ID must be >= 0");
		}
		writeQueue.add(p);
	}

	@Override
	public void close() throws IOException {
		if (closed) {
			// probably read/write thread requesting close
			return;
		}
		closed = true;
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
