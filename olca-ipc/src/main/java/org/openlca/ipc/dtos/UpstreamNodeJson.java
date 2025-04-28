package org.openlca.ipc.dtos;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;

import java.util.ArrayList;
import java.util.List;

public class UpstreamNodeJson {

	public String techFlow;
	/**
	 * the upstream result of this node.
	 */
	public Double result;
	/**
	 * the required amount of the provider flow of this upstream node.
	 */
	public Double requiredAmount;
	/**
	 * the scaling factor of this upstream node.
	 */
	public Double scaling;
	/**
	 * the direct contribution of the process (tech-flow) of the node to
	 * the total result the node.
	 */
	public Double direct;

	/**
	 * The child nodes of this node. Node that the child nodes are calculated on
	 * demand and thus initialized with null.
	 */
	public List<UpstreamNodeJson> children;


	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("techFlow", techFlow);
		json.addProperty("result", result);
		json.addProperty("requiredAmount", requiredAmount);
		json.addProperty("scaling", scaling);
		json.addProperty("direct", direct);
		if (children != null) {
			var childArray = new JsonArray();
			children.forEach(child -> childArray.add(child.toJson()));
			json.add("children", childArray);
		}
		return json;
	}

	public static UpstreamNodeJson of(UpstreamTree tree,UpstreamNode node) {
		var json = new UpstreamNodeJson();
		json.techFlow = node.provider().provider().id + "-" + node.provider().flow().id;
		json.result = node.result();
		json.requiredAmount = node.requiredAmount();
		json.scaling = node.scalingFactor();
		json.direct = node.directContribution();
		tree.childs(node).forEach(child -> {
			if (json.children == null) json.children = new ArrayList<>();
			json.children.add(of(tree, child));
		});
		return json;
	}
}

