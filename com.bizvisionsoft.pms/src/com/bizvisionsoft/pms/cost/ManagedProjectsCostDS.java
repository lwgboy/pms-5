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
		// �ӳɱ��������Ŀ�ɱ�����ʱ������contextInputΪCBSItem
		if (input instanceof CBSItem) {
			scope_id = ((CBSItem) input).getScope_id();

		} else {
			// ��Ŀ���׶δ���Ŀ�ɱ�����ʱ���contextRootInput��ȡ����Ĳ���
			if (input == null) {
				input = context.getRootInput();
			}
			if (input != null) {
				ICBSScope cbsScope = (ICBSScope) input;
				scope_id = cbsScope.getScope_id();
			}
		}
	}

	@DataSet({ "�ɱ�����/" + DataSet.LIST })
	@Deprecated
	private List<CBSItem> listProject(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		return Services.get(CBSService.class).listProjectCost(condition, userId,br.getDomain());
	}

	@DataSet({ "Ԥ��ɱ��Աȷ���/" + DataSet.LIST })
	private List<CBSItem> listProjectCostAnalysis(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		// TODO �޸�Ϊֱ�ӵ��÷���
		return Services.get(CBSService.class).listProjectCostAnalysis(condition, userId, br.getDomain());
	}

	@DataSet({ "�ɱ�����/" + DataSet.COUNT, "Ԥ��ɱ��Աȷ���/" + DataSet.COUNT })
	private long countProject(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		// TODO �޸�Ϊֱ�ӵ��÷���
		return Services.get(CBSService.class).countProjectCost(filter, userId, br.getDomain());
	}

	@DataSet({ "��Ŀ�ɱ�����/" + DataSet.LIST, "��Ŀ�ɱ������鿴��/" + DataSet.LIST, "��ĿԤ��ɱ��Աȷ���/" + DataSet.LIST })
	private List<CBSItem> listCBSItemCost() {
		// TODO �޸�Ϊֱ�ӵ��÷���
		return Services.get(CBSService.class).getScopeRoot(scope_id, br.getDomain());
	}

}
