// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.coref.md.DependencyCorefMentionFinder;
import edu.stanford.nlp.pipeline.CorefAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a coreference mention model to use with
 * {@link DependencyCorefMentionFinder}, whose methods are called by
 * {@link CorefAnnotator}. It is assumed that it doesn't use constituency parse
 * (i.e. {@code coref.useConstituencyParse} is {@code false}).
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "coreferenceMentionDetectionModel")
public final class CoreferenceMentionDetectionModelProcessingStepParameter extends AbstractProcessingStepParameter {}
