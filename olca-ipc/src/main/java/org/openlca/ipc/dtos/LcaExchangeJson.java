package org.openlca.ipc.dtos;

import org.openlca.core.model.Exchange;

public class LcaExchangeJson {
    public Long id;
    public Integer internalId;
    public Long flowId;
    public Long defaultProviderId;
    public Boolean isInput;
    public Double amount;
    public Long unitId;
    public LcaFlowPropertyFactorJson fpf;
    public Long processId;


//    public Exchange toExchange(Map<Long, Flow> flowsMap, Map<Long, FlowProperty> flowPropertiesMap, Map<String, LcaFlowPropertyFactorJson> fpfsMap, Map<Long, Unit> unitsMap) {
//        Exchange exchange = new Exchange();
//        exchange.id = id;
//        exchange.internalId = internalId;
//        exchange.flow = flowsMap.get(flowId);
//        exchange.defaultProviderId = defaultProviderId;
//        exchange.isInput = isInput;
//        exchange.amount = amount;
//        exchange.unit = unitsMap.get(unitId);
//        var addedFpf = fpfsMap.get(flowId + "-" + fpf.fpId);
//        exchange.flowPropertyFactor = addedFpf.toFlowPropertyFactor(flowPropertiesMap);
//        return exchange;
//    }


    public static LcaExchangeJson from(Exchange exchange) {
        LcaExchangeJson dto = new LcaExchangeJson();
        dto.id = exchange.id;
        dto.internalId = exchange.internalId;
        dto.flowId = exchange.flow.id;
        dto.defaultProviderId = exchange.defaultProviderId;
        dto.isInput = exchange.isInput;
        dto.amount = exchange.amount;
        dto.unitId = exchange.unit.id;
        dto.fpf = LcaFlowPropertyFactorJson.from(exchange.flowPropertyFactor, exchange.flow.id);
        return dto;
    }
}
