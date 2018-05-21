package com.bizvisionsoft.pms.workpackage.dataset;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkPackageDataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private Work work;

	@Init
	private void init() {
		work = (Work) context.getParentContext().getInput();
	}

	@DataSet(DataSet.INSERT)
	private WorkPackage insert(@ServiceParam(ServiceParam.OBJECT) WorkPackage wp) {
		return Services.get(WorkService.class).insertWorkPackage(wp);
	}

	@DataSet(DataSet.DELETE)
	private long delete(@ServiceParam(ServiceParam._ID) ObjectId _id) {
		return Services.get(WorkService.class).deleteWorkPackage(_id);
	}

	@DataSet(DataSet.UPDATE)
	private long update(BasicDBObject filterAndUpdate) {
		return Services.get(WorkService.class).updateWorkPackage(filterAndUpdate);
	}

	@DataSet(DataSet.LIST)
	private List<WorkPackage> listBasic(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			ObjectId work_id) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		appendFilter(filter);
		return Services.get(WorkService.class).listWorkPackage(condition);
	}

	private void appendFilter(BasicDBObject filter) {
		filter.append("work_id", work.get_id());
		TrackView tv = (TrackView) context.getInput();
		if(tv!=null) {
			filter.append("catagory", tv.getCatagory()).append("name", tv.getName());
		}else {
			filter.append("catagory", null).append("name", null);
		}
	}

	@DataSet(DataSet.COUNT)
	private long countBasic(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter, ObjectId work_id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		appendFilter(filter);
		return Services.get(WorkService.class).countWorkPackage(filter);
	}

}
