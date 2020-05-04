// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The TextProc main and entity classes module.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.main {
	requires es.uvigo.esei.sing.textproc.logging;
	requires es.uvigo.esei.sing.textproc.persistence;
	requires es.uvigo.esei.sing.textproc.step;

	requires lombok;

	// FIXME Required libraries that do not define a module name
	// (automatic modules). Nag their authors for proper JPMS
	// support
	requires jfiglet;

	exports es.uvigo.esei.sing.textproc;

	uses es.uvigo.esei.sing.textproc.step.ProcessingStepService;

	// Bundled processing steps (needed for Javadoc generation)
	requires es.uvigo.esei.sing.textproc.step.tpptokenization;
	requires es.uvigo.esei.sing.textproc.step.corenlptokenization;
	requires es.uvigo.esei.sing.textproc.step.tppstopwordfiltering;
	requires es.uvigo.esei.sing.textproc.step.tpplemmatization;
	requires es.uvigo.esei.sing.textproc.step.corenlplemmatization;
	requires es.uvigo.esei.sing.textproc.step.mentionfiltering;
	requires es.uvigo.esei.sing.textproc.step.emptyfiltering;
	requires es.uvigo.esei.sing.textproc.step.luceneindex;
	requires es.uvigo.esei.sing.textproc.step.corenlpentityextraction;
	requires es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation;
}
