// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.ParserAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a POS tagging model to use with
 * {@link ParserAnnotator}.
 *
 * @author Alejandro González García
 * @see <a href=
 *      "https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/pipeline/ParserAnnotator.html">https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/pipeline/ParserAnnotator.html</a>
 */
@XmlRootElement(name = "parserModel")
public final class ParserModelProcessingStepParameter extends AbstractProcessingStepParameter {}
