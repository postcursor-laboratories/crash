package network;

import java.io.DataInputStream;
import java.io.IOException;

public interface PacketFactory {

	short getPacketId();

	Packet createPacket(DataInputStream in) throws IOException;

}
