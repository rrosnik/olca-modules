package org.openlca.ipc.dtos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LcaExchangeJson {
    public Long id;
    public Integer internalId;
    public Long flowId;
		public FlowType flowType;
    public Long defaultProviderId;
    public Boolean isInput;
    public Double amount;
    public Long unitId;
    public LcaFlowPropertyFactorJson fpf;
    public Long processId;

		public CalcExchange toCalcExchange() {
			var result = new CalcExchange();
			result.exchangeId = this.id;
			result.amount = this.amount;
			result.defaultProviderId = this.defaultProviderId;
			result.conversionFactor = fpf.conversionFactor;
			result.flowId = this.flowId;
			result.flowType = this.flowType;
			result.isInput = this.isInput;
			result.processId = this.processId;
			return result;
		}

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
				dto.flowType = exchange.flow.flowType;
        return dto;
    }
}
