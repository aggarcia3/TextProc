// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The TextProc persistence access layer, using Java Persistence API.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.persistence {
	requires es.uvigo.esei.sing.textproc.logging;

	requires transitive java.persistence;
	requires transitive java.sql;
	requires org.hibernate.orm.core;
	requires lombok;

	exports es.uvigo.esei.sing.textproc.persistence;
}
