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
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkPackageDataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private IWorkPackageMaster master;

	private TrackView view;

	@Init
	private void init() {
		Object[] input = (Object[]) context.getParentContext().getInput();
		master = (IWorkPackageMaster) input[0];
		view = (TrackView) input[1];
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
	private List<WorkPackage> list(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		appendFilter(filter);
		if(master.isTemplate()) {
			return Services.get(WorkService.class).listWorkInTemplatePackage(condition);
		}else {
			return Services.get(WorkService.class).listWorkPackage(condition);
		}
	}

	private void appendFilter(BasicDBObject filter) {
		filter.append("work_id", master.get_id());
		if(view!=null) {
			filter.append("catagory", view.getCatagory()).append("name", view.getName());
		}else {
			filter.append("catagory", null).append("name", null);
		}
	}

	@DataSet(DataSet.COUNT)
	private long count(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		appendFilter(filter);
		return Services.get(WorkService.class).countWorkPackage(filter);
	}

}
