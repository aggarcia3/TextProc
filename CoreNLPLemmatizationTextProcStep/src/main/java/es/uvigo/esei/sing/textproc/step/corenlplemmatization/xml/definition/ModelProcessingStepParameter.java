// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlplemmatization.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a POS tagging model to use with
 * {@link POSTaggerAnnotator}.
 *
 * @author Alejandro González García
 * @see <a href=
 *      "https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/tagger/maxent/MaxentTagger.html">https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/tagger/maxent/MaxentTagger.html</a>
 */
@XmlRootElement(name = "posModel")
public final class ModelProcessingStepParameter extends AbstractProcessingStepParameter {}
