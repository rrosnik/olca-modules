package org.openlca.ipc.dtos;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;

import java.util.List;

public class LcaFlowJson {
    public long id;
    public String refId;
    public String name;
    public String description;
    public FlowType flowType;
    public boolean infrastructureFlow;
    public long rfpId;
    public List<LcaFlowPropertyFactorJson> fpfs;
    public boolean isEcoinventFlow = true;
    public Long category;


//    public Flow toFlow(Map<Long, FlowProperty> flowPropertiesMap, Map<Long, Category> categoriesMap, Map<String, LcaFlowPropertyFactorJson> fpfsMap) {
//        Flow flow = new Flow();
//        flow.id = id;
//        flow.refId = refId;
//        flow.name = name;
//        flow.description = description;
//        flow.flowType = flowType;
//        flow.infrastructureFlow = infrastructureFlow;
//        flow.referenceFlowProperty = flowPropertiesMap.get(rfpId);
//        fpfs.forEach(fpf -> {
//            var addedFpf = fpfsMap.get(fpf.flowId + "-" + fpf.fpId);
//            flow.flowPropertyFactors.add(addedFpf.toFlowPropertyFactor(flowPropertiesMap));
//        });
//        if (category != null) {
//            var cat = categoriesMap.get(category);
//            if (cat == null)
//                throw new RuntimeException("Category not found: " + category);
//            flow.category = cat;
//        }
//        return flow;
//    }


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
