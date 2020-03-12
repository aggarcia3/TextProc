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
}
