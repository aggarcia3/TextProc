// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tppstopwordfiltering;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import es.uvigo.esei.sing.textproc.abstracttppstep.AbstractTppProcessingStep;
import es.uvigo.esei.sing.textproc.abstracttppstep.JsonResponseAttributeType;
import es.uvigo.esei.sing.textproc.abstracttppstep.ProcessingBiConsumer;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.entity.StopwordFilteredTextDocument;
import es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.entity.StopwordFilteredTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.xml.definition.LanguageProcessingStepParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Removes stopwords from documents, using Text Processing Python.
 *
 * @author Alejandro González García
 */
final class TppStopwordFilteringProcessingStep extends AbstractTppProcessingStep {
	private static final String LANGUAGE_PROCESSING_STEP_PARAMETER_NAME = new LanguageProcessingStepParameter().getName();

	/**
	 * Instantiates a Text Processing Python stopword filtering processing step.
	 *
	 * @param requestParametersAction The action to use to add the language
	 *                                parameter to the request.
	 * @throws IllegalArgumentException If the parameter is {@code null}.
	 */
	public TppStopwordFilteringProcessingStep(@NonNull final AddLanguageParameterAction requestParametersAction) {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(
				LANGUAGE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.trim().isEmpty()
			),
			// Additional mandatory parameters
			Set.of(LANGUAGE_PROCESSING_STEP_PARAMETER_NAME),
			// Result entity types
			List.of(StopwordFilteredTextWithTitleDocument.class, StopwordFilteredTextDocument.class),
			// Step description format string
			"Removing %s stopwords",
			// JSON response attribute type
			JsonResponseAttributeType.STRING,
			// Request parameters action
			requestParametersAction,
			// Attribute store action
			new FilteredAttributeStoreAction()
		);
	}

	/**
	 * Adds the stopword filtering algorithm parameters to the generated JSON
	 * request. The object who instantiates this class must call
	 * {@link #setStep(TppStopwordFilteringProcessingStep)} ASAP.
	 *
	 * @author Alejandro González García
	 */
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	static final class AddLanguageParameterAction implements ProcessingConsumer<JsonGenerator> {
		@NonNull @Setter(AccessLevel.PACKAGE)
		private TppStopwordFilteringProcessingStep step;

		@Override
		public void accept(final JsonGenerator requestJsonGenerator) throws ProcessingException {
			try {
				requestJsonGenerator
					.writeStartObject("parameters")
						.write("language", step.getParameters().get(LANGUAGE_PROCESSING_STEP_PARAMETER_NAME))
					.writeEnd();
			} catch (final Exception exc) {
				throw new ProcessingException(
					"An exception occurred while adding parameters to a request", exc
				);
			}
		}
	}

	/**
	 * Parses a stopword filtered document attribute from the corresponding Text
	 * Processing Python JSON response object into a map.
	 *
	 * @author Alejandro González García
	 */
	private static final class FilteredAttributeStoreAction implements ProcessingBiConsumer<Entry<String, JsonObject>, Map<String, String>> {
		@Override
		public void accept(
			final Entry<String, JsonObject> returnedAttributeObject, final Map<String, String> processedAttributesMap
		) throws ProcessingException {
			try {
				processedAttributesMap.put(
					returnedAttributeObject.getKey(), returnedAttributeObject.getValue().getString("text")
				);
			} catch (final Exception exc) {
				throw new ProcessingException(
					"An exception occurred while processing the stopword filtered document", exc
				);
			}
		}
	}
}
