// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlptokenization.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying a language code.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "language")
public final class LanguageProcessingStepParameter extends AbstractProcessingStepParameter {}
