package com.bizvisionsoft.pms.workpackage.dataset;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkPackageProgressDataset {

	@Inject
	private BruiAssemblyContext context;

	private WorkPackage workPackage;
	
	@Inject
	private IBruiService br;

	@Init
	private void init() {
		workPackage = (WorkPackage) context.getInput();
	}

	@DataSet(DataSet.INSERT)
	private WorkPackageProgress insert(@MethodParam(MethodParam.OBJECT) WorkPackageProgress wp) {
		return Services.get(WorkService.class).insertWorkPackageProgress(wp.setPackage_id(workPackage.get_id()), br.getDomain());
	}

	@DataSet(DataSet.DELETE)
	private long delete(@MethodParam(MethodParam._ID) ObjectId _id) {
		return Services.get(WorkService.class).deleteWorkPackageProgress(_id, br.getDomain());
	}

	@DataSet(DataSet.UPDATE)
	private long update(BasicDBObject filterAndUpdate) {
		return Services.get(WorkService.class).updateWorkPackageProgress(filterAndUpdate, br.getDomain());
	}

	@DataSet(DataSet.LIST)
	private List<WorkPackageProgress> list(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			ObjectId work_id) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		appendFilter(filter);

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort == null) {
			sort = new BasicDBObject("updateTime", -1);
			condition.put("sort", sort);
		}

		return Services.get(WorkService.class).listWorkPackageProgress(condition, br.getDomain());
	}

	private void appendFilter(BasicDBObject filter) {
		filter.append("package_id", workPackage.get_id());
	}

	@DataSet(DataSet.COUNT)
	private long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter, ObjectId work_id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		appendFilter(filter);
		return Services.get(WorkService.class).countWorkPackageProgress(filter, br.getDomain());
	}

}
