package org.openlca.ipc.services;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemService {

	private final IDatabase database;

	private ProductSystemService(IDatabase db) {
		this.database = db;
	}

	public ProductSystemDescriptor getByProcessId(long processId) {
		final ProductSystemDescriptor[] result = new ProductSystemDescriptor[1];
		NativeSql.on(database)
				.query(
						"SELECT ps.id, ps.ref_id, ps.name FROM tbl_product_systems ps " +
								"WHERE ps.f_reference_process = " + processId,
						r -> {
							result[0] = new ProductSystemDescriptor();
							result[0].id = r.getLong(1);
							result[0].refId = r.getString(2);
							result[0].name = r.getString(3);
							return true;
						});
		return result[0];
	}

	public void deleteByProcessId(long processId) {
		try {
			ProductSystemDescriptor psd = getByProcessId(processId);
			ProductSystemDao dao = new ProductSystemDao(database);
			// delete bulk from tbl_product_systems
			// tbl_product_system_processes
			// tbl_process_links
			// tbl_parameter_redef_sets
			// TODO: if we are working with projects table then we also need to delete that
			NativeSql.on(database).runUpdate("DELETE FROM tbl_product_systems WHERE f_reference_process = " + processId);
			NativeSql.on(database).runUpdate("DELETE FROM tbl_product_system_processes WHERE f_product_system = " + psd.id);
			NativeSql.on(database).runUpdate("DELETE FROM tbl_process_links WHERE f_product_system = " + psd.id);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void deleteByRefId(String refId) {
		ProductSystemDao dao = new ProductSystemDao(database);
		ProductSystem ps = dao.getForRefId(refId);
		dao.delete(ps.id);
	}

	public void deleteById(long id) {
		new ProductSystemDao(database).delete(id);
	}


//////////////////////


	public static ProductSystemService of(IDatabase db) {
		return new ProductSystemService(db);
	}


}
