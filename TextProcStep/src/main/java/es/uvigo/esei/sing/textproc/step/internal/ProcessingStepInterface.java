// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.internal;

import java.util.Map;

import es.uvigo.esei.sing.textproc.step.ProcessingException;

/**
 * Interface that defines the contract common to every processing step. This
 * provides indirection, should the abstract classes in this module that
 * implement this interface change.
 *
 * @author Alejandro González García
 */
public interface ProcessingStepInterface {
	/**
	 * Executes the processing step implemented by this object, with the given
	 * parameters. No guarantees are made about whether a JPA transaction is already
	 * opened when this method is invoked.
	 *
	 * @param parameters An unmodifiable, non-null map of non-null keys which
	 *                   contains all parameters specified by the user. The values
	 *                   may be null or not be appropriate for this step.
	 * @throws ProcessingException If an exception occurs during execution
	 *                             (including if {@code parameters} is
	 *                             {@code null}).
	 */
	public void execute(final Map<String, String> parameters) throws ProcessingException;
}
