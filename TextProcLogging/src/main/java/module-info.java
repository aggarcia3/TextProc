// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The TextProc logging facilities, that rely on the {@code java.logging}
 * package.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.logging {
	requires transitive java.logging;
	requires lombok;

	exports es.uvigo.esei.sing.textproc.logging;
}
