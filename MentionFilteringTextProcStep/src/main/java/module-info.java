// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that removes Reddit mentions from the input
 * documents.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.mentionfiltering {
	requires es.uvigo.esei.sing.textproc.step;

	requires java.persistence;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.mentionfiltering.xml.definition;

	// Entities are opened to deep reflection for JPA
	opens es.uvigo.esei.sing.textproc.step.mentionfiltering.entity;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.mentionfiltering.MentionFilteringProcessingStepService;
}
