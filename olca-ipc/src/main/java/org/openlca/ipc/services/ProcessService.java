package org.openlca.ipc.services;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.*;
import org.openlca.ipc.dtos.LcaExchangeJson;
import org.openlca.ipc.dtos.LcaFlowPropertyFactorJson;
import org.openlca.ipc.dtos.LcaProcessJson;

import java.util.*;

public class ProcessService {

	private final IDatabase database;

	private ProcessService(IDatabase db) {
		this.database = db;
	}

	public List<LcaProcessJson> search(String searchTerm, Integer page, Integer pageSize, List<String> refIds) {
		var result = new ArrayList<LcaProcessJson>();
		StringBuilder sql = new StringBuilder("""
					select
						d.id,
						d.ref_id,
						d.name,
						d.process_type,
						e.id,
						e.f_flow,
						e.f_unit,
						f.f_reference_flow_property
				    from tbl_processes d
				    left join tbl_exchanges e on e.id = d.f_quantitative_reference
				    left  join tbl_flows f on e.f_flow = f.id
				    where f.f_reference_flow_property <> 0 AND
				""");
		// check if ref ids has items process ref ids should be in that list
		if (refIds != null && !refIds.isEmpty()) {
			sql.append("d.ref_id IN (");
			for (int i = 0; i < refIds.size(); i++) {
				sql.append("'").append(refIds.get(i)).append("'");
				if (i < refIds.size() - 1) {
					sql.append(", ");
				}
			}
			sql.append(") AND ");
		}
		var terms = searchTerm.split(" ");
		for (String term : terms) {
			sql.append(" d.name LIKE '%").append(term).append("%'");
		}

		// append page and page size to the query

		sql.append(" OFFSET ").append((page - 1) * pageSize).append("ROWS").append(" FETCH NEXT ").append(pageSize).append(" ROWS ONLY ");

		NativeSql.on(database).query(sql.toString(), List.of(), r -> {
			var d = new LcaProcessJson();
			d.id = r.getLong(1);
			d.refId = r.getString(2);
			d.name = r.getString(3);
			d.processType = ProcessType.valueOf(r.getString(4));
			var exchange = new LcaExchangeJson();
			exchange.id = r.getLong(5);
			exchange.flowId = r.getLong(6);
			exchange.unitId = r.getLong(7);
			exchange.isInput = false;
			exchange.fpf = new LcaFlowPropertyFactorJson();
			exchange.fpf.conversionFactor = 1.0;
			exchange.fpf.fpId = r.getLong(8);
			d.quantitativeReference = exchange;


			result.add(d);
			return true;
		});
		return result;
	}


	public void insertBulk(Map<Long, LcaProcessJson> lcaProcessJsonMap) {
		List<LcaProcessJson> pjl = new ArrayList<>(lcaProcessJsonMap.values());
		String sqlStmt = "insert into tbl_processes(id, ref_id, name, f_category, process_type, f_quantitative_reference, last_internal_id) values (?, ?, ?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(
				sqlStmt,
				lcaProcessJsonMap.size(),
				(i, statement) -> {
					LcaProcessJson process = pjl.get(i);
					statement.setLong(1, process.id);
					statement.setString(2, process.refId);
					statement.setString(3, process.name);
					if (process.category == null) statement.setNull(4, java.sql.Types.BIGINT);
					else statement.setLong(4, process.category);
					statement.setString(5, process.processType.toString());
					statement.setLong(6, process.quantitativeReference.id);
					statement.setInt(7, process.lastInternalId);
					return true;
				});
	}

	public void deleteBulk(List<Long> ids) {
		String sqlStmt = "DELETE FROM tbl_processes WHERE id IN " + NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(database).runUpdate(sqlStmt);
	}
//////////////////////

	public static ProcessService of(IDatabase db) {
		return new ProcessService(db);
	}


}
