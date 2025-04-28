package org.openlca.ipc.dtos;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openlca.core.results.UpstreamTree;

public class UpstreamTreeJson {
	public UpstreamNodeJson root;
	public Object ref;


	public JsonObject toJson() {
		var gson = new Gson();
		var json = new JsonObject();
		json.add("root", root.toJson());
		if (ref != null) {
			json.add("ref", gson.toJsonTree(ref));
		}
		return json;
	}

	public static UpstreamTreeJson of(UpstreamTree tree) {
		var json = new UpstreamTreeJson();
		json.root = UpstreamNodeJson.of(tree, tree.root);
		json.ref = tree.ref;
		return json;
	}

}
