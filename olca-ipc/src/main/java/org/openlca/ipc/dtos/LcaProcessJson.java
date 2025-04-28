package org.openlca.ipc.dtos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import java.util.List;


@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LcaProcessJson {
	public Long id;
	public String refId;
	public String name;
	public String description;
	public Long category;
	public ProcessType processType;
	public Integer lastInternalId;
	public LcaExchangeJson quantitativeReference;

	public List<LcaExchangeJson> exchanges;


	public ProcessDescriptor toDescriptor() {
		var result = new ProcessDescriptor();
		result.id = this.id;
		result.refId = this.refId;
		result.name = this.name;
		result.category = this.category;
		result.processType = this.processType;
		result.flowType = quantitativeReference.flowType;
		result.type = ModelType.PROCESS;
		return result;
	}


	public static LcaProcessJson from(Process process) {
		LcaProcessJson dto = new LcaProcessJson();
		dto.id = process.id;
		dto.refId = process.refId;
		dto.name = process.name;
		dto.description = process.description;
		dto.category = process.category == null ? null : process.category.id;
		dto.quantitativeReference = LcaExchangeJson.from(process.quantitativeReference);
		dto.processType = process.processType;
		dto.lastInternalId = process.lastInternalId;
		dto.exchanges = process.exchanges.stream().map(LcaExchangeJson::from).toList();
		return dto;
	}
}
