// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlptokenization;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.entity.CoreNLPTokenizedTextDocument;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.entity.CoreNLPTokenizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.xml.definition.LanguageProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.xml.definition.TokenizerOptionsProcessingStepParameter;

/**
 * Tokenizes documents, according to the provided parameters, using Stanford
 * CoreNLP.
 *
 * @author Alejandro González García
 */
final class CoreNLPTokenizationProcessingStep extends AbstractProcessingStep {
	private static final String LANGUAGE_PROCESSING_STEP_PARAMETER_NAME = new LanguageProcessingStepParameter().getName();
	private static final String TOKENIZER_OPTIONS_PROCESSING_STEP_PARAMETER_NAME = new TokenizerOptionsProcessingStepParameter().getName();

	/**
	 * Instantiates a Stanford CoreNLP tokenization processing step.
	 */
	CoreNLPTokenizationProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(
				LANGUAGE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank(),
				TOKENIZER_OPTIONS_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()
			),
			// Additional mandatory parameters
			Set.of(LANGUAGE_PROCESSING_STEP_PARAMETER_NAME)
		);
	}

	@Override
	protected void run() throws ProcessingException {
		final List<Class<? extends ProcessedDocument>> processedDocumentTypes = List.of(
			CoreNLPTokenizedTextWithTitleDocument.class, CoreNLPTokenizedTextDocument.class
		);

		final AnnotationPipeline nlpPipeline = new AnnotationPipeline();
		nlpPipeline.addAnnotator(
			new TokenizerAnnotator(
				false,
				getParameters().get(LANGUAGE_PROCESSING_STEP_PARAMETER_NAME),
				getParameters().getOrDefault(TOKENIZER_OPTIONS_PROCESSING_STEP_PARAMETER_NAME, "quotes=ascii")
			)
		);

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
					String.format("Tokenizing %s", unprocessedDocumentTypesNames.get(i)),
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
										.append(token.get(TextAnnotation.class))
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
