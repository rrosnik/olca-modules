package org.openlca.ipc.handlers;

import com.google.gson.JsonObject;
import org.openlca.core.services.JsonDataService;
import org.openlca.core.services.JsonResultService;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.dtos.LcaModelData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openlca.ipc.services.LcaModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LcaModelHandler {

	private static final Logger log = LoggerFactory.getLogger(LcaModelHandler.class);
	private final JsonDataService service;
	private final JsonResultService results;

	public LcaModelHandler(HandlerContext context) {
		this.results = context.results();
		this.service = new JsonDataService(context.db());
	}

	@Rpc("lcaModel/create")
	public RpcResponse createLcaModel(RpcRequest req) {
		try {
			var params = req.requireJsonObject();
			if (params.isError()) return Responses.badRequest("Invalid JSON object", req);

			ObjectMapper objectMapper = new ObjectMapper();
			LcaModelData lcaModelData = objectMapper.readValue(params.value().toString(), LcaModelData.class);

			LcaModelService lcaModelService = LcaModelService.of(service.db());
			try {
				lcaModelService.createLcaModel(lcaModelData);

				return Responses.ok("LCA model created successfully", req);

			} catch (Exception e) {
				lcaModelService.deleteLcaModel(lcaModelData);
				log.error("Error in deleting LCA Model | {}", e.getMessage());
				return Responses.serverError(e, req);
			}
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	@Rpc("lcaModel/delete")
	public RpcResponse deleteLCaModel(RpcRequest req) {
		try {
			var params = req.requireJsonObject();
			if (params.isError()) return Responses.badRequest("Invalid JSON object", req);
			ObjectMapper objectMapper = new ObjectMapper();
			LcaModelData lcaModelData = objectMapper.readValue(params.value().getAsJsonObject("lcaModel").toString(), LcaModelData.class);
			LcaModelService lcaModelService = LcaModelService.of(service.db());
			lcaModelService.deleteLcaModel(lcaModelData);
			return Responses.ok("LCA model deleted successfully", req);
		} catch (Exception e) {
			log.error("Error in deleting LCA Model | {}", e.getMessage());
			return Responses.serverError(e, req);
		}
	}


	@Rpc("lcaModel/calculate")
	public RpcResponse calculateLcaModel(RpcRequest req) {
		try {
			var params = req.requireJsonObject();
			if (params.isError()) return Responses.badRequest("Invalid JSON object", req);
			// creation of LCA model
			LcaModelService lcaModelService = LcaModelService.of(service.db());
			try {
				lcaModelService.createLcaModel(params.value().getAsJsonObject("lcaModel"));
			} catch (Exception e) {
				lcaModelService.deleteLcaModel(params.value().getAsJsonObject("lcaModel"));
				log.error("Error in Creating LCA Model | {}", e.getMessage());
				return Responses.serverError(e, req);
			}

			// calculation request
			var state = results.calculate(params.value().getAsJsonObject("calculationSetup"));
			if (state.isError()) {
				lcaModelService.deleteLcaModel(params.value().getAsJsonObject("lcaModel"));
				log.error("Error in calculation of LCA Model | {}", state.error());
				return Responses.serverError(new Exception("Server Error in calculation of Lca Model"), req);
			}
			return Responses.of(state, req);
		} catch (Exception e) {
			log.error("Error in calculating LCA Model | {}", e.getMessage());
			return Responses.serverError(e, req);
		}
	}

}
