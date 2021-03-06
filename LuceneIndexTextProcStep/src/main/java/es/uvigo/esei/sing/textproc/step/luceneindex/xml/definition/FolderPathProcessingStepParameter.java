// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.luceneindex.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a folder path.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "folderPath")
public final class FolderPathProcessingStepParameter extends AbstractProcessingStepParameter {}
