// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameter for processing steps which represents the number of documents
 * within a page that will be batched for processing at once.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "documentsPerBatch")
public final class DocumentBatchSizeProcessingStepParameter extends AbstractProcessingStepParameter {}
