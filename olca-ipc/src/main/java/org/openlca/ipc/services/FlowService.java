package org.openlca.ipc.services;

import org.openlca.core.database.*;
import org.openlca.ipc.dtos.LcaFlowJson;

import java.util.*;

public class FlowService {

	public final IDatabase database;

	private FlowService(IDatabase db) {
		this.database = db;
	}

	public void insertBulk(Map<Long, LcaFlowJson> lcaFlowsMap) {
		List<LcaFlowJson> cjl = new ArrayList<>(lcaFlowsMap.values()).stream().filter(f -> !f.isEcoinventFlow).toList();
		String sqlStmt = "insert into tbl_flows(id, ref_id, name, f_category, flow_type,infrastructure_flow,f_reference_flow_property) values (?, ?, ?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(
				sqlStmt,
				cjl.size(),
				(i, statement) -> {
					LcaFlowJson flow = cjl.get(i);
					statement.setLong(1, flow.id);
					statement.setString(2, flow.refId);
					statement.setString(3, flow.name);
					if (flow.category == null) statement.setNull(4, java.sql.Types.BIGINT);
					else statement.setLong(4, flow.category);
					statement.setString(5, flow.flowType.toString());
					statement.setBoolean(6, flow.infrastructureFlow);
					statement.setLong(7, flow.rfpId);
					return true;
				}
		);
	}

	public void deleteBulk(List<Long> ids) {
		String sqlStmt = "DELETE FROM tbl_flows WHERE id IN " + NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(database).runUpdate(sqlStmt);
	}

	public static FlowService of(IDatabase db) {
		return new FlowService(db);
	}

}
