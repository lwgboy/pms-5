package com.bizvisionsoft.pms.risk;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectRiskDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

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
	public List<RBSItem> listRootRBSItems(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		filter.append("project_id", project_id).append("parent_id", null);
		return Services.get(RiskService.class).listRBSItem(condition);
	}
	
	@DataSet(DataSet.COUNT)
	public long countRootRBSItem(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		filter.append("project_id", project_id).append("parent_id", null);
		return Services.get(RiskService.class).countRBSItem(filter);
	}

}
