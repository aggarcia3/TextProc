// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameter for processing steps which represents a text document entity
 * table name.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "textDocumentTableName")
public final class TextDocumentTableNameProcessingStepParameter extends AbstractProcessingStepParameter {}
