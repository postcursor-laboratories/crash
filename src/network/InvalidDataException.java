package network;
import java.io.IOException;

/**
 * The data from the stream is invalid.
 */
public class InvalidDataException extends IOException {

	private static final long serialVersionUID = 8165854966160669961L;

	/**
	 * Constructs an {@code InvalidDataException} with {@code null} as its error
	 * detail message.
	 */
	public InvalidDataException() {
	}

	/**
	 * Constructs an {@code InvalidDataException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 */
	public InvalidDataException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code InvalidDataException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 */
	public InvalidDataException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an {@code InvalidDataException} with the specified detail
	 * message and cause.
	 *
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated into this exception's detail message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 *
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 *
	 * @since 1.6
	 */
	public InvalidDataException(String message, Throwable cause) {
		super(message, cause);
	}

}
