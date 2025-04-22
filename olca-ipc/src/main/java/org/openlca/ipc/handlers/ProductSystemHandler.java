package org.openlca.ipc.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.services.JsonDataService;
import org.openlca.core.services.JsonUtil;
import org.openlca.core.services.Response;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.services.ProductSystemService;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSystemHandler {

	private static final Logger log = LoggerFactory.getLogger(ProductSystemHandler.class);
	private final JsonDataService service;
	private final HandlerContext context;

	public ProductSystemHandler(HandlerContext context) {
		this.context = context;
		this.service = new JsonDataService(context.db());
	}

	@Rpc("data/system/descriptors")
	public RpcResponse getAll(RpcRequest req) {
		try {
			ProductSystemDao dao = new ProductSystemDao(service.db());
			var result = dao.getDescriptors();
			Gson gson = new Gson();
			return Responses.ok(gson.toJsonTree(result), req);
		} catch (Exception e) {
			log.error("Error in getting all product system descriptors | {}", e.getMessage());
			return Responses.serverError(e, req);
		}
	}

	@Rpc("data/system/create")
	public RpcResponse create(RpcRequest req) {
		try {
			if (req.params == null || !req.params.isJsonObject())
				return Responses.invalidParams("no parameters given", req);
			var obj = req.params.getAsJsonObject();
			var processId = Json.getRefId(obj, "process");
			var config = Json.getObject(obj, "config");

			var resp = service.createProductSystem(processId, config);

			return Responses.of(resp, req);
		} catch (Exception e) {
			log.error("Error in creating product system | {}", e.getMessage());
			return Responses.serverError(e, req);
		}
	}

//	@Rpc("data/system/create2")
//	public RpcResponse create2(RpcRequest req) {
//		try {
//			if (req.params == null || !req.params.isJsonObject())
//				return Responses.invalidParams("no parameters given", req);
//			var obj = req.params.getAsJsonObject();
//			var processId = Json.getRefId(obj, "process");
//			var config = Json.getObject(obj, "config");
//
//			// var resp = service.createProductSystem(processId, config);
//			// instead of above line
//			var db = context.db();
//			var process = db.get(Process.class, processId);
//			if (process == null)
//				return Responses.serverError(new Exception("process does not exist: id=" + processId), req);
//			if (process.quantitativeReference == null)
//				return Responses.serverError(new Exception("process does not have a quantitative reference"), req);
//
//			var system = db.insert(ProductSystem.of(process));
//			var linkingConfig = JsonUtil.linkingConfigOf(config);
//			MatrixCache matrixCache = MatrixCache.createLazy(db);
//			matrixCache.registerNew();
//			var builder = new ProductSystemBuilder(matrixCache, linkingConfig);
//			builder.autoComplete(system);
//			system = ProductSystemBuilder.update(db, system);
//			var ref = Json.asRef(system);
//			return Responses.of(Response.of(ref), req);
//		} catch (Exception e) {
//			log.error("Error in creating product system | {}", e.getMessage());
//			return Responses.serverError(e, req);
//		}
//
//	}


	@Rpc("data/system/delete/processId")
	public RpcResponse deleteByProcessId(RpcRequest req) {
		try {
			if (req.params == null || !req.params.isJsonObject())
				return Responses.invalidParams("no parameters given", req);
			var obj = req.params.getAsJsonObject();
			var processId = Json.getLong(obj, "processId", 0);
			if (processId == 0)
				return Responses.invalidParams("no process id given", req);
			ProductSystemService.of(context.db()).deleteByProcessId(processId);
			return Responses.ok("Product system deleted successfully", req);
		} catch (Exception e) {
			log.error("Error in deleting product system | {}", e.getMessage());
			return Responses.serverError(e, req);
		}
	}

	@Rpc("data/system/delete/refId")
	public RpcResponse deleteByRefId(RpcRequest req) {
		try {
			if (req.params == null || !req.params.isJsonObject())
				return Responses.invalidParams("no parameters given", req);
			var obj = req.params.getAsJsonObject();
			var refId = Json.getRefId(obj, "product_system");
			if (refId == null)
				return Responses.invalidParams("no ref id given", req);
			ProductSystemService.of(context.db()).deleteByRefId(refId);
			return Responses.ok("Product system deleted successfully", req);
		} catch (Exception e) {
			log.error("Error in deleting product system | {}", e.getMessage());
			return Responses.serverError(e, req);
		}
	}

}
