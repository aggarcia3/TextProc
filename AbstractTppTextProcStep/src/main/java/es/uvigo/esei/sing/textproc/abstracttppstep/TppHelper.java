// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.abstracttppstep;

import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep.ProcessingConsumer;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import static java.util.AbstractMap.SimpleImmutableEntry;

/**
 * Contains common logic to text processing steps that involve calling the Text
 * Processing Python web service, or any compatible web service.
 *
 * @author Alejandro González García
 */
final class TppHelper {
	private static final String USER_AGENT_STRING = "TextProc/PROTOCOL-1";

	/**
	 * Processes the batch of attributes contained in the specified list, each
	 * attribute identified by a name, by sending an appropriate POST HTTP request
	 * to Text Processing Python, and parsing the resulting JSON response. The
	 * resulting list contains maps of attribute names with their processed
	 * versions, in the same order as the input documents in the batch.
	 *
	 * @param attributesBatch               The batch of attribute values, as
	 *                                      directly read from the DB. It must have
	 *                                      at least one element. By convention, the
	 *                                      first attribute value for each element
	 *                                      is its primary key.
	 * @param startIndex                    The start index from where actual
	 *                                      attributes will be read from
	 *                                      {@code attributes}, inclusive.
	 * @param attributeNames                The attribute names. This array length
	 *                                      must be equal to
	 *                                      {@code attributes - startIndex}.
	 * @param target                        The web service endpoint to send the
	 *                                      POST HTTP request to.
	 * @param requestParametersAction       The action to execute to populate the
	 *                                      request object with parameters, after
	 *                                      the documents object.
	 * @param responseAttributeType         The expected type of the processed text
	 *                                      in the response.
	 * @param storeProcessedAttributeAction An action that receives the JSON
	 *                                      document object of the response, and is
	 *                                      expected to put its processed form in
	 *                                      the provided map.
	 * @return The described list. The values of this list and its maps are not
	 *         {@code null}. This list is not modifiable.
	 * @throws ProcessingException      If some exception occurs during the
	 *                                  operation.
	 * @throws IllegalArgumentException If some parameter is {@code null} or
	 *                                  invalid.
	 */
	public static List<Map<String, String>> processAttributes(
		@NonNull final List<String[]> attributesBatch, final int startIndex, @NonNull final String[] attributeNames, @NonNull final WebTarget target,
		@NonNull final ProcessingConsumer<? super JsonGenerator> requestParametersAction,
		@NonNull final JsonResponseAttributeType responseAttributeType,
		@NonNull final ProcessingBiConsumer<? super Entry<String, JsonObject>, ? super Map<String, String>> storeProcessedAttributeAction
	) throws ProcessingException {
		final List<Map<String, String>> batchAttributeValues;
		final List<Map<String, String>> batchAttributeValuesReadOnly;
		final int batchSize = attributesBatch.size();

		if (batchSize < 1) {
			throw new IllegalArgumentException("The attribute batch can't be empty");
		}

		if (attributesBatch.get(0).length - startIndex != attributeNames.length) {
			throw new IllegalArgumentException(
				"The length of the attribute values minus the start index must be equal to the attribute names array length"
			);
		}

		batchAttributeValues = new ArrayList<>(batchSize);

		// Prepare batch document attribute map
		for (final String[] documentAttributes : attributesBatch) {
			final Map<String, String> attributeValuesMap = new HashMap<>(
				(int) Math.ceil(attributeNames.length / 0.75)
			);

			// Prepare attribute map for the current document
			for (int i = 0; i < attributeNames.length; ++i) {
				attributeValuesMap.put(attributeNames[i], documentAttributes[i + startIndex]);
			}

			batchAttributeValues.add(attributeValuesMap);
		}

		batchAttributeValuesReadOnly = Collections.unmodifiableList(batchAttributeValues);

		try {
			final VariableHolder<Integer> attributeIndex = new VariableHolder<>(0);
			final VariableHolder<Integer> documentIndex = new VariableHolder<>(0);

			processProcessedResponseDocument(
				batchAttributeValuesReadOnly,
				target,
				requestParametersAction,
				responseAttributeType,
				(final Entry<String, JsonObject> returnedAttributeObject) -> {
					int currentDocumentIndex = documentIndex.getVariable();
					int currentAttributeIndex = attributeIndex.getVariable();

					final Map<String, String> currentDocumentAttributes = batchAttributeValues.get(currentDocumentIndex);
					// Reuse map for storing processed attribute values
					storeProcessedAttributeAction.accept(returnedAttributeObject, currentDocumentAttributes);

					// Move on to the next document if its attributes ended
					if (++currentAttributeIndex >= attributeNames.length) {
						// We have finished with this map, set it read only
						batchAttributeValues.set(currentDocumentIndex, Collections.unmodifiableMap(currentDocumentAttributes));

						++currentDocumentIndex;
						currentAttributeIndex = 0;
					}

					documentIndex.setVariable(currentDocumentIndex);
					attributeIndex.setVariable(currentAttributeIndex);
				}
			);
		} catch (final Exception exc) {
			if (!(exc instanceof ProcessingException)) {
				throw new ProcessingException(exc);
			} else {
				throw (ProcessingException) exc;
			}
		}

		return batchAttributeValuesReadOnly;
	}

	/**
	 * Processes the specified batch of document attributes, each element of the
	 * batch being pairs of attribute names and values, by sending a HTTP POST
	 * request to a JSON web service endpoint, that is assumed to be Text Processing
	 * Python compatible. The caller can inspect the results and do appropriate
	 * side-effects with them via the {@code processedAttributeConsumer}.
	 *
	 * @param attributesBatch                 The batch of attributes to process in
	 *                                        a single request with Text Processing
	 *                                        Python.
	 * @param target                          The Text Processing Python web service
	 *                                        endpoint method to invoke.
	 * @param requestParametersAction         An extension point for adding
	 *                                        parameters to the request body, after
	 *                                        the documents JSON object. The format
	 *                                        of these parameters is endpoint
	 *                                        specific.
	 * @param responseAttributeType           The expected type of the processed
	 *                                        text in the response, for each
	 *                                        document in the batch.
	 * @param processedResponseObjectConsumer Consumes each of the processed
	 *                                        document attribute response object,
	 *                                        doing the appropriate actions with it,
	 *                                        in the same order that they were in
	 *                                        the batch. The key of the entry it
	 *                                        receives is the name of the attribute.
	 * @throws IllegalArgumentException If some parameter is {@code null} or
	 *                                  invalid.
	 * @throws JsonException            If some exception occurs during JSON parsing
	 *                                  or generation.
	 * @throws ProcessingException      If some exception occurs while parsing the
	 *                                  server response.
	 * @throws WebApplicationException  If some exception occurs while parsing the
	 *                                  server response.
	 * @throws ProcessingException      If some other error occurs during the
	 *                                  processing.
	 */
	private static void processProcessedResponseDocument(
		@NonNull final List<Map<String, String>> attributesBatch, @NonNull final WebTarget target,
		@NonNull final ProcessingConsumer<? super JsonGenerator> requestParametersAction,
		@NonNull final JsonResponseAttributeType responseAttributeType,
		@NonNull final ProcessingConsumer<? super Entry<String, JsonObject>> processedResponseObjectConsumer
	) throws ProcessingException {
		final StringWriter requestJsonWriter = new StringWriter(8192);
		final Set<String> attributeNames = new HashSet<>();
		int documentNumber = 0;

		// Generate the request body
		try (final JsonGenerator requestJsonGenerator = Json.createGenerator(requestJsonWriter)) {
			requestJsonGenerator.writeStartObject().writeStartObject("documents");

			for (final Map<String, String> attributes : attributesBatch) {
				for (final Entry<String, String> attribute : attributes.entrySet()) {
					final String attributeName = attribute.getKey();

					requestJsonGenerator.writeStartObject(documentNumber + "_" + attributeName)
						.write("text", attribute.getValue())
					.writeEnd();

					// Save the attribute name for parsing the response later
					// (we assume that all the documents have the same attributes)
					attributeNames.add(attributeName);
				}

				++documentNumber;
			}

			requestJsonGenerator.writeEnd();
			requestParametersAction.accept(requestJsonGenerator);
			requestJsonGenerator.writeEnd();
		}

		// Send the request and retrieve the response as a JSON object
		try (
			final JsonReader responseJsonReader = Json.createReader(
				target
					.request(MediaType.APPLICATION_JSON_TYPE)
					// Identify ourselves via the User-Agent header
					.header("User-Agent", USER_AGENT_STRING)
					.post(Entity.json(requestJsonWriter.toString()), Reader.class)
			)
		) {
			final JsonObject processedDocuments = responseJsonReader.readObject().getJsonObject("documents");

			for (int i = 0; i < documentNumber; ++i) {
				for (final String attributeName : attributeNames) {
					final JsonObject processedAttribute = Objects.requireNonNullElse(
						processedDocuments.getJsonObject(i + "_" + attributeName),
						responseAttributeType.getDummyValue()
					);

					processedResponseObjectConsumer.accept(
						new SimpleImmutableEntry<>(attributeName, processedAttribute)
					);
				}
			}
		}
	}

	/**
	 * Utility class whose only purpose is to provide a final reference to an
	 * object, even if the wrapped object itself changes.
	 *
	 * @author Alejandro González García
	 *
	 * @param <T> The type of the variable to hold.
	 * @implNote The implementation of this class is not thread safe.
	 */
	@AllArgsConstructor
	private static final class VariableHolder<T> {
		@NonNull @Getter @Setter
		private T variable;
	}
}
