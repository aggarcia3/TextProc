// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.pipeline.CorefAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the signatures that will be read by
 * {@link Dictionaries}, which is a transitive dependency of
 * {@link CorefAnnotator}.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "coreferenceSignatures")
public final class CoreferenceSignaturesProcessingStepParameter extends AbstractProcessingStepParameter {}
