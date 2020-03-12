// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tpplemmatization.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a model to use for NLP operations.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "model")
public final class ModelProcessingStepParameter extends AbstractProcessingStepParameter {}
