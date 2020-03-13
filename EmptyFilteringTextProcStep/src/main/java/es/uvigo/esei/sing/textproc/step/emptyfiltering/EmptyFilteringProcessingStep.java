// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.emptyfiltering;

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
import es.uvigo.esei.sing.textproc.step.emptyfiltering.entity.NonEmptyTextDocument;
import es.uvigo.esei.sing.textproc.step.emptyfiltering.entity.NonEmptyTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.emptyfiltering.xml.definition.IncludeRedditDeletedProcessingStepParameter;

/**
 * Skips copying to a processed entity a unprocessed entity that has all its
 * texts attributes empty (or filled with non-alphabetical characters).
 *
 * @author Alejandro González García
 */
final class EmptyFilteringProcessingStep extends AbstractProcessingStep {
	private static final String INCLUDE_REDDIT_DELETED_PROCESSING_STEP_PARAMETER_NAME = new IncludeRedditDeletedProcessingStepParameter().getName();
	/**
	 * Instantiates a non empty filtering processing step.
	 */
	EmptyFilteringProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(
				INCLUDE_REDDIT_DELETED_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null
			),
			// Additional mandatory parameters
			Set.of()
		);
	}

	@Override
	protected void run() throws ProcessingException {
		final List<Class<? extends ProcessedDocument>> processedDocumentTypes = List.of(
			NonEmptyTextWithTitleDocument.class, NonEmptyTextDocument.class
		);
		final Pattern emptyRegex;

		switch (getParameters().getOrDefault(INCLUDE_REDDIT_DELETED_PROCESSING_STEP_PARAMETER_NAME, "true")) {
		case "true":
		case "1":
			emptyRegex = Pattern.compile(
				"^[\\[(][\\p{Space}]*(?:removed|deleted|remove|delete)[\\p{Space}]*[\\])]$|^[^\\p{Lower}]*$",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
			);
			break;
		default:
			emptyRegex = Pattern.compile(
				"^[^\\p{Lower}]*$",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
			);
		}

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
					String.format("Filtering empty %s", unprocessedDocumentTypesNames.get(i)),
					numberOfUnprocessedEntitiesProviders.get(i).get(),
					(final List<String[]> batchAttributes) -> {
						final Map<String, String> documentAttributesMap = new HashMap<>(
							(int) Math.ceil(unprocessedAttributeNames.length / 0.75)
						);

						for (final String[] documentAttributes : batchAttributes) {
							boolean allAreEmpty = true;
							for (int j = 0; j < unprocessedAttributeNames.length && allAreEmpty; ++j) {
								allAreEmpty = emptyRegex.matcher(documentAttributes[j + 1]).matches();
							}

							if (!allAreEmpty) {
								// Pass through the document attributes
								for (int j = 0; j < unprocessedAttributeNames.length; ++j) {
									documentAttributesMap.put(unprocessedAttributeNames[j], documentAttributes[j + 1]);
								}

								saveProcessedDocument(
									processedDocumentType, Integer.parseInt(documentAttributes[0]),
									Collections.unmodifiableMap(documentAttributesMap)
								);
							}
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
