package org.openlca.ipc.services;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.ipc.dtos.LcaExchangeJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ExchangeService {

	private final IDatabase database;
	private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

	private ExchangeService(IDatabase db) {
		this.database = db;
	}

	public void insertBulk(Map<Long, LcaExchangeJson> lcaExchangeJsonMap) {
		List<LcaExchangeJson> ejl = new ArrayList<>(lcaExchangeJsonMap.values());

		String sqlStmt = "insert into tbl_exchanges(id, f_owner, internal_id, f_flow, f_unit, is_input, f_flow_property_factor, resulting_amount_value, f_default_provider) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(
				sqlStmt,
				lcaExchangeJsonMap.size(),
				(i, statement) -> {
					LcaExchangeJson ex = ejl.get(i);
					statement.setLong(1, ex.id);
					statement.setLong(2, ex.processId);
					statement.setInt(3, ex.internalId);
					statement.setLong(4, ex.flowId);
					statement.setLong(5, ex.unitId);
					statement.setBoolean(6, ex.isInput);
					statement.setLong(7, ex.fpf.id);
					statement.setDouble(8, ex.amount);
					statement.setLong(9, ex.defaultProviderId);
					return true;
				});
	}

	public void deleteBulk(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			log.info("deleteBulk exchanges: no IDs provided");
			return;
		}
		var unique = new ArrayList<>(new HashSet<>(ids));
		int batchSize = 1000;
		int totalDeleted = 0;
		long start = System.currentTimeMillis();
		for (int i = 0; i < unique.size(); i += batchSize) {
			var batch = unique.subList(i, Math.min(i + batchSize, unique.size()));
			int before = countExisting(batch);
			String sqlStmt = "DELETE FROM tbl_exchanges WHERE id IN " + NativeSql.asList(new HashSet<>(batch));
			NativeSql.on(database).runUpdate(sqlStmt); // runUpdate returns void
			int after = countExisting(batch);
			int affected = before - after;
			totalDeleted += affected;
			log.debug("Deleted {} exchanges (existing before {}) in batch {}..{}", affected, before, i, Math.min(i + batchSize, unique.size()));
		}
		log.info("deleteBulk exchanges: requested={}, deleted={}, durationMs={}", unique.size(), totalDeleted, System.currentTimeMillis() - start);
	}

	private int countExisting(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return 0;
		final int[] result = {0};
		String inList = NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(database).query("SELECT COUNT(*) FROM tbl_exchanges WHERE id IN " + inList, r -> { result[0] = r.getInt(1); return true; });
		return result[0];
	}

	public static ExchangeService of(IDatabase db) {
		return new ExchangeService(db);
	}
}
