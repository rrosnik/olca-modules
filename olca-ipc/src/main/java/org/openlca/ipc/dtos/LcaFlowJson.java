package org.openlca.ipc.dtos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LcaFlowJson {
	public long id;
	public String refId;
	public String name;
	public String description;
	public FlowType flowType;
	public boolean infrastructureFlow;
	public long rfpId;
	public List<LcaFlowPropertyFactorJson> fpfs;
	public boolean isEcoinventFlow;
	public Long category;


	public FlowDescriptor toDescriptor() {
		var result = new FlowDescriptor();
		result.id = id;
		result.refId = refId;
		result.name = name;
		result.flowType = flowType;
		result.refFlowPropertyId = rfpId;
		result.category = category;
		return result;
	}


	public static LcaFlowJson from(Flow flow) {
		LcaFlowJson dto = new LcaFlowJson();
		dto.id = flow.id;
		dto.refId = flow.refId;
		dto.name = flow.name;
		dto.description = flow.description;
		dto.flowType = flow.flowType;
		dto.infrastructureFlow = flow.infrastructureFlow;
		dto.rfpId = flow.referenceFlowProperty.id;
		dto.fpfs = flow.flowPropertyFactors.stream().map(fpf -> LcaFlowPropertyFactorJson.from(fpf, flow.id)).toList();
		if (flow.category != null) dto.category = flow.category.id;
		return dto;
	}
}
