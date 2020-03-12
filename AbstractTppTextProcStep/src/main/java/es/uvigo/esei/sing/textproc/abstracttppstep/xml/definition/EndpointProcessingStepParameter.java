// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.abstracttppstep.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for processing steps which represents a web service endpoint.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "endpoint")
public final class EndpointProcessingStepParameter extends AbstractProcessingStepParameter {}
