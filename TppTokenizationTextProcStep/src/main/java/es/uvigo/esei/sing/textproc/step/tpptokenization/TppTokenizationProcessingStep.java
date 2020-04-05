// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tpptokenization;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonString;

import es.uvigo.esei.sing.textproc.abstracttppstep.AbstractTppProcessingStep;
import es.uvigo.esei.sing.textproc.abstracttppstep.JsonResponseAttributeType;
import es.uvigo.esei.sing.textproc.abstracttppstep.ProcessingBiConsumer;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.tpptokenization.entity.TppTokenizedTextDocument;
import es.uvigo.esei.sing.textproc.step.tpptokenization.entity.TppTokenizedTextWithTitleDocument;

/**
 * Tokenizes documents, according to the provided parameters, using Text
 * Processing Python.
 * <p>
 * Example declaration for this step in a process definition file:
 * </p>
 * <pre>
 * {@code <step action="TppTokenization">
 * 	<parameters>
 * 		<tpp:endpoint>http://127.0.0.1:5005/tpp/v1/casual-tokenize</tpp:endpoint>
 * 		<textDocumentWithTitleTableName>submission</textDocumentWithTitleTableName>
 * 		<textDocumentTableName>comment</textDocumentTableName>
 * 	</parameters>
 * </step>}
 * </pre>
 *
 * @author Alejandro González García
 */
final class TppTokenizationProcessingStep extends AbstractTppProcessingStep {
	/**
	 * Instantiates a Text Processing Python tokenization processing step.
	 */
	TppTokenizationProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(),
			// Additional mandatory parameters
			Set.of(),
			// Result entity types
			List.of(TppTokenizedTextWithTitleDocument.class, TppTokenizedTextDocument.class),
			// Step description format string
			"Tokenizing %s",
			// JSON response attribute type
			JsonResponseAttributeType.STRING_ARRAY,
			// Request parameters action
			new NullProcessingConsumer<>(),
			// Attribute store action
			new TokenStoreAction()
		);
	}

	/**
	 * Parses a tokenized document attribute from the corresponding Text Processing
	 * Python JSON response object.
	 *
	 * @author Alejandro González García
	 */
	private static final class TokenStoreAction implements ProcessingBiConsumer<Entry<String, JsonObject>, Map<String, String>> {
		@Override
		public void accept(
			final Entry<String, JsonObject> returnedAttributeObject, final Map<String, String> processedAttributesMap
		) throws ProcessingException {
			try {
				final StringBuilder tokenizedAttributeBuilder = new StringBuilder();

				final List<JsonString> attributeTokens = returnedAttributeObject.getValue()
					.getJsonArray("text").getValuesAs(JsonString.class);

				for (final JsonString token : attributeTokens) {
					tokenizedAttributeBuilder.append(token.getString());
					tokenizedAttributeBuilder.append(' ');
				}

				// Delete extra space at the end if it was added
				final int tokenizedAttributeLength = tokenizedAttributeBuilder.length();
				if (tokenizedAttributeLength > 0) {
					tokenizedAttributeBuilder.deleteCharAt(tokenizedAttributeLength - 1);
				}

				processedAttributesMap.put(returnedAttributeObject.getKey(), tokenizedAttributeBuilder.toString());
			} catch (final Exception exc) {
				throw new ProcessingException(
					"An exception occurred while processing the tokenized document", exc
				);
			}
		}
	}
}
