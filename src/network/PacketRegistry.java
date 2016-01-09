package network;

import java.util.HashMap;
import java.util.Map;

public final class PacketRegistry {

	private static final Map<Short, PacketFactory> idToPacket =
			new HashMap<>();

	public static PacketFactory getPacketFactory(short id) {
		return idToPacket.get(id);
	}

	public static void registerPacketFactory(PacketFactory factory) {
		idToPacket.put(factory.getPacketId(), factory);
	}

	static {
		// All packets are registered here.
		short packetIdTracker = 0;
		registerPacketFactory(new ProtobufPacketFactory(packetIdTracker++));
	}

	private PacketRegistry() {
	}

}
