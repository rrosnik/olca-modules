package org.openlca.ipc.services;

import org.openlca.core.database.ExchangeDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.ipc.dtos.LcaExchangeJson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ExchangeService {

	private final ExchangeDao dao;

	private ExchangeService(IDatabase db) {
		dao = new ExchangeDao(db);
	}

	public void insertBulk(Map<Long, LcaExchangeJson> lcaExchangeJsonMap) {
		List<LcaExchangeJson> ejl = new ArrayList<>(lcaExchangeJsonMap.values());

		String sqlStmt = "insert into tbl_exchanges(id, f_owner, internal_id, f_flow, f_unit, is_input, f_flow_property_factor, resulting_amount_value) values (?, ?, ?, ?, ?, ?, ?, ?)";
		NativeSql.on(dao.getDatabase()).batchInsert(
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
//                statement.setLong(8, ex.defaultProviderId);
					return true;
				});
	}

	public void deleteBulk(List<Long> ids) {
		String sqlStmt = "DELETE FROM tbl_exchanges WHERE id IN " + NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(dao.getDatabase()).runUpdate(sqlStmt);
	}

	public static ExchangeService of(IDatabase db) {
		return new ExchangeService(db);
	}
}
