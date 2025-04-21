package org.openlca.ipc.dtos;


import org.openlca.core.model.FlowPropertyFactor;

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
