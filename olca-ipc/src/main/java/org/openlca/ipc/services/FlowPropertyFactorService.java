package org.openlca.ipc.services;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.ipc.dtos.LcaFlowPropertyFactorJson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class FlowPropertyFactorService {

	FlowPropertyDao dao;

	public FlowPropertyFactorService on(IDatabase db) {
		dao = new FlowPropertyDao(db);
		return this;
	}

	public void insertBulk(Map<String, LcaFlowPropertyFactorJson> fpfsMap) {
		List<LcaFlowPropertyFactorJson> fpfJL = new ArrayList<>(fpfsMap.values());
		String sqlStmt = "insert into tbl_flow_property_factors(id, conversion_factor, f_flow, f_flow_property) values (?, ?, ?, ?)";
		NativeSql.on(dao.getDatabase()).batchInsert(
				sqlStmt,
				fpfsMap.size(),
				(i, statement) -> {
					LcaFlowPropertyFactorJson fpf = fpfJL.get(i);
					statement.setLong(1, fpf.id);
					statement.setDouble(2, fpf.conversionFactor);
					statement.setLong(3, fpf.flowId);
					statement.setLong(4, fpf.fpId);
					return true;
				});
	}

	public void deleteBulk(List<Long> ids) {
		String sqlStmt = "DELETE FROM tbl_flow_property_factors WHERE id IN " + NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(dao.getDatabase()).runUpdate(sqlStmt);
	}

}
