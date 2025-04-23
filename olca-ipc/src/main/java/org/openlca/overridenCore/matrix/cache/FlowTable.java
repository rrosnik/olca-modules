package org.openlca.overridenCore.matrix.cache;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowTable extends org.openlca.core.matrix.cache.FlowTable {

	private static FlowTable instance;

	public static FlowTable create(IDatabase database) {
		if (instance == null) {
			instance = new FlowTable(database);
		}
		return instance;
	}

	protected FlowTable(IDatabase database) {
		super(database);
	}

	public void addFlow(FlowDescriptor flow) {
		this.map.put(flow.id, flow);
	}

	public void remove(long flowId) {
		this.map.remove(flowId);
	}

}
