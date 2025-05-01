package org.openlca.ipc.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.openlca.core.services.JsonDataService;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.dtos.LcaModelData;
import org.openlca.ipc.dtos.LcaProcessJson;
import org.openlca.ipc.services.LcaModelService;
import org.openlca.ipc.services.ProcessService;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LcaProcessHandler {

	private static final Logger log = LoggerFactory.getLogger(LcaProcessHandler.class);
	private final JsonDataService service;

	public LcaProcessHandler(HandlerContext context) {
		this.service = new JsonDataService(context.db());
	}

	@Rpc("data/processes/byRefIds")
	public RpcResponse byRefIds(RpcRequest req) {
		try {
			var params = req.requireJsonObject();
			if (params.isError()) return Responses.badRequest("Invalid JSON object", req);
			var refIds = params.value().getAsJsonArray("refIds");
			if (refIds == null || refIds.isEmpty()) return Responses.badRequest("No refIds provided", req);
			var refIdsList = new ArrayList<String>();
			for (int i = 0; i < refIds.size(); i++) refIdsList.add(refIds.get(i).getAsString());
			ProcessService processService = ProcessService.of(service.db());
			var processes = processService.search("", 1, 10, refIdsList);
			Gson gson = new Gson();
			JsonArray jsonArray = new JsonArray();

			for (LcaProcessJson process : processes) {
				jsonArray.add(gson.toJsonTree(process));
			}
			return Responses.ok(jsonArray, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	@Rpc("data/processes/search")
	public RpcResponse search(RpcRequest req) {
		try {

			var params = req.requireJsonObject();
			if (params.isError()) return Responses.badRequest("Invalid JSON object", req);
			String searchTerm = params.value().get("searchTerm").getAsString();
			Integer page = params.value().get("page").getAsInt();
			Integer pageSize = params.value().get("pageSize").getAsInt();
			JsonArray refIdsJsonArray = params.value()
					.getAsJsonArray("refIds");

			List<String> refIds = new ArrayList<>();

			if (refIdsJsonArray != null && !refIdsJsonArray.isEmpty())
				refIds = refIdsJsonArray.asList().stream()
						.filter(JsonElement::isJsonPrimitive)
						.map(JsonElement::getAsString)
						.collect(Collectors.toList());
			ProcessService processService = ProcessService.of(service.db());
			System.out.println("refIds: " + refIds);
			var searchResult = processService.search(searchTerm, page, pageSize, refIds);
			Gson gson = new Gson();
			JsonArray jsonArray = new JsonArray();
			for (LcaProcessJson process : searchResult) jsonArray.add(gson.toJsonTree(process));
			return Responses.ok(jsonArray, req);
		} catch (Exception e) {
			System.out.println("Error in searching process | " + e.getMessage());
			e.printStackTrace();
			return Responses.serverError(e, req);
		}
	}
}
