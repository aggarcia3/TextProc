package es.uvigo.esei.sing.textproc.step.corenlpentityextraction;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Contains helper methods for dealing with the contents of preexisting,
 * internal use named entity dictionaries.
 *
 * @author Alejandro González García
 */
final class NamedEntityDictionaryHelper {
	private static final CSVFormat DICTIONARY_FILE_FORMAT;
	private static final Charset DICTIONARY_FILE_CHARSET = StandardCharsets.UTF_8;
	private static final String MALFORMED_INPUT_RECORD_ERROR_FORMAT_STR = "Malformed input record at line %d, file %s: %s";

	static {
		// The format for dictionary files, which essentially is TSV.
		// This specification deviates a bit from the IANA standard,
		// but it is backwards compatible with it:
		// https://www.iana.org/assignments/media-types/text/tab-separated-values
		DICTIONARY_FILE_FORMAT = CSVFormat.TDF
			.withRecordSeparator('\n')
			.withCommentMarker('#')
			.withFirstRecordAsHeader()
			.withAllowMissingColumnNames(false)
			.withEscape('\\')
			.withIgnoreHeaderCase()
			.withIgnoreEmptyLines();
	}

	/**
	 * Disallows instantiation of this class.
	 */
	private NamedEntityDictionaryHelper() {}

	/**
	 * Walks through the specified root file system path and its children, searching
	 * for TSV files, and parses them as named entity dictionaries, adding the terms
	 * they contain to a map. The entity type label for the terms in a file is the
	 * file name in upper case.
	 *
	 * @param rootPath The root path whose children files will be scanned for
	 *                 entities. Subdirectories will be ignored. Normally this path
	 *                 is a directory, but it can also be a regular file, in which
	 *                 case it will be the only file that is visited.
	 * @return An unmodifiable map whose keys are the entity type labels which have
	 *         at least a term, and the associated value is a set of terms for that
	 *         label.
	 * @throws IOException              If some I/O error occurs during the
	 *                                  operation.
	 * @throws IllegalArgumentException If {@code rootPath} is {@code null}.
	 */
	public static Map<String, Set<NamedEntityTerm>> namedEntitiesFromPathChildren(@NonNull final Path rootPath) throws IOException {
		final Map<String, Set<NamedEntityTerm>> labelTerms = new HashMap<>();

		Files.walkFileTree(
			rootPath,
			EnumSet.of(FileVisitOption.FOLLOW_LINKS), // Resolve symbolic links to give the operator extra flexibility
			1, // Do not walk into subdirectories
			new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					final String fileName = file.getFileName().toString();
					final String entityLabel = fileName.substring(
						0, Math.max(fileName.length() - 4, 0)
					).toUpperCase(Locale.ROOT);

					// Only consider files with .tsv extension, ignoring case
					if (!Files.isDirectory(file) && fileName.substring(Math.max(fileName.length() - 4, 0)).equalsIgnoreCase(".tsv")) {
						try (final CSVParser tsvParser = DICTIONARY_FILE_FORMAT.parse(
							Files.newBufferedReader(file, DICTIONARY_FILE_CHARSET)
						)) {
							final List<String> headerFieldNames = Arrays.stream(DictionaryFileFields.values())
								.map(DictionaryFileFields::toString)
								.collect(Collectors.toUnmodifiableList());

							if (!tsvParser.getHeaderNames().containsAll(headerFieldNames)) {
								throw new IOException("Not all required fields are present in the header");
							}

							for (final CSVRecord record : tsvParser) {
								final String term = record.get(DictionaryFileFields.TERM);
								final String synonyms;

								try {
									synonyms = record.get(DictionaryFileFields.SYNONYMS);
								} catch (final IllegalArgumentException exc) {
									throw new IOException(
										String.format(
											MALFORMED_INPUT_RECORD_ERROR_FORMAT_STR,
											tsvParser.getCurrentLineNumber(), file,
											"missing synonyms"
										)
									);
								}

								// The term is required, and can't be blank
								if (term == null || term.isBlank()) {
									throw new IOException(
										String.format(
											MALFORMED_INPUT_RECORD_ERROR_FORMAT_STR,
											tsvParser.getCurrentLineNumber(), file,
											"missing term"
										)
									);
								}

								final Set<NamedEntityTerm> terms = labelTerms.computeIfAbsent(
									entityLabel, (final String key) -> new LinkedHashSet<>()
								);
								final String[] synonymArray = synonyms.split(",");
								final Set<String> synonymList = new LinkedHashSet<>(
									(int) Math.ceil(synonymArray.length / 0.75)
								);

								// Add the term and its synonyms to the term set for this label.
								// Ignore empty synonyms, if they are present, because they are meaningless
								for (final String synonym : synonymArray) {
									String normalizedSynonym;

									if (
										!synonym.isBlank() &&
										// Make sure the synonym is not an already registered term
										!terms.contains(
											new NamedEntityTerm(
												normalizedSynonym = synonym.toLowerCase(Locale.ROOT),
												Set.of()
											)
										)
									) {
										synonymList.add(normalizedSynonym);
									}
								}
								terms.add(
									new NamedEntityTerm(
										term.toLowerCase(Locale.ROOT),
										Collections.unmodifiableSet(synonymList)
									)
								);
							}
						}
					}

					return FileVisitResult.CONTINUE;
				}
			}
		);

		return Collections.unmodifiableMap(labelTerms);
	}

	/**
	 * Converts the specified named entity map, as returned by
	 * {@link #namedEntitiesFromPathChildren(Path)}, to a seed words files property
	 * that can be passed to SPIED ({@link GetPatternsFromDataMultiClass}) for
	 * further processing. The seed words files specified in the property will be
	 * created in the temporary directory reported by the operating system, and
	 * should be cleaned up when they are no longer necessary by calling
	 * {@link es.uvigo.esei.sing.textproc.step.corenlpentityextraction.NamedEntityDictionaryHelper.PropertyWithTemporaryFiles#close()}
	 * explicitly or implicitly (for instance, in a try-with-resources block).
	 *
	 * @param labelTerms The map that associates named entity type labels with its
	 *                   terms, as returned by
	 *                   {@link #namedEntitiesFromPathChildren(Path)}.
	 * @return The property to pass to SPIED, whose value can be retrieved by
	 *         calling
	 *         {@link es.uvigo.esei.sing.textproc.step.corenlpentityextraction.NamedEntityDictionaryHelper.PropertyWithTemporaryFiles#getValue()}.
	 * @throws IOException              If some I/O error occurs during the
	 *                                  operation.
	 * @throws IllegalArgumentException If {@code labelTerms} is {@code null}.
	 */
	public static PropertyWithTemporaryFiles namedEntitiesToSeedWordsFilesProperty(@NonNull final Map<String, Set<NamedEntityTerm>> labelTerms) throws IOException {
		final StringBuilder propertyValueBuilder = new StringBuilder();
		final List<Path> temporaryFilePaths = new ArrayList<>();

		final Path tempDir = Files.createTempDirectory("TP_NEDH");
		// Delete the temporary folder on normal VM exit
		// if we don't do that before (we should)
		tempDir.toFile().deleteOnExit();

		for (final Entry<String, Set<NamedEntityTerm>> namedEntitiesEntry : labelTerms.entrySet()) {
			final Path tempFile = Files.createTempFile(tempDir, namedEntitiesEntry.getKey(), null);

			// The default charset is the one that will be used by CoreNLP,
			// as it delegates the choice to the InputStreamReader one argument constructor
			try (final Writer fileWriter = Files.newBufferedWriter(tempFile, Charset.defaultCharset())) {
				for (final NamedEntityTerm term : namedEntitiesEntry.getValue()) {
					fileWriter.write(term.getTerm());
					fileWriter.write('\n'); // CoreNLP IOUtils uses BufferedReader for reading lines

					for (final String synonym : term.getSynonyms()) {
						fileWriter.write(synonym);
						fileWriter.write('\n');
					}
				}
			}

			propertyValueBuilder
				.append(namedEntitiesEntry.getKey())
				.append(',')
				.append(tempFile.toRealPath().toString())
				.append(';');

			temporaryFilePaths.add(tempFile);

			// Delete the files in the folder too, because it is not specified that
			// deleting a folder with deleteOnExit deletes its files recursively
			tempFile.toFile().deleteOnExit();
		}

		// Remove trailing ";"
		if (propertyValueBuilder.length() > 0) {
			propertyValueBuilder.deleteCharAt(propertyValueBuilder.length() - 1);
		}

		return new PropertyWithTemporaryFiles(
			propertyValueBuilder.toString(),
			Collections.unmodifiableList(temporaryFilePaths)
		);
	}

	/**
	 * Holds the value for a property, and also provides a {@link #close()} method
	 * to release temporary files created for this property.
	 *
	 * @author Alejandro González García
	 */
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class PropertyWithTemporaryFiles implements Closeable {
		@NonNull @Getter
		private final String value;
		@NonNull
		private final List<Path> temporaryFilePaths;

		@Override
		public void close() throws IOException {
			for (final Path path : temporaryFilePaths) {
				try {
					Files.deleteIfExists(path);
				} catch (final DirectoryNotEmptyException ignored) {}
			}
		}
	}

	/**
	 * Represents a named entity term, which can have synonyms. Two named entity
	 * terms are equal as per the {@link #equals(Object)} method if and only if the
	 * term they represent is equal, without considering synonym lists.
	 *
	 * @author Alejandro González García
	 */
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class NamedEntityTerm {
		@NonNull @Getter
		private final String term;
		@NonNull @Getter
		private final Set<String> synonyms;

		@Override
		public int hashCode() {
			return term.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			return obj instanceof NamedEntityTerm &&
				term.equals(((NamedEntityTerm) obj).term);
		}
	}

	/**
	 * Represents the fields that each record in a named entity dictionary file can
	 * contain.
	 *
	 * @author Alejandro González García
	 */
	private static enum DictionaryFileFields {
		/**
		 * The term field, which contains a term.
		 */
		TERM,
		/**
		 * A list of synonyms for the term, separated by a comma.
		 */
		SYNONYMS;

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
