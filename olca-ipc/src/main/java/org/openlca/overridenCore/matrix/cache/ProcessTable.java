package org.openlca.overridenCore.matrix.cache;


import gnu.trove.list.array.TLongArrayList;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ipc.dtos.LcaExchangeJson;

public class ProcessTable extends org.openlca.core.matrix.cache.ProcessTable {

	// follows Singleton Design Patterns
	private static ProcessTable instance;

	public static ProcessTable create(IDatabase db) {
		if (instance == null) instance = new ProcessTable(db);
		return instance;
	}

	protected ProcessTable(IDatabase db) {
		super(db);
	}


	public void addProcess(ProcessDescriptor process) {
		this.processes.put(process.id, process);
	}

	public void removeProcess(long processId) {
		this.processes.remove(processId);
	}

	public void addFlow(FlowDescriptor flow) {
		this.flows.put(flow.id, flow);
	}

	public void removeFlow(long flowId) {
		this.flows.remove(flowId);
	}

	/**
	 * Adds a flow provider to the list of providers for the given flow. here you should make sure
	 * that the flow is not an elementary flow and is not waste
	 */
	public void addFlowProvider(CalcExchange exchange) {
		if ((exchange.isInput && exchange.flowType == FlowType.WASTE_FLOW)
				|| (!exchange.isInput && exchange.flowType == FlowType.PRODUCT_FLOW)) {
			TLongArrayList list = flowProviders.get(exchange.flowId);
			if (list == null) {
				list = new TLongArrayList();
				flowProviders.put(exchange.flowId, list);
			}
			list.add(exchange.processId);
		}
	}

	public void removeFlowProvider(CalcExchange exchange) {
		TLongArrayList list = flowProviders.get(exchange.flowId);
		if (list != null) {
			list.remove(exchange.processId);
			if (list.isEmpty()) {
				flowProviders.remove(exchange.flowId);
			}
		}
	}

}
