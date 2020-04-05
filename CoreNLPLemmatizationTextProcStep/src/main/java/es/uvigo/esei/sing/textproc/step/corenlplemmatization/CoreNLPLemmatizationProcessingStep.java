// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlplemmatization;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator.TokenizerType;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.corenlplemmatization.entity.CoreNLPLemmatizedTextDocument;
import es.uvigo.esei.sing.textproc.step.corenlplemmatization.entity.CoreNLPLemmatizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.corenlplemmatization.xml.definition.ModelProcessingStepParameter;

/**
 * Lemmatizes tokens of documents, according to the provided parameters, using
 * Stanford CoreNLP. This step also normalizes documents to lowercase; see
 * <a href=
 * "https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/process/Morphology.html">https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/process/Morphology.html</a>.
 * <p>
 * Example declaration for this step in a process definition file:
 * </p>
 * <pre>
 * {@code <step action="CoreNLPLemmatization">
 * 	<parameters>
 * 		<textDocumentWithTitleTableName>stopword_filtered_submission</textDocumentWithTitleTableName>
 * 		<textDocumentTableName>stopword_filtered_comment</textDocumentTableName>
 * 	</parameters>
 * </step>}
 * </pre>
 *
 * @author Alejandro González García
 */
final class CoreNLPLemmatizationProcessingStep extends AbstractProcessingStep {
	private static final String MODEL_PROCESSING_STEP_PARAMETER_NAME = new ModelProcessingStepParameter().getName();

	/**
	 * Instantiates a Stanford CoreNLP lemmatization processing step.
	 */
	CoreNLPLemmatizationProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
			// Additional mandatory parameters
			Set.of()
		);
	}

	@Override
	protected void run() throws ProcessingException {
		final List<Class<? extends ProcessedDocument>> processedDocumentTypes = List.of(
			CoreNLPLemmatizedTextWithTitleDocument.class, CoreNLPLemmatizedTextDocument.class
		);

		final AnnotationPipeline nlpPipeline = new AnnotationPipeline();
		// We assume the input is already tokenized, so we use a cheap whitespace tokenizer
		nlpPipeline.addAnnotator(new TokenizerAnnotator(false, TokenizerType.Whitespace));
		// This annotator is required by MorphaAnnotator
		nlpPipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		// POS tags are required by lemmatization
		nlpPipeline.addAnnotator(
			new POSTaggerAnnotator(
				getParameters().getOrDefault(
					MODEL_PROCESSING_STEP_PARAMETER_NAME,
					// Mirror of a model included with the latest CoreNLP models (as of 03-26-2020)
					"https://dl.dropboxusercontent.com/s/bmqus6uon0cr9ek/english-caseless-left3words-distsim.tagger"
				), false, Integer.MAX_VALUE, 1
			)
		);
		nlpPipeline.addAnnotator(new MorphaAnnotator(false));

		try {
			// Delete previous results
			for (final Class<? extends ProcessedDocument> processedDocumentType : processedDocumentTypes) {
				deleteAllProcessedDocumentsOfType(processedDocumentType);
			}

			for (int i = 0; i < processedDocumentTypes.size(); ++i) {
				final String[] unprocessedAttributeNames = unprocessedDocumentsAttributes.get(i);
				final Class<? extends ProcessedDocument> processedDocumentType = processedDocumentTypes.get(i);

				forEachDocumentInNativeQuery(
					unprocessedDocumentsQuerySuppliers.get(i),
					String.format("Lemmatizing %s", unprocessedDocumentTypesNames.get(i)),
					numberOfUnprocessedEntitiesProviders.get(i).get(),
					(final List<String[]> batchAttributes) -> {
						final Map<String, String> processedAttributesMap = new HashMap<>(
							(int) Math.ceil(unprocessedAttributeNames.length / 0.75)
						);
						final StringBuilder tokenizedAttribute = new StringBuilder();

						for (final String[] documentAttributes : batchAttributes) {
							for (int j = 0; j < unprocessedAttributeNames.length; ++j) {
								final Annotation annotatedAttribute = new Annotation(documentAttributes[j + 1]);

								nlpPipeline.annotate(annotatedAttribute);
								for (final CoreLabel token : annotatedAttribute.get(TokensAnnotation.class)) {
									tokenizedAttribute
										.append(token.get(LemmaAnnotation.class))
										.append(' ');
								}

								// Remove trailing space
								if (tokenizedAttribute.length() > 0) {
									tokenizedAttribute.deleteCharAt(tokenizedAttribute.length() - 1);
								}

								processedAttributesMap.put(unprocessedAttributeNames[j], tokenizedAttribute.toString());

								// Reuse string builder buffer
								tokenizedAttribute.setLength(0);
							}

							saveProcessedDocument(
								processedDocumentType, Integer.parseInt(documentAttributes[0]),
								Collections.unmodifiableMap(processedAttributesMap)
							);
						}
					},
					null
				);
			}
		} catch (final IllegalArgumentException | PersistenceException exc) {
			throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
		}
	}
}
