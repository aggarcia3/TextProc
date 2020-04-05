// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the path to a folder where Apache Jena's TB2 storage
 * engine will store a unnamed dataset containing the knowledge base.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "jenaDatasetFolder")
public final class JenaDatasetFolderProcessingStepParameter extends AbstractProcessingStepParameter {}
