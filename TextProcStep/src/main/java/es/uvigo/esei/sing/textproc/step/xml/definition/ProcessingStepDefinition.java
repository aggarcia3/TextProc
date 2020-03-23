// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.xml.definition;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader.Provider;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.ProcessingStepServices;
import es.uvigo.esei.sing.textproc.step.internal.ProcessingStepInterface;
import lombok.Getter;
import lombok.NonNull;

/**
 * Models a processing step definition, suitable for marshalling and
 * unmarshalling using JAXB.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "step")
@XmlSeeAlso({
	// Common step parameter definitions
	PageSizeProcessingStepParameter.class,
	BatchSizeProcessingStepParameter.class,
	TextDocumentWithTitleTableNameProcessingStepParameter.class,
	TextDocumentTableNameProcessingStepParameter.class,
	PrimaryKeyColumnProcessingStepParameter.class,
	TextColumnProcessingStepParameter.class,
	TitleColumnProcessingStepParameter.class
	// Other step parameter definitions TBD by
	// step service providers, and added to JAXB
	// context at runtime
})
public final class ProcessingStepDefinition {
	private static final String INVALID_STEP_MARSHAL_EXC_MESSAGE = "The specified step is not valid or accessible. Is it in the classpath?";

	@XmlAttribute @XmlJavaTypeAdapter(ProcessingStepAdapter.class) @Getter
	private ProcessingStepInterface action; // Never null due to how the adapter works
	@XmlElementWrapper @XmlAnyElement(lax = true)
	private List<ProcessingStepParameter> parameters; // Can be null if there are no parameters
	@XmlTransient
	private boolean parametersRead = false;

	/**
	 * Retrieves the list of parameters given by the user for this step.
	 *
	 * @return The described list.
	 */
	public final List<ProcessingStepParameter> getParameters() {
		if (!parametersRead) {
			if (parameters == null) {
				parameters = Collections.emptyList();
			} else {
				// Ignore parameters that couldn't be resolved to an object
				parameters.removeIf(
					(final Object element) -> !(element instanceof ProcessingStepParameter)
				);

				parameters = Collections.unmodifiableList(parameters);
			}
		}

		parametersRead = true;
		return parameters;
	}

	/**
	 * Converts from a XML string to a processing step class for the purposes of
	 * JAXB, and vice versa.
	 *
	 * @author Alejandro González García
	 */
	private static final class ProcessingStepAdapter extends XmlAdapter<String, ProcessingStepInterface> {
		@Override
		public ProcessingStepInterface unmarshal(@NonNull final String v) throws Exception {
			final ProcessingStepService stepService = ProcessingStepServices.getServiceLoader().stream()
				.filter((final Provider<ProcessingStepService> stepServiceProvider) ->
					stepServiceProvider.get().getName().equals(v)
				).findFirst().orElseThrow(
					() -> new IllegalArgumentException(INVALID_STEP_MARSHAL_EXC_MESSAGE)
				).get();

			return stepService.create();
		}

		@Override
		public String marshal(@NonNull final ProcessingStepInterface v) throws Exception {
			final Class<? extends ProcessingStepInterface> clazz = v.getClass();
			final int clazzModifiers = clazz.getModifiers();

			if (Modifier.isAbstract(clazzModifiers) || Modifier.isInterface(clazzModifiers)) {
				throw new IllegalArgumentException(INVALID_STEP_MARSHAL_EXC_MESSAGE);
			}

			return clazz.getSimpleName();
		}
	}
}
