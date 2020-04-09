// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying whether the resulting Apache Jena dataset should be
 * compacted after all write operations on it are done. This helps reducing its
 * storage requirements dramatically.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "compactKnowledgeBase")
public final class CompactKnowledgeBaseProcessingStepParameter extends AbstractProcessingStepParameter {}
