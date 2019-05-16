package com.bizvisionsoft.pms.risk;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.service.model.RiskResponse;
import com.bizvisionsoft.service.model.RiskResponseType;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectRiskDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private ObjectId project_id;

	@Init
	private void init() {
		Object input = context.getRootInput();
		if (input instanceof Project) {
			this.project_id = ((Project) input).get_id();
		} else if (input instanceof Work) {
			this.project_id = ((Work) input).getProject_id();
		} else {
			throw new RuntimeException("ProjectRiskDS数据集只能用于项目和工作的上下文");
		}
	}

	@DataSet(DataSet.LIST)
	public List<RBSItem> listRootRBSItems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		condition.append("sort", new BasicDBObject("rbsType.id", 1).append("index", 1));
		filter.append("project_id", project_id).append("parent_id", null);
		return Services.get(RiskService.class).listRBSItem(condition, br.getDomain());
	}

	@DataSet(DataSet.COUNT)
	public long countRootRBSItem(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", project_id).append("parent_id", null);
		return Services.get(RiskService.class).countRBSItem(filter, br.getDomain());
	}

	@DataSet({ "项目风险登记簿/" + DataSet.INSERT })
	public RBSItem insertRBSItem(@MethodParam(MethodParam.OBJECT) RBSItem item) {
		return Services.get(RiskService.class).insertRBSItem(item, br.getDomain());
	}

	@DataSet(DataSet.DELETE)
	private long delete(@MethodParam(MethodParam._ID) ObjectId _id, @MethodParam(MethodParam.OBJECT) Object selected) {
		if (selected instanceof RBSItem) {
			return Services.get(RiskService.class).deleteRBSItem(_id, br.getDomain());
		} else if (selected instanceof RiskEffect) {
			return Services.get(RiskService.class).deleteRiskEffect(_id, br.getDomain());
		} else if (selected instanceof RiskResponse) {
			return Services.get(RiskService.class).deleteRiskResponse(_id, br.getDomain());
		}
		return 0l;
	}

	@DataSet("项目风险应对计划/" + DataSet.INSERT)
	public RiskResponse insertRiskResponse(@MethodParam(MethodParam.PARENT_OBJECT) RiskResponseType type,
			@MethodParam(MethodParam.OBJECT) RiskResponse response) {
		return Services.get(RiskService.class)
				.insertRiskResponse(response.setType(type.getType()).setRBSItem_id(type.get_id()), br.getDomain());
	}

	@DataSet(DataSet.UPDATE)
	private long update(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.OBJECT) Object selected) {
		if (selected instanceof RBSItem)
			return Services.get(RiskService.class).updateRBSItem(filterAndUpdate, br.getDomain());
		else if (selected instanceof RiskEffect)
			return Services.get(RiskService.class).updateRiskEffect(filterAndUpdate, br.getDomain());
		else if (selected instanceof RiskResponse)
			return Services.get(RiskService.class).updateRiskResponse(filterAndUpdate, br.getDomain());
		return 0l;
	}

}
