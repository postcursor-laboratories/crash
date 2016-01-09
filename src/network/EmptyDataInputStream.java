package network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EmptyDataInputStream extends DataInputStream {

	public static final EmptyDataInputStream INSTANCE =
			new EmptyDataInputStream();

	private EmptyDataInputStream() {
		super(new InputStream() {

			@Override
			public int read() throws IOException {
				return -1;
			}

			@Override
			public int read(byte[] b) throws IOException {
				return -1;
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return -1;
			}

			@Override
			public long skip(long n) throws IOException {
				return 0;
			}

		});
	}

}
