package org.openlca.ipc.services;


import org.openlca.core.database.IDatabase;
import org.openlca.ipc.requests.LcaModelData;

import java.util.stream.Collectors;

public class LcaModelService {

	private final ProcessService processService;
	private final CategoryService categoryService;
	private final FlowService flowService;
	private final FlowPropertyFactorService fpfService;
	private final ExchangeService exchangeService;

	public LcaModelService(IDatabase db) {
		this.processService = ProcessService.of(db);
		this.categoryService = CategoryService.of(db);
		this.flowService = FlowService.of(db);
		this.fpfService = FlowPropertyFactorService.of(db);
		this.exchangeService = ExchangeService.of(db);
	}

	public void createLcaModel(LcaModelData lcaModelData) {
		categoryService.insertBulk(lcaModelData.getLcaCategoriesMap());
		flowService.insertBulk(lcaModelData.getLcaFlowsMap());
		fpfService.insertBulk(lcaModelData.getLcaFpfsMap());
		exchangeService.insertBulk(lcaModelData.getLcaExchangesMap());
		processService.insertBulk(lcaModelData.getLcaProcessesMap());
	}

	public void deleteLcaModel(LcaModelData lcaModelData) {
		categoryService.deleteBulk(lcaModelData.getLcaCategoriesMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		flowService.deleteBulk(lcaModelData.getLcaFlowsMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		fpfService.deleteBulk(lcaModelData.getLcaFpfsMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		exchangeService.deleteBulk(lcaModelData.getLcaExchangesMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
		processService.deleteBulk(lcaModelData.getLcaProcessesMap().values().stream().map(c -> c.id).collect(Collectors.toList()));
	}

	public static LcaModelService of(IDatabase db) {
		return new LcaModelService(db);
	}


}
