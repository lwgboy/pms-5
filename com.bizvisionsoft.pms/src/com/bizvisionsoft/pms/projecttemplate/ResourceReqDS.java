package com.bizvisionsoft.pms.projecttemplate;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ResourcePlanInTemplate;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ResourceReqDS {

	@Inject
	private IBruiService br;

	@DataSet(DataSet.LIST)
	private List<ResourcePlanInTemplate> listResourcePlan() {
		return new ArrayList<ResourcePlanInTemplate>();
	}

	@DataSet(DataSet.COUNT)
	private long countResourcePlan() {
		return 0;
	}

	@DataSet(DataSet.UPDATE)
	private long updateResourcePlan(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate) {
		return Services.get(ProjectTemplateService.class).updateResourcePlan(filterAndUpdate, br.getDomain());
	}

	@DataSet(DataSet.DELETE)
	private long delete(@MethodParam(MethodParam.OBJECT) ResourcePlanInTemplate rp) {
		ObjectId work_id = rp.getWork_id();
		String resId = rp.getUsedHumanResId();
		if (resId != null) {
			return Services.get(ProjectTemplateService.class).deleteHumanResourcePlan(work_id, resId, br.getDomain());
		}
		resId = rp.getUsedEquipResId();
		if (resId != null) {
			return Services.get(ProjectTemplateService.class).deleteEquipmentResourcePlan(work_id, resId, br.getDomain());
		}
		resId = rp.getUsedTypedResId();
		if (resId != null) {
			return Services.get(ProjectTemplateService.class).deleteTypedResourcePlan(work_id, resId, br.getDomain());
		}

		return 0;
	}
}
