package org.openlca.ipc.services;

import org.openlca.core.database.*;
import org.openlca.ipc.dtos.LcaFlowJson;

import java.util.*;

public class FlowService {

	public final FlowDao dao;

	private FlowService(IDatabase db) {
		dao = new FlowDao(db);
	}
//
//	public List<FlowDescriptor> list(Optional<Long> categoryId) {
//		List<FlowDescriptor> result = new ArrayList<FlowDescriptor>(List.of());
//
//		String sql = "SELECT d.id,d.ref_id,d.name FROM tbl_flows d where d.f_category";
//
//		if (categoryId.isEmpty()) sql += " is null";
//		else sql += " = " + categoryId.get();
//
//		NativeSql.on(dao.getDatabase()).query(sql, null, r -> {
//			var d = new FlowDescriptor();
//			d.id = r.getLong(1);
//			d.refId = r.getString(2);
//			d.name = r.getString(3);
//			result.add(d);
//			return true;
//		});
//		return result;
//	}

//	public Flow detail(Long flowId) {
//		return dao.getForId(flowId);
//	}

//	public List<Flow> byRefIds(List<String> refIds) {
//		List<Flow> result = dao.getForRefIds(refIds.stream().collect(Collectors.toSet()));
//		result.forEach(f -> {
//			f.category = null;
//			f.referenceFlowProperty.category = null;
//			f.referenceFlowProperty.unitGroup.category = null;
//			f.referenceFlowProperty.unitGroup.units.clear();
//			f.referenceFlowProperty.unitGroup.defaultFlowProperty = null;
//
//			f.flowPropertyFactors.forEach(fpf -> {
//				fpf.flowProperty.category = null;
//				fpf.flowProperty.unitGroup = null;
//			});
//		});
//		return result;
//	}

//	public FlowDescriptor detailWithRefId(String flowRefId) {
//		return dao.getDescriptorForRefId(flowRefId);
//	}
//
//	public Flow create(NewFlowRequest request) {
//		Flow newFlow = Flow.of(
//				request.name,
//				request.flowType,
//				new FlowPropertyDao(dao.getDatabase()).getForId(request.referenceFlowProperty)
//		);
//
//		newFlow.description = request.description;
//		newFlow.category = new CategoryDao(dao.getDatabase()).getForId(request.category);
//
//		Flow result = dao.insert(newFlow);
//
//		result.category = null;
//
//		return result;
//	}
//
//	public Flow create2(Flow flow) {
//		return dao.insert(flow);
//	}
//
//	public List<Flow> find(String search) {
//		throw new UnsupportedOperationException("Not implemented yet");
//	}

	public void insertBulk(Map<Long, LcaFlowJson> lcaFlowsMap) {
		List<LcaFlowJson> cjl = new ArrayList<>(lcaFlowsMap.values()).stream().filter(f -> !f.isEcoinventFlow).toList();
		String sqlStmt = "insert into tbl_flows(id, ref_id, name, f_category, flow_type,infrastructure_flow,f_reference_flow_property) values (?, ?, ?, ?, ?, ?, ?)";
		NativeSql.on(dao.getDatabase()).batchInsert(
				sqlStmt,
				cjl.size(),
				(i, statement) -> {
					LcaFlowJson flow = cjl.get(i);
					statement.setLong(1, flow.id);
					statement.setString(2, flow.refId);
					statement.setString(3, flow.name);
					if (flow.category == null) statement.setNull(4, java.sql.Types.BIGINT);
					else statement.setLong(4, flow.category);
					statement.setString(5, flow.flowType.toString());
					statement.setBoolean(6, flow.infrastructureFlow);
					statement.setLong(7, flow.rfpId);
					return true;
				}
		);
	}

	public void deleteBulk(List<Long> ids) {
		String sqlStmt = "DELETE FROM tbl_flows WHERE id IN " + NativeSql.asList(new HashSet<>(ids));
		NativeSql.on(dao.getDatabase()).runUpdate(sqlStmt);
	}
//
//	public boolean delete(Long flowId) {
//		try {
//			dao.delete(flowId);
//			return true;
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//			return false;
//		}
//	}
//
//	public boolean deleteAllInCategory(List<Category> categories) {
//		try {
//			List<Long> catIds = categories.stream().map(c -> c.id).toList();
//
//			Map<String, Object> parameters = new HashMap<>();
//			parameters.put("categoryIds", catIds);  // Example category IDs
//			List<Flow> flows = dao.getAll("SELECT f FROM Flow f WHERE f.category.id IN :categoryIds ", parameters);
//			dao.deleteAll(flows);
//			return true;
//		} catch (
//				Exception e) {
//			System.out.println(e.getMessage());
//			return false;
//		}
//	}
//
//	public boolean deleteAll(List<Long> flowIds) {
//		try {
//			Set<Long> list = Set.of();
//			flowIds.forEach(id -> list.add(id));
//			list.forEach(id -> {
//				dao.delete(id);
//
//			});
//			return true;
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//			return false;
//		}
//	}


	public static FlowService of(IDatabase db) {
		return new FlowService(db);
	}


}
