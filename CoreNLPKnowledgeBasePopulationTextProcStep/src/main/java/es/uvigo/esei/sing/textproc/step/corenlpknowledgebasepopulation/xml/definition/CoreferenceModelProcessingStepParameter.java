// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.coref.fastneural.FastNeuralCorefAlgorithm;
import edu.stanford.nlp.pipeline.CorefAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a coreference model to use with
 * {@link CorefAnnotator}.
 * <p>
 * It is assumed that the model contained in the file is a fastneural model. For
 * more information about the underlying coreference resolution algorithm, see
 * {@link FastNeuralCorefAlgorithm}.
 * </p>
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "coreferenceModel")
public final class CoreferenceModelProcessingStepParameter extends AbstractProcessingStepParameter {}
