// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.emptyfiltering.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter which represents whether to take into account deleted posts in
 * Reddit for the step.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "includeRedditDeleted")
public final class IncludeRedditDeletedProcessingStepParameter extends AbstractProcessingStepParameter {}
