// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Utility class whose only purpose is to provide a final reference to an
 * object, even if the wrapped object itself changes. The reference can be
 * {@code null}.
 *
 * @author Alejandro González García
 *
 * @param <T> The type of the variable to hold.
 * @implNote The implementation of this class is not thread safe.
 */
@AllArgsConstructor
public final class VariableHolder<T> {
	@Getter @Setter
	private T variable;
}
