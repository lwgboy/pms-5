package com.bizvisionsoft.pms.cost;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ManagedProjectsCostDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private CBSItem cbsItem;

	@Init
	private void init() {
		cbsItem = (CBSItem) context.getInput();
	}

	@DataSet("成本管理/" + DataSet.LIST)
	private List<CBSItem> listProject(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		return Services.get(CBSService.class).listProjectCost(condition);
	}

	@DataSet("成本管理/" + DataSet.COUNT)
	private long countProject(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter) {
		return Services.get(CBSService.class).countProjectCost(filter);
	}

	@DataSet("项目成本管理/" + DataSet.LIST)
	private List<CBSItem> listCBSItemCost() {
		return Services.get(CBSService.class).getScopeRoot(cbsItem.getScope_id());
	}
}
