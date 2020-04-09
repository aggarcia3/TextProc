// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpentityextraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.persistence.PersistenceException;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.patterns.DataInstance;
import edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass;
import edu.stanford.nlp.patterns.PatternFactory;
import edu.stanford.nlp.patterns.surface.SurfacePattern;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator.TokenizerType;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.NamedEntityDictionaryHelper.NamedEntityTerm;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.NamedEntityDictionaryHelper.PropertyWithTemporaryFiles;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.NERMappingsFileStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.OverwritableNERCategoriesStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.PropertiesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.SeedWordsDirectoryProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.util.VariableHolder;
import es.uvigo.esei.sing.textproc.step.util.PathUtil;

/**
 * Extracts new named entities from the input documents, from a seed set of
 * named entities, using the bootstrapped pattern-based learning algorithm
 * included with CoreNLP.
 * <p>
 * Example declaration for this step in a process definition file:
 * </p>
 * <pre>
 * {@code <step action="CoreNLPEntityExtraction">
 * 	<parameters>
 * 		<textDocumentWithTitleTableName>non_empty_submission</textDocumentWithTitleTableName>
 * 		<textDocumentTableName>non_empty_comment</textDocumentTableName>
 * 		<cnlpee:seedWordsFilesDirectory>entityextraction/dictionaries</cnlpee:seedWordsFilesDirectory>
 * 		<cnlpee:nerMappingsFile>entityextraction/entities.tsv</cnlpee:nerMappingsFile>
 * 		<cnlpee:overwritableNerCategories>CAUSE_OF_DEATH,IDEOLOGY,PERSON,ORGANIZATION</cnlpee:overwritableNerCategories>
 * 	</parameters>
 * </step>}
 * </pre>
 *
 * @author Alejandro González García
 */
final class CoreNLPEntityExtractionProcessingStep extends AbstractProcessingStep {
	private static final String PROPERTIES_FILE_STEP_PARAMETER_NAME = new PropertiesFileProcessingStepParameter().getName();
	private static final String SEED_WORDS_DIR_STEP_PARAMETER_NAME = new SeedWordsDirectoryProcessingStepParameter().getName();
	private static final String NER_MAPPINGS_FILE_STEP_PARAMETER_NAME = new NERMappingsFileStepParameter().getName();
	private static final String OVERWRITABLE_NER_CATEGORIES_STEP_PARAMETER_NAME = new OverwritableNERCategoriesStepParameter().getName();

	private static final Properties DEFAULT_SPIED_PROPERTIES;

	private static final String CPU_THREADS_STRING = Integer.toString(Runtime.getRuntime().availableProcessors());

	static {
		try {
			DEFAULT_SPIED_PROPERTIES = new Properties();
			DEFAULT_SPIED_PROPERTIES.load(
				new InputStreamReader(
					CoreNLPEntityExtractionProcessingStep.class.getResourceAsStream("/default_spied_properties.properties"),
					StandardCharsets.UTF_8
				)
			);
		} catch (final IOException exc) {
			throw new ExceptionInInitializerError(
				"Couldn't load the default SPIED properties resource. Is the JAR of this step correctly packaged?"
			);
		}
	}

	/**
	 * Instantiates a Stanford CoreNLP named entity extraction processing step.
	 */
	CoreNLPEntityExtractionProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(
				SEED_WORDS_DIR_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank(),
				PROPERTIES_FILE_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank(),
				NER_MAPPINGS_FILE_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank(),
				OVERWRITABLE_NER_CATEGORIES_STEP_PARAMETER_NAME, (final String value) -> value != null
			),
			// Additional mandatory parameters
			Set.of(
				SEED_WORDS_DIR_STEP_PARAMETER_NAME, NER_MAPPINGS_FILE_STEP_PARAMETER_NAME,
				OVERWRITABLE_NER_CATEGORIES_STEP_PARAMETER_NAME
			)
		);
	}

	@Override
	protected void run() throws ProcessingException {
		final Object spiedLock = new Object();
		final VariableHolder<Boolean> modelGenerated = new VariableHolder<>(false);
		final List<String> overwritableNerCategories = ((Function<List<String>, List<String>>)
			// A single blank overwritable category should be an empty list,
			// because it means no categories to overwrite
			(final List<String> l) -> l.get(0).isBlank() ? Collections.emptyList() : l
		).apply(
			Arrays.asList(getParameters().get(OVERWRITABLE_NER_CATEGORIES_STEP_PARAMETER_NAME).trim().split(","))
		);

		try (final PrintStream nullPrintStream = new PrintStream(OutputStream.nullOutputStream())) {
			final Map<String, Set<NamedEntityTerm>> labelTerms = NamedEntityDictionaryHelper.namedEntitiesFromPathChildren(
				Path.of(getParameters().get(SEED_WORDS_DIR_STEP_PARAMETER_NAME))
			);

			try (final NERMappingsFileWriter nerMappingsWriter = new NERMappingsFileWriter(
					Path.of(getParameters().get(NER_MAPPINGS_FILE_STEP_PARAMETER_NAME))
				)
			) {
				final Path temporaryProcessingDirectory = Files.createTempDirectory("TP_CEES");
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					// Just in case the user interrupts us, or we never manage to reach the finally
					PathUtil.deletePathRecursively(temporaryProcessingDirectory);
				}));

				try (final PropertyWithTemporaryFiles seedWordsFilesProperty = NamedEntityDictionaryHelper.namedEntitiesToSeedWordsFilesProperty(labelTerms)) {
					// Directory where SPIED stores learning results which should be
					// carried on from document to document
					final Path temporaryOutputDirectory = Files.createDirectory(temporaryProcessingDirectory.resolve("out"));
					final Path temporaryModelDirectory;

					// With every input parsed, it is a good time to add initial NER mappings to the result file
					for (final Entry<String, Set<NamedEntityTerm>> labelTermsEntry : labelTerms.entrySet()) {
						final String label = labelTermsEntry.getKey();
						for (final NamedEntityTerm term : labelTermsEntry.getValue()) {
							nerMappingsWriter.writeMapping(term.getTerm(), label, overwritableNerCategories);
							for (final String synonym : term.getSynonyms()) {
								nerMappingsWriter.writeMapping(synonym, label, overwritableNerCategories);
							}
						}
					}

					final Properties spiedProperties = new Properties();
					spiedProperties.putAll(DEFAULT_SPIED_PROPERTIES); // The default properties mechanism doesn't work with CoreNLP
					// Set a reasonable default value for numThreads
					spiedProperties.setProperty("numThreads", CPU_THREADS_STRING);

					// Load custom properties, if applicable
					if (getParameters().get(PROPERTIES_FILE_STEP_PARAMETER_NAME) != null) {
						spiedProperties.load(
							new FileReader(
								getParameters().get(PROPERTIES_FILE_STEP_PARAMETER_NAME), StandardCharsets.UTF_8
							)
						);
					}

					spiedProperties.setProperty("seedWordsFiles", seedWordsFilesProperty.getValue());
					spiedProperties.setProperty("fileFormat", "ser"); // We generate this serialized format for performance. See below
					spiedProperties.setProperty("outDir", temporaryOutputDirectory.toRealPath().toString());
					temporaryModelDirectory = Files.createDirectories(
						temporaryProcessingDirectory.resolve(spiedProperties.getProperty("identifier")).resolve("model")
					);
					spiedProperties.setProperty(
						"patternsWordsDir",
						// Yes, appending the separator at the end is actually needed...
						// Otherwise, CoreNLP doesn't interpret the path correctly and it
						// breaks silently :)
						temporaryModelDirectory.toRealPath().toString() + File.separatorChar
					);

					final Properties nerAnnotatorProperties = new Properties();
					nerAnnotatorProperties.setProperty("ner.model", spiedProperties.getProperty("nerModelPaths"));
					// Time named entities are recognized with SUTime, which needs model files.
					// We don't need to recognize time named entities, so we can disable
					// SUTime entirely
					nerAnnotatorProperties.setProperty("ner.useSUTime", Boolean.toString(false));
					nerAnnotatorProperties.setProperty("ner.applyFineGrained", spiedProperties.getProperty("applyFineGrainedRegexner"));
					nerAnnotatorProperties.setProperty("ner.fine.regexner.mapping", spiedProperties.getProperty("fineGrainedRegexnerMapping"));
					// Use all CPU threads for NER. This usually improves performance
					nerAnnotatorProperties.setProperty("ner.nthreads", CPU_THREADS_STRING);

					// We have the seed words files. Now it is a good time to create the pipeline.
					// Although SPIED automatically creates a pipeline and reuses it, we can do better
					// by handling things ourselves. This minimizes overheads and gives us control of
					// temporary serialized files. See:
					// https://github.com/stanfordnlp/CoreNLP/blob/a9a4c2d75b177790a24c0f46188810668d044cd8/src/edu/stanford/nlp/patterns/GetPatternsFromDataMultiClass.java#L654
					final AnnotationPipeline nlpPipeline = new AnnotationPipeline();
					// We assume the input is already tokenized, so we use a cheap whitespace tokenizer.
					// The original code uses this property for the tokenizer:
					// props.setProperty("tokenize.options", "ptb3Escaping=false,normalizeParentheses=false,escapeForwardSlashAsterisk=false");
					nlpPipeline.addAnnotator(new TokenizerAnnotator(false, TokenizerType.Whitespace));
					// Required by SPIED
					nlpPipeline.addAnnotator(new WordsToSentencesAnnotator(false));
					// Required by SPIED
					nlpPipeline.addAnnotator(
						new POSTaggerAnnotator(
							new MaxentTagger(spiedProperties.getProperty("posModelPath"))
						)
					);
					// Required by SPIED
					nlpPipeline.addAnnotator(new MorphaAnnotator(false));
					// Required by SPIED
					nlpPipeline.addAnnotator(new NERCombinerAnnotator(nerAnnotatorProperties));

					// Pipeline ready. Time to do the extraction for each document
					for (int i = 0; i < unprocessedDocumentTypesNames.size(); ++i) {
						final String[] unprocessedAttributeNames = unprocessedDocumentsAttributes.get(i);

						forEachDocumentInNativeQuery(
							unprocessedDocumentsQuerySuppliers.get(i),
							String.format("Extracting extra NE from %s", unprocessedDocumentTypesNames.get(i)),
							numberOfUnprocessedEntitiesProviders.get(i).get(),
							(final List<String[]> batchAttributes) -> {
								int k = 0;
								final StringBuilder concatenatedDocuments = new StringBuilder(
									// On average, there are 5 letters per word (+ 1 for space)
									6 *
									// Most social media posts contain 150 or less words
									150 *
									// How many posts (documents) are we processing now
									Integer.parseInt(
										getParameters().getOrDefault(
											BATCH_SIZE_STEP_PARAMETER_NAME, DEFAULT_BATCH_SIZE_STEP_PARAMETER
										)
									)
								);
								final Map<String, DataInstance> sentenceMap = new HashMap<>();

								// Concatenate all attributes of all documents in a buffer for
								// top efficiency
								for (final String[] documentAttributes : batchAttributes) {
									for (int j = 0; j < unprocessedAttributeNames.length; ++j) {
										if (!documentAttributes[j + 1].isBlank()) {
											concatenatedDocuments.append(documentAttributes[j + 1])
												.append('\n');
										}
									}
								}

								// In the unlikely case that the resulting document is empty, ignore it
								final String document = concatenatedDocuments.toString().strip();
								if (document.isEmpty()) {
									return;
								}

								// Now store every sentence of the concatenated documents buffer in a map.
								// Based on https://github.com/stanfordnlp/CoreNLP/blob/a9a4c2d75b177790a24c0f46188810668d044cd8/src/edu/stanford/nlp/patterns/GetPatternsFromDataMultiClass.java#L702
								final Annotation annotatedDocument = new Annotation(document);
								nlpPipeline.annotate(annotatedDocument);
								for (final CoreMap sentence : annotatedDocument.get(SentencesAnnotation.class)) {
									sentenceMap.put(
										Integer.toString(k++), // Luckily, the key value just needs to be unique
										DataInstance.getNewInstance(PatternFactory.PatternType.SURFACE, sentence)
									);
								}

								Path temporarySentencesFile = null;
								try {
									temporarySentencesFile = Files.createTempFile(
										temporaryProcessingDirectory, "docsents", ".ser"
									);

									// Serialize the map, like IOUtils does
									// (but better, because this closes the file even if an exception is thrown)
									try (
										final ObjectOutputStream sentenceMapStream = new ObjectOutputStream(
											Files.newOutputStream(temporarySentencesFile)
										)
									) {
										sentenceMapStream.writeObject(sentenceMap);
									}

									spiedProperties.setProperty("file", temporarySentencesFile.toRealPath().toString());

									// SPIED has to be executed serially, because each run for each document attribute
									// updates the model, and the model isn't supposed to be read and updated concurrently.
									// However, the numThreads property allows to parallelize the actual entity extraction
									// process, which is the time consuming thing here
									final List<Entry<String, Path>> learnedWordsFiles = new ArrayList<>();
									synchronized (spiedLock) {
										spiedProperties.setProperty("loadSavedPatternsWordsDir", modelGenerated.getVariable().toString());

										final PrintStream stdout = System.out;
										final PrintStream stderr = System.err;
										try {
											// SPIED pollutes standard output no matter what.
											// Avoid that by reassigning the output streams temporarily
											System.setOut(nullPrintStream);
											System.setErr(nullPrintStream);

											GetPatternsFromDataMultiClass.<SurfacePattern>run(spiedProperties);
										} finally {
											System.setOut(stdout);
											System.setErr(stderr);
										}

										modelGenerated.setVariable(true);

										// SPIED stores the learned words here:
										// ${temporaryOutputDirectory}/${identifier}/${label}/learnedwords.txt
										// These files are overwritten between runs, so their contents need to
										// be copied elsewhere. The resulting NER mapping file is the ideal place,
										// but we are in a critical section here, so copy the files to another path
										// and process them later
										for (final String label : labelTerms.keySet()) {
											learnedWordsFiles.add(new SimpleImmutableEntry<>(label,
												Files.copy(
													temporaryOutputDirectory
														.resolve(spiedProperties.getProperty("identifier"))
														.resolve(label)
														.resolve("learnedwords.txt"),
													Files.createTempFile(
														temporaryProcessingDirectory, "learnedwords", ".txt"
													),
													StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES
												)
											));
										}
									}

									// We are out of the critical section now.
									// Add the learned words to the mapping file with the least contention possible
									for (final Entry<String, Path> learnedWordsFile : learnedWordsFiles) {
										final String label = learnedWordsFile.getKey();

										try (final BufferedReader reader = new BufferedReader(
											new InputStreamReader(
												Files.newInputStream(
													learnedWordsFile.getValue(), StandardOpenOption.DELETE_ON_CLOSE
												), StandardCharsets.UTF_8
											)
										)) {
											String word;
											while ((word = reader.readLine()) != null) {
												if (!word.isBlank()) {
													nerMappingsWriter.writeMapping(word, label, overwritableNerCategories);
												}
											}
										}
									}
								} catch (
									final IOException | ReflectiveOperationException |
									InterruptedException | ExecutionException | SQLException exc
								) {
									throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
								} finally {
									// Clean up the current temporary sentences file
									if (temporarySentencesFile != null) {
										try {
											Files.deleteIfExists(temporarySentencesFile);
										} catch (final IOException ignored) {}
									}
								}
							},
							null
						);
					}
				} finally {
					// Remove temporary files directory
					PathUtil.deletePathRecursively(temporaryProcessingDirectory);
				}
			}
		} catch (final IOException | IllegalArgumentException | PersistenceException exc) {
			throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
		}
	}
}
