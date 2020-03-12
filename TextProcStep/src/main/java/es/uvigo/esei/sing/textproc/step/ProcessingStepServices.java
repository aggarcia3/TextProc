// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step;

import java.util.ServiceLoader;

/**
 * Contains common functionality for available processing step services.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is thread-safe.
 */
public final class ProcessingStepServices {
	private static final ThreadLocal<ServiceLoader<ProcessingStepService>> CACHED_SERVICE_LOADERS;

	static {
		CACHED_SERVICE_LOADERS = new ThreadLocal<>() {
			@Override
			protected ServiceLoader<ProcessingStepService> initialValue() {
				return ServiceLoader.load(ProcessingStepService.class);
			}
		};
	}

	/**
	 * Returns a service loader for iterating over every available processing step
	 * service. This method is efficient, in the sense that it avoids creating new
	 * service loader objects if it's not necessary.
	 *
	 * @return The described service loader.
	 */
	public static ServiceLoader<ProcessingStepService> getServiceLoader() {
		return CACHED_SERVICE_LOADERS.get();
	}
}
