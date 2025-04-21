package org.openlca.ipc.dtos;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openlca.core.model.FlowPropertyFactor;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LcaFlowPropertyFactorJson {
    public Long id;
    public Long fpId;
    public Double conversionFactor;
    public Long flowId;

//    public FlowPropertyFactor toFlowPropertyFactor(Map<Long, FlowProperty> flowPropertiesMap) {
//        FlowPropertyFactor fpf = new FlowPropertyFactor();
//        fpf.id = id;
//        fpf.conversionFactor = conversionFactor;
//        fpf.flowProperty = flowPropertiesMap.get(fpId);
//        return fpf;
//    }

    public static LcaFlowPropertyFactorJson from(FlowPropertyFactor fpf, Long flowId) {
        LcaFlowPropertyFactorJson dto = new LcaFlowPropertyFactorJson();
        dto.id = fpf.id;
        dto.conversionFactor = fpf.conversionFactor;
        dto.fpId = fpf.flowProperty.id;
        dto.flowId = flowId;
        return dto;
    }
}
