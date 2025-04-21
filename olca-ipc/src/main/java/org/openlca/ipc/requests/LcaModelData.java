package org.openlca.ipc.requests;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openlca.ipc.dtos.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LcaModelData {
	private Map<Long, LcaCategoryJson> _lcaCategoriesMap;
	private Map<String, LcaFlowPropertyFactorJson> _lcaFpfs;
	private Map<Long, LcaFlowJson> _lcaFlowsMap;
	private Map<Long, LcaProcessJson> _lcaProcessesMap;
	private Map<Long, LcaExchangeJson> _exchangesMap;

	public List<LcaCategoryJson> lcaCategories;
	public List<LcaFlowPropertyFactorJson> lcaFpfs;
	public List<LcaFlowJson> lcaFlows;
	public List<LcaProcessJson> lcaProcesses;

	public Map<String, LcaFlowPropertyFactorJson> getLcaFpfsMap() {
		if (_lcaFpfs == null) {
			_lcaFpfs = new HashMap<>();
			lcaFpfs.forEach(c -> _lcaFpfs.put(c.flowId + "-" + c.fpId, c));
		}
		return _lcaFpfs;
	}

	public Map<Long, LcaCategoryJson> getLcaCategoriesMap() {
		if (_lcaCategoriesMap == null) {
			_lcaCategoriesMap = new HashMap<>();
			lcaCategories.forEach(c -> _lcaCategoriesMap.put(c.id, c));
		}
		return _lcaCategoriesMap;
	}

	public Map<Long, LcaProcessJson> getLcaProcessesMap() {
		if (_lcaProcessesMap == null) {
			_lcaProcessesMap = new HashMap<>();
			lcaProcesses.forEach(c -> _lcaProcessesMap.put(c.id, c));
		}
		return _lcaProcessesMap;
	}

	public Map<Long, LcaExchangeJson> getLcaExchangesMap() {
		if (_exchangesMap == null) {
			_exchangesMap = new HashMap<>();
			for (LcaProcessJson c : getLcaProcessesMap().values()) {
				c.exchanges.forEach(e -> _exchangesMap.put(e.id, e));
			}
		}
		return _exchangesMap;
	}

	public Map<Long, LcaFlowJson> getLcaFlowsMap() {
		if (_lcaFlowsMap == null) {
			_lcaFlowsMap = new HashMap<>();
			lcaFlows.forEach(c -> _lcaFlowsMap.put(c.id, c));
		}
		return _lcaFlowsMap;
	}

}
