// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.BaseModelURIProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CompactKnowledgeBaseProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceAnimateFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceCountriesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceDictionaryListProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceDictionaryPMIProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceGenderNumberFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceInanimateFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceMentionDetectionModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceSignaturesProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceStatesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceStatesProvincesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceWordCountsProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.DemonymsFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.DependencyParseModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.JenaDatasetFolderProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.KnowledgeBaseExportFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.NERMappingsFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.NERPropertiesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.OpenIEPropertiesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.POSModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.ParserModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.SentimentModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class CoreNLPKnowledgeBasePopulationProcessingStepService implements ProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new CoreNLPKnowledgeBasePopulationProcessingStep();
	}

	@Override
	public String getName() {
		return "CoreNLPKnowledgeBasePopulation";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of();
	}

	@Override
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		return Set.of(
			NERMappingsFileProcessingStepParameter.class, JenaDatasetFolderProcessingStepParameter.class,
			BaseModelURIProcessingStepParameter.class, POSModelProcessingStepParameter.class,
			DependencyParseModelProcessingStepParameter.class, ParserModelProcessingStepParameter.class,
			CoreferenceModelProcessingStepParameter.class, CoreferenceMentionDetectionModelProcessingStepParameter.class,
			DemonymsFileProcessingStepParameter.class, CoreferenceAnimateFileProcessingStepParameter.class,
			CoreferenceInanimateFileProcessingStepParameter.class, CoreferenceStatesFileProcessingStepParameter.class,
			CoreferenceGenderNumberFileProcessingStepParameter.class, CoreferenceCountriesFileProcessingStepParameter.class,
			CoreferenceStatesProvincesFileProcessingStepParameter.class, CoreferenceDictionaryListProcessingStepParameter.class,
			CoreferenceDictionaryPMIProcessingStepParameter.class, CoreferenceSignaturesProcessingStepParameter.class,
			CoreferenceWordCountsProcessingStepParameter.class, SentimentModelProcessingStepParameter.class,
			NERPropertiesFileProcessingStepParameter.class, OpenIEPropertiesFileProcessingStepParameter.class,
			KnowledgeBaseExportFileProcessingStepParameter.class, CompactKnowledgeBaseProcessingStepParameter.class
		);
	}
}
