package org.openlca.ipc.services;

import org.openlca.core.database.*;
import org.openlca.ipc.dtos.LcaFlowJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FlowService {

	public final IDatabase database;
	private static final Logger log = LoggerFactory.getLogger(FlowService.class);

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
		if (ids == null || ids.isEmpty()) {
			log.info("deleteBulk flows: no IDs provided");
			return;
		}
		var unique = new ArrayList<>(new HashSet<>(ids));
		int batchSize = 1000;
		int totalDeleted = 0;
		for (int i = 0; i < unique.size(); i += batchSize) {
			var batch = unique.subList(i, Math.min(i + batchSize, unique.size()));
			int before = countExisting(batch);
			String sqlStmt = "DELETE FROM tbl_flows WHERE id IN " + NativeSql.asList(new HashSet<>(batch));
			NativeSql.on(database).runUpdate(sqlStmt); // returns void
			int after = countExisting(batch);
			int affected = before - after;
			totalDeleted += affected;
			log.debug("Deleted {} flows (existing before {}) in batch {}..{}", affected, before, i, Math.min(i + batchSize, unique.size()));
		}
		log.info("deleteBulk flows: requested={}, deleted={}", unique.size(), totalDeleted);
	}

	private int countExisting(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return 0;
		final int[] result = {0};
		String inList = NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(database).query("SELECT COUNT(*) FROM tbl_flows WHERE id IN " + inList, r -> { result[0] = r.getInt(1); return true; });
		return result[0];
	}

	public static FlowService of(IDatabase db) {
		return new FlowService(db);
	}

}
