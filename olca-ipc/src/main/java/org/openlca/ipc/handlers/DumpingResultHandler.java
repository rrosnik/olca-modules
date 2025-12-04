package org.openlca.ipc.handlers;

import com.google.gson.JsonObject;
import org.openlca.core.services.JsonDataService;
import org.openlca.core.services.JsonResultService;
import org.openlca.core.services.Response;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.services.LcaModelService;
import org.openlca.ipc.services.ResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpingResultHandler {
	private static final Logger log = LoggerFactory.getLogger(LcaProcessHandler.class);
	private final JsonDataService service;
	private final JsonResultService results;
	private final LcaModelService lcaModelService;

	public DumpingResultHandler(HandlerContext context) {
		this.service = new JsonDataService(context.db());
		this.results = context.results();
		this.lcaModelService = new LcaModelService(context.db());
	}

	// added by @rrosnik - Reza Rostaminikoo <RezaRostaminikoo@gmail.com>
	@Rpc("result/dump")
	public RpcResponse dump(RpcRequest req) {
		return ResultRequest.of(req, rr -> {
			try {

				ResultService resultService = ResultService.of(service.db(), results.getResult(rr.id()));
				var result = new JsonObject();

				// add impact categories
				var impactCategories = new JsonObject();
				result.add("impactCategories", impactCategories);
				resultService.getImpactCategoriesMap().forEach((key, impact) -> {
					impactCategories.add(key, resultService.encodeImpact(impact));
				});

				// add total impacts
				var impactValues = new JsonObject();
				result.add("impactValues", impactValues);
				resultService.getTotalImpacts().forEach(impactValues::addProperty);

				// add envi flows
				var enviFlows = new JsonObject();
				result.add("enviFlows", enviFlows);
				resultService.getEnviFlows().forEach((key, enviFlow) -> enviFlows.add(String.valueOf(key), resultService.encodeEnviFlow(enviFlow)));

				// add total envi flows
				var totalEnviFlows = new JsonObject();
				result.add("totalEnviFlows", totalEnviFlows);
				resultService.getTotalEnviFlows().forEach((key, value) -> totalEnviFlows.addProperty(String.valueOf(key), value));

				// add tech flows
				var techFlows = new JsonObject();
				result.add("techFlows", techFlows);
				resultService.getTechFlows().forEach((key, techFlow) -> {
					techFlows.add(key, resultService.encodeTechFlow(techFlow));
				});

				// add contribution trees
				var contributionTrees = new JsonObject();
				result.add("contributionTrees", contributionTrees);
				resultService.getUpstreamTrees().forEach((impactRefId, tree) -> {
					var treeJson = resultService.encodeUpstreamTree(tree);
					contributionTrees.add(impactRefId, treeJson);
				});

				// add contribution of techFlows in enviFlows
				var techFlowContributions = resultService.encodeNestedMap(resultService.getContributionOfTechFlowsInEnviFlows());
				result.add("flowContribInEnvi", techFlowContributions);

				// add contribution of techFlows in impacts
				var techFlowContributionsInImpacts = resultService.encodeNestedMap(resultService.getContributionOfTechFlowsInImpacts());
				result.add("flowContribInImpacts", techFlowContributionsInImpacts);

				// add contribution of envi in impacts
				var enviFlowContributionsInImpacts = resultService.encodeNestedMap(resultService.getContributionOfEnviFlowsInImpacts());
				result.add("enviContribInImpacts", enviFlowContributionsInImpacts);

				// add contribution of enviFlows in techFlows
				var enviFlowContributions = resultService.encodeNestedMap(resultService.getContributionOfEnviFlowsInTechFlows());
				result.add("enviContribInFlow", enviFlowContributions);

				// before returning the result dispose the result by id
				results.dispose(rr.id());

				// also remove the lca Model from database
				var params = req.requireJsonObject();
				lcaModelService.deleteLcaModel(params.value().getAsJsonObject("lcaModel"));

				return Response.of(result);
			} catch (Exception e) {
				e.printStackTrace();
				return Response.error("failed to dump result: " + e.getMessage());
			}
		});
	}
}
