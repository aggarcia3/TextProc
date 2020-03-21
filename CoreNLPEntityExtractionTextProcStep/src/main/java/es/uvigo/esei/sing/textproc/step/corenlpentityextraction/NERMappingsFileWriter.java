// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpentityextraction;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import es.uvigo.esei.sing.textproc.step.util.VariableHolder;
import lombok.NonNull;

/**
 * Writes NER mappings to a file, in the format specified by
 * {@link TokensRegexNERAnnotator}.
 * <p>
 * This class is designed to be used by several threads in parallel, and it is
 * responsible for serializing writes to the resulting file. Therefore, write
 * calls may block.
 * </p>
 *
 * @author Alejandro González García
 */
final class NERMappingsFileWriter implements Closeable {
	private static final CSVFormat NER_MAPPINGS_FILE_FORMAT;

	private final BlockingQueue<Object[]> recordQueue = new SynchronousQueue<>();
	private final BlockingQueue<VariableHolder<IOException>> resultQueue = new SynchronousQueue<>();
	private final Thread recordWriterThread;
	private volatile boolean closed = false;

	static {
		// The format for TokensRegexNERAnnotator files, which essentially is TSV
		NER_MAPPINGS_FILE_FORMAT = CSVFormat.TDF
			.withRecordSeparator('\n')
			.withTrim()
			.withHeader(MappingsFileFields.class)
			.withSkipHeaderRecord()
			.withIgnoreEmptyLines();
	}

	/**
	 * Creates a new NER mappings file writer.
	 *
	 * @param mappingsFile The file path that will contain the mappings.
	 * @throws IOException              If an I/O error occurs during
	 *                                  initialization.
	 * @throws IllegalArgumentException If {@code mappingsFile} is {@code null}.
	 */
	NERMappingsFileWriter(@NonNull final Path mappingsFile) throws IOException {
		final CSVPrinter csvPrinter = NER_MAPPINGS_FILE_FORMAT.print(mappingsFile, StandardCharsets.UTF_8);
		final CountDownLatch recordWriterThreadReady = new CountDownLatch(1);

		this.recordWriterThread = new Thread(() -> {
			try {
				while (!Thread.interrupted()) {
					recordWriterThreadReady.countDown();

					final Object[] record = recordQueue.take();
					IOException writeException = null;

					try {
						csvPrinter.printRecord(record);
						try {
							csvPrinter.flush();
						} catch (final IOException ignored) {}
					} catch (final Exception exc) {
						// Capture every exception type, just in case
						writeException = exc instanceof IOException ?
							(IOException) exc : new IOException(exc);
					} finally {
						boolean resultHandedOff = false;
						while (!resultHandedOff) {
							try {
								resultQueue.put(new VariableHolder<>(writeException));
								resultHandedOff = true;
							} catch (final InterruptedException ignored) {
								// We must hand off the result. Just record
								// the interrupt status to exit the loop later.
								// We can't hang the producers
								Thread.currentThread().interrupt();
							}
						}
					}
				}
			} catch (InterruptedException bailOut) {
			} finally {
				try {
					csvPrinter.close();
				} catch (final IOException ignored) {
					// Not much can be done to handle this
				}
			}
		}, "NER mapping file record writer thread");

		recordWriterThread.setDaemon(true);
		recordWriterThread.setPriority(Thread.MIN_PRIORITY);
		recordWriterThread.start();

		// Wait until the record writer thread is ready to write records.
		// Consider not being able to guarantee that as an I/O error
		try {
			recordWriterThreadReady.await();
		} catch (final InterruptedException exc) {
			throw new IOException(exc);
		}
	}

	/**
	 * Writes the specified named entity mapping to the named entity mappings file
	 * associated to this writer, with a priority of 0.
	 *
	 * @param token             The token to add a mapping to. It will be
	 *                          interpreted as a literal string, and any Java
	 *                          regular expression metacharacters will be escaped.
	 * @param type              The label of the named entity type the named entity
	 *                          identified by the token belongs to.
	 * @param overwritableTypes A list of named entity types that this mapping is
	 *                          able to override. This list can be empty.
	 * @throws IllegalArgumentException If any parameter is {@code null}, or
	 *                                  {@code overwritableTypes} contains a
	 *                                  {@code null} element.
	 * @throws IOException              If an I/O error occurs during the operation,
	 *                                  or the writer is closed.
	 */
	public void writeMapping(
		@NonNull final String token, @NonNull final String type, @NonNull final List<String> overwritableTypes
	) throws IOException {
		writeMapping(token, type, overwritableTypes, 0);
	}

	/**
	 * Writes the specified named entity mapping to the named entity mappings file
	 * associated to this writer.
	 *
	 * @param token             The token to add a mapping to. It will be
	 *                          interpreted as a literal string, and any Java
	 *                          regular expression metacharacters will be escaped.
	 * @param type              The label of the named entity type the named entity
	 *                          identified by the token belongs to.
	 * @param overwritableTypes A list of named entity types that this mapping is
	 *                          able to override. This list can be empty.
	 * @param priority          The priority of this mapping. Higher values take
	 *                          precendence.
	 * @throws IllegalArgumentException If any parameter is {@code null}, or
	 *                                  {@code overwritableTypes} contains a
	 *                                  {@code null} element.
	 * @throws IOException              If an I/O error occurs during the operation,
	 *                                  or the writer is closed.
	 */
	public void writeMapping(
		@NonNull final String token, @NonNull final String type, @NonNull final List<String> overwritableTypes,
		final int priority
	) throws IOException {
		final StringBuilder overwritableTypesStringBuilder = new StringBuilder();
		IOException writeException = null;

		if (closed) {
			throw new IOException("Can't write NER mappings to a closed writer");
		}

		for (final String overwritableType : overwritableTypes) {
			if (overwritableType == null) {
				throw new IllegalArgumentException("A named entity type can't be null");
			}

			overwritableTypesStringBuilder.append(overwritableType);
			overwritableTypesStringBuilder.append(',');
		}

		// Delete trailing comma
		if (overwritableTypesStringBuilder.length() > 0) {
			overwritableTypesStringBuilder.setLength(overwritableTypesStringBuilder.length() - 1);
		}

		if (!token.isBlank()) {
			try {
				recordQueue.put(
					new Object[] {
						Pattern.quote(token), type.toUpperCase(Locale.ROOT),
						overwritableTypesStringBuilder.toString(),
						priority
					}
				);

				// No two threads can be waiting for a result at the same time,
				// because the put call above acts like a mutex: we can't reach
				// this take call without putting before, and we can't put before
				// the writer thread puts the result for us
				boolean tookResult = false;
				while (!tookResult) {
					try {
						writeException = resultQueue.take().getVariable();
						tookResult = true;
					} catch (final InterruptedException ignored) {
						// Take the result no matter what. We can't retreat
						// once the record is submitted, or the writer thread
						// will hang
					}
				}

				if (writeException != null) {
					throw writeException;
				}
			} catch (final InterruptedException bailOut) {}
		}
	}

	/**
	 * @implNote This implementation never throws an exception. Any error which
	 *           occurs closing resources is silently ignored.
	 */
	@Override
	public void close() throws IOException {
		closed = true;
		recordWriterThread.interrupt();
	}

	/**
	 * Represents the fields that each record in a NER mappings file can contain.
	 *
	 * @author Alejandro González García
	 */
	private static enum MappingsFileFields {
		/**
		 * A sequence of Java regular expressions that will be applied to a tokenized
		 * input document. The matches will be considered instances of a named entity.
		 */
		TOKEN_REGEX,
		/**
		 * The named entity type (label) for the matched token.
		 */
		TYPE,
		/**
		 * The named entity types this match can override.
		 */
		OVERWRITABLE_TYPES,
		/**
		 * The priority of the match 
		 */
		PRIORITY;

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
