package es.uvigo.esei.sing.textproc.step.tpplemmatization;

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
import es.uvigo.esei.sing.textproc.step.tpplemmatization.entity.LemmatizedTextDocument;
import es.uvigo.esei.sing.textproc.step.tpplemmatization.entity.LemmatizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.tpplemmatization.xml.definition.ModelProcessingStepParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Lemmatizes all the words in a document, according to the provided parameters,
 * using Text Processing Python.
 *
 * @author Alejandro González García
 */
final class TppLemmatizationProcessingStep extends AbstractTppProcessingStep {
	private static final String MODEL_PROCESSING_STEP_PARAMETER_NAME = new ModelProcessingStepParameter().getName();

	/**
	 * Instantiates a Text Processing Python lemmatization processing step.
	 *
	 * @param requestParametersAction The action to use to add the model parameter
	 *                                to the request.
	 * @throws IllegalArgumentException If the parameter is {@code null}.
	 */
	public TppLemmatizationProcessingStep(@NonNull final AddModelParameterAction requestParametersAction) {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(
				MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.trim().isEmpty()
			),
			// Additional mandatory parameters
			Set.of(MODEL_PROCESSING_STEP_PARAMETER_NAME),
			// Result entity types
			List.of(LemmatizedTextWithTitleDocument.class, LemmatizedTextDocument.class),
			// Step description format string
			"Lemmatizing %s",
			// JSON response attribute type
			JsonResponseAttributeType.STRING,
			// Request parameters action
			requestParametersAction,
			// Attribute store action
			new LemmatizedAttributeStoreAction()
		);
	}

	/**
	 * Adds the language model algorithm parameter to the generated JSON request.
	 * The object who instantiates this class must call
	 * {@link #setStep(TppLemmatizationProcessingStep)} ASAP.
	 *
	 * @author Alejandro González García
	 */
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	static final class AddModelParameterAction implements ProcessingConsumer<JsonGenerator> {
		@NonNull @Setter(AccessLevel.PACKAGE)
		private TppLemmatizationProcessingStep step;

		@Override
		public void accept(final JsonGenerator requestJsonGenerator) throws ProcessingException {
			try {
				requestJsonGenerator
					.writeStartObject("parameters")
						.write("model", step.getParameters().get(MODEL_PROCESSING_STEP_PARAMETER_NAME))
					.writeEnd();
			} catch (final Exception exc) {
				throw new ProcessingException(
					"An exception occurred while adding parameters to a request", exc
				);
			}
		}
	}

	/**
	 * Parses a lemmatized document attribute from the corresponding Text Processing
	 * Python JSON response object into a map.
	 *
	 * @author Alejandro González García
	 */
	private static final class LemmatizedAttributeStoreAction implements ProcessingBiConsumer<Entry<String, JsonObject>, Map<String, String>> {
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
					"An exception occurred while processing the lemmatized document", exc
				);
			}
		}
	}
}
