// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.riot.Lang;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the path of the file that a knowledge base will be
 * exported to, along with its format.
 * <p>
 * For the available export formats, see the Apache Jena {@link Lang} class, and
 * the Apache Jena documentation in general.
 * </p>
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "knowledgeBaseExportFile")
public final class KnowledgeBaseExportFileProcessingStepParameter extends AbstractProcessingStepParameter {
	private static final char TOKEN_SEPARATOR = ',';
	private static final int NUMBER_OF_TOKENS = 2;

	private static final String TOKEN_SEPARATOR_REGEX = Pattern.quote(
		String.valueOf(TOKEN_SEPARATOR)
	);

	/**
	 * Checks whether the specified value is valid for this processing step
	 * parameter.
	 *
	 * @param value The value to check.
	 * @return True if and only if the value is valid, false otherwise.
	 */
	public static boolean isValid(final String value) {
		boolean valid = value != null;

		if (valid) {
			final String strippedValue = value.strip();
			valid = !strippedValue.isEmpty();

			if (valid) {
				final String[] splittedValue = strippedValue.split(TOKEN_SEPARATOR_REGEX, NUMBER_OF_TOKENS);
				valid = splittedValue.length == NUMBER_OF_TOKENS;

				for (int i = 0; i < NUMBER_OF_TOKENS && valid; ++i) {
					valid = !splittedValue[i].isBlank();
				}
			}
		}

		return valid;
	}

	/**
	 * Retrieves the path component of a valid value for this processing step
	 * parameter.
	 *
	 * @param value The value for this processing step parameter.
	 * @return The path component of this value, that represents the path to the
	 *         exported knowledge base.
	 * @throws IllegalArgumentException If {@code value} is not valid.
	 */
	public static String getPathFromValue(final String value) {
		if (!isValid(value)) {
			throw new IllegalArgumentException("The value is not valid");
		}

		return getValueToken(value, 0);
	}

	/**
	 * Retrieves the format component of a valid value for this processing step
	 * parameter.
	 *
	 * @param value The value for this processing step parameter.
	 * @return The path component of this value, that represents the format of the
	 *         exported knowledge base file. For available formats, see the Apache
	 *         Jena {@link Lang} class.
	 * @throws IllegalArgumentException If {@code value} is not valid.
	 */
	public static String getFormatFromValue(final String value) {
		if (!isValid(value)) {
			throw new IllegalArgumentException("The value is not valid");
		}

		return getValueToken(value, 1);
	}

	/**
	 * Returns a token of a value that is in the specified zero-based position, so
	 * the first token has index 0, the second 1, and so on. The value is assumed to
	 * be valid, as per {@link #isValid(String)}.
	 *
	 * @param value The value that contains the token to get.
	 * @param index The index of the token.
	 * @return The non-null value of the token.
	 * @throws IllegalArgumentException If the index is invalid.
	 */
	private static String getValueToken(
		final String value, final int index
	) {
		return getValueToken(value, index, NUMBER_OF_TOKENS);
	}

	/**
	 * Returns a token of a value that is in the specified zero-based position, so
	 * the first token has index 0, the second 1, and so on. The value is assumed to
	 * be valid, as per {@link #isValid(String)}.
	 *
	 * @param value     The value that contains the token to get.
	 * @param index     The index of the token.
	 * @param maxTokens The maximum number of tokens that the value will be split
	 *                  to.
	 * @return The non-null value of the token.
	 * @throws IllegalArgumentException If the index is invalid.
	 */
	private static String getValueToken(
		final String value, final int index, final int maxTokens
	) {
		if (index < 0 || index >= NUMBER_OF_TOKENS) {
			throw new IllegalArgumentException("The index points to an inexistent token");
		}

		return value.strip().split(TOKEN_SEPARATOR_REGEX, maxTokens)[index];
	}
}
