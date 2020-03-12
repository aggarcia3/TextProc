// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The TextProc processing step API, including unprocessed document types, step
 * interfaces and abstract classes, and process definition document handling
 * logic. This module is of interest for those willing to extend TextProc with
 * more processing capabilities.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step {
	requires es.uvigo.esei.sing.textproc.logging;
	requires es.uvigo.esei.sing.textproc.persistence;

	requires lombok;
	requires transitive java.xml.bind;

	// FIXME Required libraries that do not define a module name
	// (automatic modules). Nag their authors for proper JPMS
	// support
	requires progressbar;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.process.xml.definition;
	opens es.uvigo.esei.sing.textproc.step.xml.definition;

	// Entities are opened to deep reflection for JPA
	opens es.uvigo.esei.sing.textproc.entity;

	exports es.uvigo.esei.sing.textproc.entity;
	exports es.uvigo.esei.sing.textproc.process;
	exports es.uvigo.esei.sing.textproc.step;
	exports es.uvigo.esei.sing.textproc.step.xml.definition;

	uses es.uvigo.esei.sing.textproc.step.ProcessingStepService;
}
