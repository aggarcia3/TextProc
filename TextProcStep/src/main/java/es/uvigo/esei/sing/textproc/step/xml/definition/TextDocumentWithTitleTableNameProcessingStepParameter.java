// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameter for processing steps which represents a text document with title
 * entity table name.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "textDocumentWithTitleTableName")
public final class TextDocumentWithTitleTableNameProcessingStepParameter extends AbstractProcessingStepParameter {}
