package org.openlca.ipc.dtos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;

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


//    public Process toProcess(Map<Long, Category> categoriesMap, Map<Long, Flow> flowsMap, Map<Long, FlowProperty> flowPropertiesMap, Map<String, LcaFlowPropertyFactorJson> fpfsMap, Map<Long, Unit> unitsMap) {
//        Process process = new Process();
//        process.id = id;
//        process.refId = refId;
//        process.name = name;
//        process.description = description;
//        process.processType = processType;
//        process.category = categoriesMap.get(category);
//        exchanges.forEach(e -> {
//            var exchange = e.toExchange(flowsMap, flowPropertiesMap, fpfsMap, unitsMap);
//            process.exchanges.add(exchange);
//            if (!e.isInput)
//                process.quantitativeReference = exchange;
//        });
//        return process;
//    }


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
