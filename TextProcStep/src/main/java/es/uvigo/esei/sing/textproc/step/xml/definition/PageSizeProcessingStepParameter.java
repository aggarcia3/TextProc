// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameter for processing steps which represents the page size; that is,
 * the maximum number of documents that will be loaded to memory at a time.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "pageSize")
public final class PageSizeProcessingStepParameter extends AbstractProcessingStepParameter {}
