// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

/**
 * Represents a read-only parameter passed on to a processing step.
 * Subclasses must be annotated with a unique JAXB XmlRootElement name, and
 * configure their XML binding appropriately.
 *
 * @author Alejandro González García
 * @see ProcessingStepDefinition
 */
public interface ProcessingStepParameter {
	/**
	 * Retrieves the name of the processing step parameter.
	 *
	 * @return The name of the processing step parameter. It never is
	 *         {@code null}.
	 */
	public String getName();

	/**
	 * Returns the value of the processing step parameter.
	 *
	 * @return The value of the processing step parameter. It never is
	 *         {@code null}.
	 */
	public String getValue();
}
