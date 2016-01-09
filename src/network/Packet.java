package network;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet {

	short getId();

	/**
	 * The written bytes should not include the packet ID. It is up to the
	 * network manager to send that.
	 */
	void writeBytes(DataOutputStream out) throws IOException;

}
