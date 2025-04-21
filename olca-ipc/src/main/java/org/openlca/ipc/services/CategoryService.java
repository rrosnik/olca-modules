package org.openlca.ipc.services;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.ipc.dtos.LcaCategoryJson;

import java.util.*;

public class CategoryService {

	private final IDatabase database;

	private CategoryService(IDatabase db) {
		this.database = db;
	}

	public void insertBulk(Map<Long, LcaCategoryJson> lcaCategoriesMap) {
		List<LcaCategoryJson> cjl = new ArrayList<>(lcaCategoriesMap.values());
		String sqlStmt = "insert into tbl_categories(id, ref_id, name, f_category, model_type) values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(
				sqlStmt,
				lcaCategoriesMap.size(),
				(i, statement) -> {
					LcaCategoryJson cat = cjl.get(i);
					statement.setLong(1, cat.id);
					statement.setString(2, cat.refId);
					statement.setString(3, cat.name);
					if (cat.category == null) statement.setNull(4, java.sql.Types.BIGINT);
					else statement.setLong(4, cat.category);
					statement.setString(5, cat.modelType.toString());
					return true;
				});
	}

	public void deleteBulk(List<Long> ids) {
		String sqlStmt = "DELETE FROM tbl_categories WHERE id IN " + NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(database).runUpdate(sqlStmt);
	}

	public static CategoryService of(IDatabase db) {
		return new CategoryService(db);
	}

}
