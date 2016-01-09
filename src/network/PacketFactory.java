package network;

import java.io.DataInputStream;

public interface PacketFactory {

	int getPacketId();

	Packet createPacket(DataInputStream in);

}
