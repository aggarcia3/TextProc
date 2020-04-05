// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.SentimentAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a sentiment model to use with
 * {@link SentimentAnnotator}.
 *
 * @author Alejandro González García
 * @see <a href=
 *      "https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/pipeline/SentimentAnnotator.html">https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/pipeline/SentimentAnnotator.html</a>
 */
@XmlRootElement(name = "sentimentModel")
public final class SentimentModelProcessingStepParameter extends AbstractProcessingStepParameter {}
