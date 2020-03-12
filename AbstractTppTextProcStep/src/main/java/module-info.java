// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * This module provides a skeletal implementation of a TextProc processing step
 * in the form of an abstract class, to keep code DRY and consistent between
 * steps.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.abstracttppstep {
	requires transitive es.uvigo.esei.sing.textproc.step;

	requires lombok;
	requires transitive java.json;
	requires java.ws.rs;
	requires transitive java.persistence;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.abstracttppstep.xml.definition;

	// Dependency apparently needed by Eclipse to
	// import Entry from the Map interface without complaining.
	// What?
	requires java.base;

	exports es.uvigo.esei.sing.textproc.abstracttppstep;
}
