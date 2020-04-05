// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the base URI to use for the knowledge base model,
 * entities, relations and, in general, every piece of data in a model that
 * should be identified by a URI.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "baseModelURI")
public final class BaseModelURIProcessingStepParameter extends AbstractProcessingStepParameter {}
