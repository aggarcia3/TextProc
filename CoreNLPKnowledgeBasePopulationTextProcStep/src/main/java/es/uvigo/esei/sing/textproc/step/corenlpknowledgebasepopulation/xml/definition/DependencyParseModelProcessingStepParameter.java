// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a dependency parser model to use with
 * {@link DependencyParseAnnotator}.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "dependencyParseModel")
public final class DependencyParseModelProcessingStepParameter extends AbstractProcessingStepParameter {}
