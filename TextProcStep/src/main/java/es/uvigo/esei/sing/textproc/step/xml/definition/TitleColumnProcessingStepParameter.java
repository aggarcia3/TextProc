// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameter that specifies the title column name of a document entity in a
 * relational database.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "titleColumn")
public final class TitleColumnProcessingStepParameter extends AbstractProcessingStepParameter {}
