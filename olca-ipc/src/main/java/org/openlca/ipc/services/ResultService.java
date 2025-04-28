package org.openlca.ipc.services;


import com.google.gson.JsonObject;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.UpstreamTree;
import org.openlca.ipc.dtos.UpstreamTreeJson;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.JsonRefs;

import java.util.HashMap;
import java.util.Map;


public class ResultService {

	private IDatabase database;
	private final LcaResult lcaResult;
	private final JsonRefs refs;

	private Map<String, TechFlow> _techflowsMap;
	private Map<String, EnviFlow> _enviFlowsMap;
	private Map<String, ImpactDescriptor> _impactCategoriesMap;

	private ResultService(IDatabase database, LcaResult result) {
		this.database = database;
		this.lcaResult = result;
		this.refs = JsonRefs.of(database);
	}

	public static ResultService of(IDatabase database, LcaResult result) {
		return new ResultService(database, result);
	}

	public Map<String, EnviFlow> getEnviFlows() {
		if (_enviFlowsMap == null) {
			_enviFlowsMap = new HashMap<>();
			lcaResult.enviIndex().forEach(ef -> _enviFlowsMap.put(getKeyOfEnviFlow(ef), ef));
		}
		return _enviFlowsMap;
	}

	public Map<String, TechFlow> getTechFlows() {
		if (_techflowsMap == null) {
			_techflowsMap = new HashMap<>();
			lcaResult.techIndex().forEach(techFlow -> _techflowsMap.put(getKeyOfTechFlow(techFlow), techFlow));
		}
		return _techflowsMap;
	}

	private ImpactDescriptor impactCategoryOf(String impactCategoryRefId) {
		return getImpactCategoriesMap().get(impactCategoryRefId);
	}

	public Map<String, ImpactDescriptor> getImpactCategoriesMap() {
		if (_impactCategoriesMap == null) {
			_impactCategoriesMap = new HashMap<>();
			var index = lcaResult.impactIndex();
			for (ImpactDescriptor impactDescriptor : index.content())
				_impactCategoriesMap.put(impactDescriptor.refId, impactDescriptor);
		}
		return _impactCategoriesMap;
	}

	public Map<String, Double> getTotalImpacts() {
		var result = new HashMap<String, Double>();
		lcaResult.getTotalImpacts().forEach(impactValue -> result.put(impactValue.impact().refId, impactValue.value()));
		return result;
	}


	public Map<String, Double> getTotalRequirements() {
		var result = new HashMap<String, Double>();
		lcaResult.getTotalRequirements().forEach(techFlowValue -> result.put(getKeyOfTechFlow(techFlowValue.techFlow()), techFlowValue.value()));
		return result;
	}

	public Map<String, Double> getTotalEnviFlows() {
		var result = new HashMap<String, Double>();
		lcaResult.getTotalFlows().forEach(techFlowValue -> {
			result.put(getKeyOfEnviFlow(techFlowValue.enviFlow()), techFlowValue.value());
		});
		return result;
	}

	public UpstreamTree getUpstreamTreeOf(ImpactDescriptor impact) {
		return UpstreamTree.of(lcaResult.provider(), impact);
	}

	public Map<String, UpstreamTree> getUpstreamTrees() {
		Map<String, UpstreamTree> result = new HashMap<>();
		getImpactCategoriesMap().values().forEach(impact -> {
			result.put(impact.refId, getUpstreamTreeOf(impact));
		});
		return result;
	}

	public Map<String, Map<String, Double>> getContributionofTechFlowsInEnviFlows() {
		var startTime = System.nanoTime();
		Map<String, Map<String, Double>> result = new HashMap<>();
		getEnviFlows().values().forEach(ef -> {
			Map<String, Double> techFlows = new HashMap<>();
			lcaResult.getDirectFlowValuesOf(ef).forEach(tfv -> {
				if (tfv.value() == 0d) return;
				techFlows.put(getKeyOfTechFlow(tfv.techFlow()), tfv.value());
			});
			if (!techFlows.isEmpty())
				result.put(getKeyOfEnviFlow(ef), techFlows);
		});
		var endTime = System.nanoTime();
		var duration = (endTime - startTime) / 1_000_000;
		System.out.println("Time taken to calculate contribution of tech flows in envi flows: " + duration + " ms");
		return result;
	}

	public Map<String, Map<String, Double>> getContributionOfEnviFlowsInTechFlows() {
		var startTime = System.nanoTime();
		Map<String, Map<String, Double>> result = new HashMap<>();
		getTechFlows().values().forEach(tf -> {
			Map<String, Double> enviFlows = new HashMap<>();
			lcaResult.getDirectFlowsOf(tf).forEach(efv -> {
				if (efv.value() == 0d) return;
				enviFlows.put(getKeyOfEnviFlow(efv.enviFlow()), efv.value());
			});
			if (!enviFlows.isEmpty())
				result.put(getKeyOfTechFlow(tf), enviFlows);
		});
		var endTime = System.nanoTime();
		var duration = (endTime - startTime) / 1_000_000;
		System.out.println("Time taken to calculate contribution of envi flows in tech flow: " + duration + " ms");
		return result;
	}

	public Map<String, Map<String, Double>> getContributionOfTechFlowsInImpacts() {
		var startTime = System.nanoTime();
		Map<String, Map<String, Double>> result = new HashMap<>();
		getImpactCategoriesMap().values().forEach(ic -> {
			Map<String, Double> techFlows = new HashMap<>();
			lcaResult.getDirectImpactValuesOf(ic).forEach(tfv -> {
				if (tfv.value() == 0d) return;
				techFlows.put(getKeyOfTechFlow(tfv.techFlow()), tfv.value());
			});
			if (!techFlows.isEmpty())
				result.put(ic.refId, techFlows);
		});
		var endTime = System.nanoTime();
		var duration = (endTime - startTime) / 1_000_000;
		System.out.println("Time taken to calculate contribution of tech flows in impacts: " + duration + " ms");
		return result;
	}

	public Map<String, Map<String, Double>> getContributionOfEnviFlowsInImpacts() {
		var startTime = System.nanoTime();
		Map<String, Map<String, Double>> result = new HashMap<>();
		getImpactCategoriesMap().values().forEach(ic -> {
			Map<String, Double> enviFlows = new HashMap<>();
			lcaResult.getFlowImpactsOf(ic).forEach(efv -> {
				if (efv.value() == 0d) return;
				enviFlows.put(getKeyOfEnviFlow(efv.enviFlow()), efv.value());
			});
			if (!enviFlows.isEmpty())
				result.put(ic.refId, enviFlows);
		});
		var endTime = System.nanoTime();
		var duration = (endTime - startTime) / 1_000_000;
		System.out.println("Time taken to calculate contribution of envi flows in impacts: " + duration + " ms");
		return result;
	}
//
//	public void getSankeyData(){
//		getImpactCategoriesMap().values().forEach
//		Sankey.of(lcaResult.impact(), result.provider()
//		// check all possible errors
//		var rr = JsonSankeyRequest.resolve(result, req);
//		if (rr.isError())
//			return Response.error("failed to handle request: " + rr.error());
//		var r = rr.value();
//		if (r.isForCosts())
//			return Response.error("not yet implemented");
//		if (!r.hasImpact() && !r.hasFlow())
//			return Response.error("no impact category or flow provided");
//
//		// build the Sankey diagram
//		var config = r.hasImpact()
//				? Sankey.of(r.impact(), result.provider())
//				: Sankey.of(r.flow(), result.provider());
//		var sankey = config.withMaximumNodeCount(r.maxNodes())
//				.withMinimumShare(r.minShare())
//				.build();
//
//		// convert the graph
//		var json = JsonSankeyGraph.of(sankey,JsonRefs.of(db));
//		return Response.of(json);
//	}

	///////////////////////////////////////////////
	// Json Object creation methods
	public JsonObject encodeTechFlow(TechFlow techFlow) {
		var obj = new JsonObject();
		Json.put(obj, "key", getKeyOfTechFlow(techFlow));
		Json.put(obj, "provider", refs.asRef(techFlow.provider()));
		Json.put(obj, "flow", refs.asRef(techFlow.flow()));
		return obj;
	}

	public JsonObject encodeImpact(ImpactDescriptor impact) {
		var obj = refs.asRef(impact);
		Json.put(obj, "key", impact.refId);
		return obj;
	}

	public JsonObject encodeEnviFlow(EnviFlow enviFlow) {
		var obj = new JsonObject();
		Json.put(obj, "key", getKeyOfEnviFlow(enviFlow));
		Json.put(obj, "flow", refs.asRef(enviFlow.flow()));
		if (enviFlow.location() != null) {
			Json.put(obj, "location", refs.asRef(enviFlow.location()));
		}
		Json.put(obj, "isInput", enviFlow.isInput());
		if (enviFlow.isVirtual()) {
			Json.put(obj, "isVirtual", true);
			if (enviFlow.wrapped() instanceof RootDescriptor wrapped) {
				Json.put(obj, "wrapped", refs.asRef(wrapped));
			}
		}
		return obj;
	}

	public JsonObject encodeUpstreamTree(UpstreamTree tree) {
		return UpstreamTreeJson.of(tree).toJson();
	}

	public JsonObject encodeNestedMap(Map<String, Map<String, Double>> map) {
		var jsonMap = new JsonObject();
		map.forEach((key, value) -> {
			var innerMap = new JsonObject();
			value.forEach(innerMap::addProperty);
			jsonMap.add(key, innerMap);
		});
		return jsonMap;
	}


	public String getKeyOfEnviFlow(EnviFlow enviFlow) {
		return String.valueOf(enviFlow.hashCode());
	}

	public String getKeyOfTechFlow(TechFlow techFlow) {
		return techFlow.provider().id + "-" + techFlow.flow().id;
	}
}
