package com.bizvisionsoft.pms.work.dataset;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ResourceDataset {

	@DataSet(DataSet.LIST)
	private List<ResourcePlan> listResourcePlan() {
		return new ArrayList<ResourcePlan>();
	}

	@DataSet(DataSet.COUNT)
	private long countResourcePlan() {
		return 0;
	}

	@DataSet(DataSet.UPDATE)
	private long updateResourcePlan(BasicDBObject filterAndUpdate) {
		return Services.get(WorkService.class).updateResourcePlan(filterAndUpdate);
	}

	@DataSet(DataSet.DELETE)
	private long delete(@ServiceParam(ServiceParam.OBJECT) ResourcePlan rp) {
		ObjectId work_id = rp.getWork_id();
		String resId = rp.getUsedHumanResId();
		if (resId != null) {
			return Services.get(WorkService.class).deleteHumanResourcePlan(work_id, resId);
		}
		resId = rp.getUsedEquipResId();
		if (resId != null) {
			return Services.get(WorkService.class).deleteEquipmentResourcePlan(work_id, resId);
		}
		resId = rp.getUsedTypedResId();
		if (resId != null) {
			return Services.get(WorkService.class).deleteTypedResourcePlan(work_id, resId);
		}

		return 0;
	}
}
