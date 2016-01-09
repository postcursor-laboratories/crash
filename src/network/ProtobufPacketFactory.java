package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import protos.HandshakeProto;
import protos.KeyProto;

public final class ProtobufPacketFactory implements PacketFactory {

	public final class PBPacket<M extends MessageLite> implements Packet {

		private final int subId;
		private final M pbData;

		protected PBPacket(int subId, M pbData) {
			this.subId = subId;
			this.pbData = pbData;
		}

		public M getMessage() {
			return pbData;
		}

		@Override
		public void writeBytes(DataOutputStream stream) throws IOException {
			stream.writeInt(subId);
			pbData.writeTo(stream);
		}

		@Override
		public short getId() {
			return ProtobufPacketFactory.this.getPacketId();
		}
	}

	private static ProtobufPacketFactory instance;

	private static void setInstance(ProtobufPacketFactory instance) {
		if (instance != null) {
			throw new UnsupportedOperationException(
					"already have a protobuf packet factory");
		}
		ProtobufPacketFactory.instance = instance;
	}

	public static ProtobufPacketFactory getInstance() {
		return instance;
	}

	// TODO: optimize as array?
	private final Map<Integer, Parser<? extends MessageLite>> subIdToParser =
			new HashMap<>();
	private final Map<Class<? extends MessageLite>, Integer> pbTypeToSubId =
			new HashMap<>();
	private final short id;
	private transient int subIdTracker;

	private <T extends MessageLite> void addParser(Class<T> type) {
		pbTypeToSubId.put(type, subIdTracker);
		Parser<T> parser = extractParser(type);
		subIdToParser.put(subIdTracker, parser);
		subIdTracker++;
	}

	@SuppressWarnings("unchecked")
	private <T extends MessageLite> Parser<T> extractParser(Class<T> type) {
		// Known field of all MessageLites.
		try {
			Field parserField = type.getDeclaredField("PARSER");
			return (Parser<T>) parserField.get(null);
		} catch (Exception e) {
			throw new IllegalStateException("PARSER field must be accessible");
		}
	}

	{
		// Protobuf packet registrations
		addParser(HandshakeProto.Handshake.class);
		addParser(KeyProto.Keys.class);
	}

	protected ProtobufPacketFactory(short id) {
		this.id = id;
		setInstance(instance);
	}

	@Override
	public short getPacketId() {
		return id;
	}

	public <M extends MessageLite> PBPacket<M> createPacket(M data) {
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null");
		}
		int subId = pbTypeToSubId.get(data.getClass());
		return new PBPacket<>(subId, data);
	}

	@Override
	public PBPacket<?> createPacket(DataInputStream in) throws IOException {
		PBPacket<?> p = null;
		int subId = in.readInt();
		Parser<? extends MessageLite> parser = subIdToParser.get(subId);
		p = new PBPacket<>(subId, parser.parseFrom(in));
		return p;
	}

}
