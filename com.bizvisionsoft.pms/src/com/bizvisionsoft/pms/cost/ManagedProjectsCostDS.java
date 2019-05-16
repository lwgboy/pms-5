package com.bizvisionsoft.pms.cost;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ManagedProjectsCostDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private ObjectId scope_id;

	@Init
	private void init() {
		Object input = context.getInput();
		// 从成本管理打开项目成本管理时，传入contextInput为CBSItem
		if (input instanceof CBSItem) {
			scope_id = ((CBSItem) input).getScope_id();

		} else {
			// 项目、阶段打开项目成本管理时需从contextRootInput获取传入的参数
			if (input == null) {
				input = context.getRootInput();
			}
			if (input != null) {
				ICBSScope cbsScope = (ICBSScope) input;
				scope_id = cbsScope.getScope_id();
			}
		}
	}

	@DataSet({ "成本管理/" + DataSet.LIST })
	@Deprecated
	private List<CBSItem> listProject(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		return Services.get(CBSService.class).listProjectCost(condition, userId,br.getDomain());
	}

	@DataSet({ "预算成本对比分析/" + DataSet.LIST })
	private List<CBSItem> listProjectCostAnalysis(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		// TODO 修改为直接调用服务
		return Services.get(CBSService.class).listProjectCostAnalysis(condition, userId, br.getDomain());
	}

	@DataSet({ "成本管理/" + DataSet.COUNT, "预算成本对比分析/" + DataSet.COUNT })
	private long countProject(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		// TODO 修改为直接调用服务
		return Services.get(CBSService.class).countProjectCost(filter, userId, br.getDomain());
	}

	@DataSet({ "项目成本管理/" + DataSet.LIST, "项目成本管理（查看）/" + DataSet.LIST, "项目预算成本对比分析/" + DataSet.LIST })
	private List<CBSItem> listCBSItemCost() {
		// TODO 修改为直接调用服务
		return Services.get(CBSService.class).getScopeRoot(scope_id, br.getDomain());
	}

}
