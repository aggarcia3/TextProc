// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step;

/**
 * Represents a unrecoverable exception which occurs while executing a
 * processing operation.
 *
 * @author Alejandro González García
 */
public final class ProcessingException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new processing exception with {@code null} as its detail
	 * message. The cause is not initialized.
	 */
	public ProcessingException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message. The cause is
	 * not initialized.
	 *
	 * @param message The detail message.
	 */
	public ProcessingException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message The detail message.
	 * @param cause   The cause. A {@code null} value is permitted, and indicates
	 *                that the cause is nonexistent or unknown.
	 */
	public ProcessingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * {@code (cause==null ? null : cause.toString())} (which typically contains the
	 * class and detail message of {@code cause}).
	 *
	 * @param cause The cause. A {@code null} value is permitted, and indicates that
	 *              the cause is nonexistent or unknown.
	 */
	public ProcessingException(final Throwable cause) {
		super(cause);
	}
}
