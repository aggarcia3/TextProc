// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * 
 */
package es.uvigo.esei.sing.textproc.abstracttppstep;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import es.uvigo.esei.sing.textproc.abstracttppstep.xml.definition.EndpointProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Implements {@link #getAdditionalParameters()} so that additional parameters
 * common to every Text Processing Python processing step are returned without
 * involvement of the subclass.
 * <p>
 * Extending this class is the mandatory way to implement a
 * {@link ProcessingStepService} for concrete processing steps.
 *
 * @author Alejandro González García
 */
public abstract class AbstractTppProcessingStepService implements ProcessingStepService {
	@Override
	public final Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		final Set<Class<? extends AbstractProcessingStepParameter>> completeSet = new HashSet<>(
			getAdditionalParticularParameters()
		);

		completeSet.addAll(
			Set.of(
				EndpointProcessingStepParameter.class
			)
		);

		return Collections.unmodifiableSet(completeSet);
	}

	/**
	 * This method serves the same purpose and has the same preconditions and
	 * postconditions than {@link #getAdditionalParameters()}, but it is named
	 * different to avoid signature clashes and allow transparent common parameter
	 * injection to concrete services.
	 *
	 * @return The value of {@link #getAdditionalParameters()}.
	 * @see ProcessingStepService#getAdditionalParameters()
	 */
	protected abstract Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParticularParameters();
}
