// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.lalyos.jfiglet.FigletFont;

import es.uvigo.esei.sing.textproc.logging.TextProcLogging;
import es.uvigo.esei.sing.textproc.persistence.TextProcPersistence;
import es.uvigo.esei.sing.textproc.process.ProcessingProcess;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.ProcessingStepServices;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Class whose responsibility is the high level application life cycle
 * management. It also provides references to objects and data which should be
 * shared between parts of the application.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is thread-safe.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextProc {
	/**
	 * The name of the application.
	 */
	public static final String APP_NAME = TextProc.class.getSimpleName();

	private static final TextProc APP_INSTANCE = new TextProc();

	@Getter(AccessLevel.PRIVATE)
	private final Logger appLogger;

	{
		TextProcLogging.initialize(APP_NAME);
		this.appLogger = TextProcLogging.getLogger();
	}

	/**
	 * Entry point of the application. It should be called only by the JVM.
	 *
	 * @param args The command line parameters passed to the application.
	 */
	public static void main(final String[] args) {
		System.exit(get().run(args));
	}

	/**
	 * Returns the only instance of this class in the JVM.
	 *
	 * @return The described instance.
	 */
	public static TextProc get() {
		return APP_INSTANCE;
	}

	/**
	 * Prints progress messages to the standard output stream.
	 *
	 * @param message The message to print.
	 * @throws IllegalArgumentException If {@code message} is {@code null}.
	 */
	public void printProgressMessage(@NonNull final String message) {
		System.out.print("> ");
		System.out.println(message);
	}

	/**
	 * Entry point of the application, executed within the context of the only
	 * instance of this class in the JVM.
	 *
	 * @param args The command line parameters passed to the application.
	 * @return The exit status of the application process.
	 */
	private int run(final String[] args) {
		final TextProcPersistence persistence = TextProcPersistence.get();
		InputStream processInputStream;
		int exitStatus = 0;
		final Set<Class<?>> processedDocumentTypes = new HashSet<>();

		if (args.length > 1) {
			throw new IllegalArgumentException("Unexpected number of arguments");
		}

		printStartBanners();

		try {
			printProgressMessage("Initializing Java Persistence API entity management...");

			// Get all the processed document types available
			for (final ProcessingStepService processingStepService : ProcessingStepServices.getServiceLoader()) {
				processedDocumentTypes.addAll(processingStepService.getProcessedDocumentTypes());
			}
			persistence.start(processedDocumentTypes);

			printProgressMessage("JPA entity management factory initialized.");

			if (args.length == 0 || (args.length == 1 && args[0] == "-")) {
				// Read the process from standard input
				processInputStream = System.in;
			} else {
				// One argument. Read the process from a file
				processInputStream = new FileInputStream(args[0]);
			}

			printProgressMessage(
				"Executing process definined in " + (processInputStream == System.in ? "standard input" : args[0]) + "."
			);

			// Do the actual work
			final long startTimestamp = System.currentTimeMillis();
			new ProcessingProcess().executeProcessDeclaration(processInputStream);
			final long endTimestamp = System.currentTimeMillis();

			final Duration elapsedTime = Duration.ofMillis(endTimestamp - startTimestamp);
			final String elapsedTimeString = String.format(
				"%d day/s, %02d:%02d:%02d", elapsedTime.toDaysPart(),
				elapsedTime.toHoursPart(), elapsedTime.toMinutesPart(), elapsedTime.toSecondsPart()
			);

			System.out.println();
			System.out.println();

			printProgressMessage(
				"Process done (" + elapsedTimeString + "). " +
				"Consider optimizing the processed entity tables for optimal performance. Have a nice day!"
			);
		} catch (final Throwable exc) {
			getAppLogger().log(
				Level.SEVERE, "An exception has occurred. Aborting program execution.", exc
			);

			exitStatus = Math.abs(exc.hashCode()) % 255 + 1; // Make sure it's greater than 0
		} finally {
			printProgressMessage("Cleaning up before stopping...");

			persistence.stop();

			printProgressMessage("Cleanup done.");
		}

		return exitStatus;
	}

	/**
	 * Prints startup messages to the standard output stream.
	 */
	private void printStartBanners() {
		// Application name
		try {
			System.out.println(FigletFont.convertOneLine(APP_NAME));
		} catch (final IOException exc) {
			// Fallback to not so pretty banner
			System.out.println(APP_NAME);
			System.out.println();
		}

		// License info
		System.out.println(
			"Copyright (C) 2020\n" + 
			"This program comes with ABSOLUTELY NO WARRANTY.\n" + 
			"This is free software, and you are welcome to redistribute it\n" + 
			"under certain conditions. See LICENSE for details."
		);
		System.out.println();
	}
}
