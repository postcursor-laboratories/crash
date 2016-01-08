package network;



public interface PacketFactory {
	
	int getPacketId();
	
	Packet createPacket(byte[] data);

}
