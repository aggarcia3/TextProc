// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.process.xml.definition;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.ProcessingStepDefinition;
import lombok.Getter;

/**
 * Represents a processing process definition, which is mapped from XML.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "process")
public final class ProcessingProcessDefinition {
	@XmlElementRef(required = true) @Getter
	private List<ProcessingStepDefinition> processingSteps; // Never null or empty, at least one step
}
