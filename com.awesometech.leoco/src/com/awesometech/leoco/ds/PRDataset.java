package com.awesometech.leoco.ds;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.bizvisionsoft.sqldb.SqlQuery;
import com.mongodb.BasicDBObject;

public class PRDataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet(DataSet.LIST)
	private List<Document> list() {
		
		Object[] data = (Object[]) context.getParentContext().getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) data[0];
		TrackView view = (TrackView)  data[1];
		String so = view.getTrackWorkOrder();
		List<Document> result = new ArrayList<>();
		if(so==null) {
			return result;
		}

		ObjectId work_id = work.get_id();
		String catagory = view.getCatagory();
		///////////////////////////////////////////////////////
		//获取已记录的备料数量
		
		new SqlQuery("erp").sql("select * from so_pr where so_num='" + so + "'").forEach(d->{
			String pr_num = d.getString("PR_NUM");
			String pr_idx = d.getString("PR_IDX");
			BasicDBObject filter = new BasicDBObject("work_id",work_id).append("catagory", catagory).append("info.pr_num", pr_num).append("info.pr_idx", pr_idx);
			List<WorkPackage> wps = Services.get(WorkService.class).listWorkPackage(new BasicDBObject("filter",filter));
			if(!wps.isEmpty()) {
				d.put("_id", wps.get(0).get_id());
				d.put("completeQty", wps.get(0).info.get("completeQty"));
			}
			result.add(d);
		});
		
		return result;
	}

}
