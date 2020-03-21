// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpentityextraction;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.NERMappingsFileStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.OverwritableNERCategoriesStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.PropertiesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition.SeedWordsDirectoryProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class CoreNLPEntityExtractionProcessingStepService implements ProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new CoreNLPEntityExtractionProcessingStep();
	}

	@Override
	public String getName() {
		return "CoreNLPEntityExtraction";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of();
	}

	@Override
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		return Set.of(
			SeedWordsDirectoryProcessingStepParameter.class, PropertiesFileProcessingStepParameter.class,
			NERMappingsFileStepParameter.class, OverwritableNERCategoriesStepParameter.class
		);
	}
}
