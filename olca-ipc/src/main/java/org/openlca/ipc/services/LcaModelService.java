package org.openlca.ipc.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.ipc.dtos.LcaModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;

public class LcaModelService {
	private final IDatabase database;

	private final ProcessService processService;
	private final CategoryService categoryService;
	private final FlowService flowService;
	private final FlowPropertyFactorService fpfService;
	private final ExchangeService exchangeService;
	private final ProductSystemService productSystemService;

	private static final Logger log = LoggerFactory.getLogger(LcaModelService.class);

	public LcaModelService(IDatabase db) {
		this.database = db;
		this.processService = ProcessService.of(db);
		this.categoryService = CategoryService.of(db);
		this.flowService = FlowService.of(db);
		this.fpfService = FlowPropertyFactorService.of(db);
		this.exchangeService = ExchangeService.of(db);
		this.productSystemService = ProductSystemService.of(db);
	}

	public void createLcaModel(JsonObject jsonData) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		LcaModelData lcaModelData = objectMapper.readValue(jsonData.toString(), LcaModelData.class);
		createLcaModel(lcaModelData);
	}

	public void createLcaModel(LcaModelData lcaModelData) {
		categoryService.insertBulk(lcaModelData.getLcaCategoriesMap());
		flowService.insertBulk(lcaModelData.getLcaFlowsMap());
		fpfService.insertBulk(lcaModelData.getLcaFpfsMap());
		exchangeService.insertBulk(lcaModelData.getLcaExchangesMap());
		processService.insertBulk(lcaModelData.getLcaProcessesMap());

		// feeding cache by new items
//		var cache = MatrixCache.createLazy(database);
//		var processTable = cache.getProcessTable();
//		var flowTable = cache.getFlowTypeTable();
//		lcaModelData.getLcaProcessesMap().values().forEach(process -> processTable.addProcess(process.toDescriptor()));
//		lcaModelData.getLcaFlowsMap().values().forEach(flow -> {
//			if(flow.isEcoinventFlow) return;
//			var descriptor = flow.toDescriptor();
//			flowTable.addFlow(descriptor);
//			processTable.addFlow(descriptor);
//		});
//		lcaModelData.getLcaExchangesMap().values().forEach(exchange -> processTable.addFlowProvider(exchange.toCalcExchange()));


		productSystemService.create(lcaModelData.productSystem, lcaModelData.getLcaProcessesMap().get(lcaModelData.productSystem.process));
	}

	public void deleteLcaModel(JsonObject jsonData) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		LcaModelData lcaModelData = objectMapper.readValue(jsonData.toString(), LcaModelData.class);
		deleteLcaModel(lcaModelData);
	}
	public void deleteLcaModel(LcaModelData lcaModelData) {
		// counts before deletion (whole tables)
		long psBefore = count("tbl_product_systems");
		long exBefore = count("tbl_exchanges");
		long fpfBefore = count("tbl_flow_property_factors");
		long flowBefore = count("tbl_flows");
		long procBefore = count("tbl_processes");
		long catBefore = count("tbl_categories");
		// targeted ID sets
		List<Long> exchangeIds = lcaModelData.getLcaExchangesMap().values().stream().map(c -> c.id).toList();
		List<Long> fpfIds = lcaModelData.getLcaFpfsMap().values().stream().map(c -> c.id).toList();
		List<Long> flowIds = lcaModelData.getLcaFlowsMap().values().stream().map(c -> c.id).toList();
		List<Long> processIds = lcaModelData.getLcaProcessesMap().values().stream().map(c -> c.id).toList();
		List<Long> categoryIds = lcaModelData.getLcaCategoriesMap().values().stream().map(c -> c.id).toList();
		long psTarget = lcaModelData.productSystem == null ? 0 : countIn("tbl_product_systems", List.of(lcaModelData.productSystem.id));
		long exTarget = countIn("tbl_exchanges", exchangeIds);
		long fpfTarget = countIn("tbl_flow_property_factors", fpfIds);
		long flowTarget = countIn("tbl_flows", flowIds);
		long procTarget = countIn("tbl_processes", processIds);
		long catTarget = countIn("tbl_categories", categoryIds);
		log.info("Table totals before deletion: ps={}, ex={}, fpfs={}, flows={}, procs={}, cats={}", psBefore, exBefore, fpfBefore, flowBefore, procBefore, catBefore);
		log.info("Targeted existing rows: psTarget={}, exTarget={}, fpfTarget={}, flowTarget={}, procTarget={}, catTarget={}", psTarget, exTarget, fpfTarget, flowTarget, procTarget, catTarget);

		// perform deletions
		if (lcaModelData.productSystem != null)
			productSystemService.deleteById(lcaModelData.productSystem.id);
		exchangeService.deleteBulk(exchangeIds);
		fpfService.deleteBulk(fpfIds);
		flowService.deleteBulk(flowIds);
		processService.deleteBulk(processIds);
		categoryService.deleteBulk(categoryIds);

		// counts after deletion
		long psAfter = count("tbl_product_systems");
		long exAfter = count("tbl_exchanges");
		long fpfAfter = count("tbl_flow_property_factors");
		long flowAfter = count("tbl_flows");
		long procAfter = count("tbl_processes");
		long catAfter = count("tbl_categories");
		long psRemainingTargets = countIn("tbl_product_systems", lcaModelData.productSystem == null ? List.of() : List.of(lcaModelData.productSystem.id));
		long exRemainingTargets = countIn("tbl_exchanges", exchangeIds);
		long fpfRemainingTargets = countIn("tbl_flow_property_factors", fpfIds);
		long flowRemainingTargets = countIn("tbl_flows", flowIds);
		long procRemainingTargets = countIn("tbl_processes", processIds);
		long catRemainingTargets = countIn("tbl_categories", categoryIds);
		log.info("Table totals after deletion: ps={}, ex={}, fpfs={}, flows={}, procs={}, cats={}", psAfter, exAfter, fpfAfter, flowAfter, procAfter, catAfter);
		log.info("Remaining targeted rows: psRem={}, exRem={}, fpfRem={}, flowRem={}, procRem={}, catRem={}", psRemainingTargets, exRemainingTargets, fpfRemainingTargets, flowRemainingTargets, procRemainingTargets, catRemainingTargets);
	}

	private long count(String table) {
		final long[] result = {0};
		NativeSql.on(database).query("SELECT COUNT(*) FROM " + table, r -> {
			result[0] = r.getLong(1);
			return true;
		});
		return result[0];
	}

	private long countIn(String table, List<Long> ids) {
		if (ids == null || ids.isEmpty()) return 0;
		final long[] result = {0};
		String inList = NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(database).query("SELECT COUNT(*) FROM " + table + " WHERE id IN " + inList, r -> { result[0] = r.getLong(1); return true; });
		return result[0];
	}

	public static LcaModelService of(IDatabase db) {
		return new LcaModelService(db);
	}


}
