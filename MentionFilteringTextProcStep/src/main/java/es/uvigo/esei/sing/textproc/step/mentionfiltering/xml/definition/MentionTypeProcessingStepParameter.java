// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.mentionfiltering.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for processing steps which represents a type of social media
 * mentions.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "mentionType")
public final class MentionTypeProcessingStepParameter extends AbstractProcessingStepParameter {}
