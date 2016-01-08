package network;


public interface Packet {

	int getId();

	/**
	 * The created bytes do not include the packet ID. It is up to the network
	 * manager to send that.
	 */
	byte[] createBytes();

}
