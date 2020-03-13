// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.mentionfiltering;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.mentionfiltering.entity.MentionFilteredTextDocument;
import es.uvigo.esei.sing.textproc.step.mentionfiltering.entity.MentionFilteredTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.mentionfiltering.xml.definition.MentionTypeProcessingStepParameter;

/**
 * Removes all the mentions in a document, according to the provided mention
 * type.
 *
 * @author Alejandro González García
 */
final class MentionFilteringProcessingStep extends AbstractProcessingStep {
	private static final String MENTION_TYPE_PROCESSING_STEP_PARAMETER_NAME = new MentionTypeProcessingStepParameter().getName();

	private static final Map<String, Pattern> MENTION_REGEXES = Map.of(
		"reddit_all", Pattern.compile(
			"(?:/[\\p{Blank}]*)?[ru][\\p{Blank}]*/[\\p{Blank}]*[^\\p{Blank}]+",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
		),
		"reddit_user", Pattern.compile(
			"(?:/[\\p{Blank}]*)?u[\\p{Blank}]*/[\\p{Blank}]*[^\\p{Blank}]+",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
		),
		"subreddit", Pattern.compile(
			"(?:/[\\p{Blank}]*)?r[\\p{Blank}]*/[\\p{Blank}]*[^\\p{Blank}]+",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
		)
	);

	/**
	 * Instantiates a mention filtering processing step.
	 */
	MentionFilteringProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(
				MENTION_TYPE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> MENTION_REGEXES.containsKey(value)
			),
			// Additional mandatory parameters
			Set.of(
				MENTION_TYPE_PROCESSING_STEP_PARAMETER_NAME
			)
		);
	}

	@Override
	protected void run() throws ProcessingException {
		final List<Class<? extends ProcessedDocument>> processedDocumentTypes = List.of(
			MentionFilteredTextWithTitleDocument.class, MentionFilteredTextDocument.class
		);
		final Pattern mentionRegex = MENTION_REGEXES.get(
			getParameters().get(MENTION_TYPE_PROCESSING_STEP_PARAMETER_NAME)
		);
		final Pattern severalSpaceRegex = Pattern.compile("[\\p{Blank}]{2,}", Pattern.UNICODE_CHARACTER_CLASS);

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
					String.format("Filtering %s mentions", unprocessedDocumentTypesNames.get(i)),
					numberOfUnprocessedEntitiesProviders.get(i).get(),
					(final List<String[]> batchAttributes) -> {
						final Map<String, String> processedAttributesMap = new HashMap<>(
							(int) Math.ceil(unprocessedAttributeNames.length / 0.75)
						);

						for (final String[] documentAttributes : batchAttributes) {
							for (int j = 0; j < unprocessedAttributeNames.length; ++j) {
								processedAttributesMap.put(unprocessedAttributeNames[j], severalSpaceRegex.matcher(
									mentionRegex.matcher(documentAttributes[j + 1]).replaceAll("")
								).replaceAll(""));
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
