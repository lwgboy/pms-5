package com.awesometech.leoco.ds;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.bizvisionsoft.sqldb.SqlQuery;
import com.mongodb.BasicDBObject;

public class OADataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet(DataSet.LIST)
	private List<Document> list() {
		Object[] data = (Object[]) context.getParentContext().getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) data[0];
		TrackView view = (TrackView) data[1];
		List instList = (List) view.getParameter("WF_INSTS");
		List<Document> result = new ArrayList<>();
		if (instList == null) {
			return result;
		}

		///////////////////////////////////////////////////////
		String sql = buildSql(instList);
		new SqlQuery("oa").sql(sql).forEach(d -> {
			result.add(d);
		});

		return result;
	}
	
	@DataSet(DataSet.DELETE)
	private long delete(@MethodParam(MethodParam._ID) ObjectId _id, @MethodParam(MethodParam.OBJECT) Object selected) {
		Object[] data = (Object[]) context.getParentContext().getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) data[0];
		TrackView view = (TrackView) data[1];
		List instList = (List) view.getParameter("WF_INSTS");
		
		Document row = (Document)selected;
		if(null != row && null != row.getString("INST_ID") && null != instList && instList.contains(row.getString("INST_ID"))) {
			instList.remove(row.getString("INST_ID"));
			Services.get(WorkService.class)
				.updateWork(new FilterAndUpdate()
					.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id",
							view.get_id()))
					.set(new BasicDBObject("workPackageSetting.$.parameter",
							new BasicDBObject("WF_INSTS", instList)))
					.bson());
			//////////////////////////////////////////////////
			// 刷新表格
			//	tv.setParameter("so_num", so_num);
			GridPart grid = (GridPart) context.getChildContextByAssemblyName("工作包-OA流程").getContent();
			grid.setViewerInput();
		}
		return 0l;
	}
	

	private String buildSql(List instList) {
		StringBuffer sb = new StringBuffer();
		sb.append("select inst.id as inst_id,wf.type_name,wf.wf_name,inst.inst_name,inst.status,inst.create_date,inst.creater from  wf wf,wf_inst inst " );
		sb.append(" where wf.id = inst.wf_id ");
		sb.append(" and inst.id in ('" + StringUtils.join(instList.toArray(), "','") + "')");
		return sb.toString();
	}

}
