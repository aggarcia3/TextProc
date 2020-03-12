// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.logging;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Entry point to the TextProc logging functionalities.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is thread-safe.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextProcLogging {
	private static final AtomicReference<Logger> LOGGER = new AtomicReference<>();

	/**
	 * Initializes logging for the given application name. Subsequent calls to
	 * {@link #getLogger()} will return a logger with the application name specified
	 * here. This method only has any effects the first time it is invoked.
	 *
	 * @param appName The application name for the logger.
	 * @throws IllegalArgumentException If {@code appName} is {@code null}.
	 */
	public static void initialize(@NonNull final String appName) {
		LOGGER.compareAndSet(null, Logger.getLogger(appName));
	}

	/**
	 * Returns the logger previously initialized with {@link #initialize(String)}.
	 * This method always returns the same object.
	 *
	 * @return The described logger. It never is {@code null}.
	 * @throws IllegalStateException If the logger wasn't initialized yet.
	 */
	public static Logger getLogger() {
		final Logger logger = LOGGER.get();

		if (logger == null) {
			throw new IllegalStateException("Logging hasn't been initialized");
		}

		return logger;
	}
}
