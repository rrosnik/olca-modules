package org.openlca.ipc.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.openlca.core.database.IDatabase;
import org.openlca.ipc.dtos.LcaModelData;
import org.openlca.overridenCore.matrix.cache.MatrixCache;

import java.util.stream.Collectors;

public class LcaModelService {
	private final IDatabase database;

	private final ProcessService processService;
	private final CategoryService categoryService;
	private final FlowService flowService;
	private final FlowPropertyFactorService fpfService;
	private final ExchangeService exchangeService;
	private final ProductSystemService productSystemService;

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
		var cache = MatrixCache.createLazy(database);
		var processTable = cache.getProcessTable();
		var flowTable = cache.getFlowTypeTable();
		lcaModelData.getLcaProcessesMap().values().forEach(process -> processTable.addProcess(process.toDescriptor()));
		lcaModelData.getLcaFlowsMap().values().forEach(flow -> {
			if(flow.isEcoinventFlow) return;
			var descriptor = flow.toDescriptor();
			flowTable.addFlow(descriptor);
			processTable.addFlow(descriptor);
		});
		lcaModelData.getLcaExchangesMap().values().forEach(exchange -> processTable.addFlowProvider(exchange.toCalcExchange()));


		productSystemService.create(lcaModelData.productSystem, lcaModelData.getLcaProcessesMap().get(lcaModelData.productSystem.process));
	}

	public void deleteLcaModel(JsonObject jsonData) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		LcaModelData lcaModelData = objectMapper.readValue(jsonData.toString(), LcaModelData.class);
		deleteLcaModel(lcaModelData);
	}
	public void deleteLcaModel(LcaModelData lcaModelData) {
		categoryService.deleteBulk(lcaModelData.getLcaCategoriesMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		flowService.deleteBulk(lcaModelData.getLcaFlowsMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		fpfService.deleteBulk(lcaModelData.getLcaFpfsMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		exchangeService.deleteBulk(lcaModelData.getLcaExchangesMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		processService.deleteBulk(lcaModelData.getLcaProcessesMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		productSystemService.deleteById(lcaModelData.productSystem.id);

		//removing fed items from cache
		var cache = MatrixCache.createLazy(database);
		var processTable = cache.getProcessTable();
		var flowTable = cache.getFlowTypeTable();
		lcaModelData.getLcaProcessesMap().values().forEach(process -> processTable.removeProcess(process.id));
		lcaModelData.getLcaFlowsMap().values().forEach(flow -> {
			if(flow.isEcoinventFlow) return;
			processTable.removeFlow(flow.id);
			flowTable.remove(flow.id);
		});
		lcaModelData.getLcaExchangesMap().values().forEach(exchange -> processTable.removeFlowProvider(exchange.toCalcExchange()));
	}

	public static LcaModelService of(IDatabase db) {
		return new LcaModelService(db);
	}


}
