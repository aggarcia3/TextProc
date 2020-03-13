// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlptokenization.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a options string for the CoreNLP
 * {@link TokenizerAnnotator}.
 *
 * @author Alejandro González García
 * @see <a href="https://nlp.stanford.edu/software/tokenizer.html#Options">https://nlp.stanford.edu/software/tokenizer.html#Options</a>
 */
@XmlRootElement(name = "tokenizerOptions")
public final class TokenizerOptionsProcessingStepParameter extends AbstractProcessingStepParameter {}
