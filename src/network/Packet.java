package network;

import java.io.DataOutputStream;

public interface Packet {

	int getId();

	/**
	 * The written bytes should not include the packet ID. It is up to the network
	 * manager to send that.
	 */
	void writeBytes(DataOutputStream out);

}
