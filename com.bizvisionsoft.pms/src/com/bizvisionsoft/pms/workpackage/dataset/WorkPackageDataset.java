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
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkPackageDataset {

	@Inject
	private BruiAssemblyContext context;
	
	@Inject
	private IBruiService br;

	private IWorkPackageMaster master;

	private TrackView view;

	@Init
	private void init() {
		Object[] input = (Object[]) context.getParentContext().getInput();
		master = (IWorkPackageMaster) input[0];
		view = (TrackView) input[1];
	}

	@DataSet(DataSet.INSERT)
	private WorkPackage insert(@MethodParam(MethodParam.OBJECT) WorkPackage wp) {
		return Services.get(WorkService.class).insertWorkPackage(wp, br.getDomain());
	}

	@DataSet(DataSet.DELETE)
	private long delete(@MethodParam(MethodParam._ID) ObjectId _id) {
		return Services.get(WorkService.class).deleteWorkPackage(_id, br.getDomain());
	}

	@DataSet(DataSet.UPDATE)
	private long update(BasicDBObject filterAndUpdate) {
		return Services.get(WorkService.class).updateWorkPackage(filterAndUpdate, br.getDomain());
	}

	@DataSet(DataSet.LIST)
	private List<WorkPackage> list(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		appendFilter(filter);
		if(master.isTemplate()) {
			return Services.get(WorkService.class).listWorkInTemplatePackage(condition, br.getDomain());
		}else {
			return Services.get(WorkService.class).listWorkPackage(condition, br.getDomain());
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
	private long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		appendFilter(filter);
		return Services.get(WorkService.class).countWorkPackage(filter, br.getDomain());
	}

}
