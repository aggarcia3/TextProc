// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Contains common logic to all processing step parameters, keeping their code
 * DRY.
 *
 * @author Alejandro González García
 * @see ProcessingStepParameter
 */
public abstract class AbstractProcessingStepParameter implements ProcessingStepParameter {
	@XmlValue
	private String value;

	@Override
	public final String getValue() {
		return value;
	}

	@Override
	public final String getName() {
		try {
			return getClass().getDeclaredAnnotation(XmlRootElement.class).name();
		} catch (final NullPointerException exc) {
			throw new AssertionError("Missing XmlRootElement annotation");
		}
	}

	/**
	 * Converts the value of a parameter to a boolean value. Currently, the value is
	 * assumed to be {@code true} if and only if it equals the strings "true" or
	 * "1", ignoring case differences. Therefore, a {@code null} value is treated as
	 * {@code false}.
	 *
	 * @param value The value for the processing step parameter.
	 * @return The value of the processing step parameter, converted to a boolean
	 *         value.
	 */
	public static boolean convertValueToBoolean(final String value) {
		return "true".equalsIgnoreCase(value) || "1".equals(value);
	}
}
