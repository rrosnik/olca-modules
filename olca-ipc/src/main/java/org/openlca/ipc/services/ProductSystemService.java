package org.openlca.ipc.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.ipc.dtos.LcaProcessJson;
import org.openlca.ipc.dtos.LcaProductSystemJson;
import org.openlca.jsonld.Json;
//import org.openlca.overridenCore.matrix.cache.MatrixCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class ProductSystemService {

	private static final Logger log = LoggerFactory.getLogger(ProductSystemService.class);
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
			// delete bulk from tbl_product_systems
			// tbl_product_system_processes
			// tbl_process_links
			// tbl_parameter_redef_sets
			// TODO: if we are working with projects table then we also need to delete that
			NativeSql.on(database).runUpdate("DELETE FROM tbl_product_systems WHERE f_reference_process = " + processId);
			NativeSql.on(database).runUpdate("DELETE FROM tbl_product_system_processes WHERE f_product_system = " + psd.id);
			NativeSql.on(database).runUpdate("DELETE FROM tbl_process_links WHERE f_product_system = " + psd.id);
		} catch (Exception e) {
			log.error("Error in deleting product system by process id | {}", e.getMessage());
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


	private static LinkingConfig linkingConfigOf(JsonObject json) {
		var conf = new LinkingConfig();
		if (json == null) return conf;
		var providerLinking = Json.getEnum(json, "providerLinking", ProviderLinking.class);
		if (providerLinking != null) conf.providerLinking(providerLinking);
		var preferUnitProcesses = Json.getBool(json, "preferUnitProcesses", false);
		conf.preferredType(preferUnitProcesses ? ProcessType.UNIT_PROCESS : ProcessType.LCI_RESULT);
		Json.getDouble(json, "cutoff").ifPresent(conf::cutoff);
		return conf;
	}

	public ProductSystem create(Long processId, JsonObject jsonConfig) {
		var process = database.get(Process.class, processId);
		var linkingConfig = ProductSystemService.linkingConfigOf(jsonConfig);
		return create(process, linkingConfig);
	}

	public ProductSystem create(String processRefId, JsonObject jsonConfig) {
		var process = database.get(Process.class, processRefId);
		var linkingConfig = ProductSystemService.linkingConfigOf(jsonConfig);
		return create(process, linkingConfig);
	}

	public ProductSystem create(LcaProductSystemJson productSystemJson, LcaProcessJson processJson) {
		var process = database.get(Process.class, processJson.id);
		Gson gson = new Gson();
		System.out.println("productSystemJson");
		System.out.println(gson.toJsonTree(productSystemJson).toString());
		System.out.println("processJson");
		System.out.println(gson.toJsonTree(processJson).toString());
		var linkingConfig = new LinkingConfig().providerLinking(productSystemJson.providerLinking).preferredType(productSystemJson.processType);
		var descriptor = productSystemJson.toDescriptor();
		return create(process, linkingConfig, descriptor);
	}

	public ProductSystem create(Process process, LinkingConfig config) {
		return create(process, config, null);
	}

	public ProductSystem create(Process process, LinkingConfig config, @Nullable ProductSystemDescriptor descriptor) {
		var ps = ProductSystem.of(process);
		if (descriptor != null) {
			ps.id = descriptor.id;
			ps.refId = descriptor.refId;
			ps.name = descriptor.name;
		}
		var system = database.insert(ps);
		var builder = new ProductSystemBuilder(MatrixCache.createLazy(database), config);
		builder.autoComplete(system);
		return ProductSystemBuilder.update(database, system);
	}

//////////////////////


	public static ProductSystemService of(IDatabase db) {
		return new ProductSystemService(db);
	}


}
