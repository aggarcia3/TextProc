// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.abstracttppstep;

import java.util.function.BiConsumer;

import es.uvigo.esei.sing.textproc.step.ProcessingException;

/**
 * A consumer of data to be processed, which can throw a checked
 * {@link ProcessingException}.
 *
 * @author Alejandro González García
 *
 * @param <T> The type of the first input argument.
 * @param <U> The type of the second input argument.
 * @see BiConsumer
 */
@FunctionalInterface
public interface ProcessingBiConsumer<T, U> {
	/**
	 * Performs the processing operation on the given arguments.
	 *
	 * @param t The first input argument.
	 * @param u The second input argument.
	 */
	public void accept(final T t, final U u) throws ProcessingException;
}