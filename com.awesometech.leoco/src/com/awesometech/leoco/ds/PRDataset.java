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
	private IBruiService br;

	@DataSet(DataSet.LIST)
	private List<Document> list() {

		Object[] data = (Object[]) context.getParentContext().getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) data[0];
		TrackView view = (TrackView) data[1];
		String so = (String) view.getParameter("so_num");
		List<Document> result = new ArrayList<>();
		if (so == null) {
			return result;
		}

		ObjectId work_id = work.get_id();
		String catagory = view.getCatagory();
		String name = view.getName();
		///////////////////////////////////////////////////////
		// 获取已记录的备料数量
		String sql = buildSql(so);
		new SqlQuery("ecology").sql(sql).forEach(d -> {
			String pr_num = d.getString("PR_NUM");
			String pr_idx = d.getString("PR_IDX");
			BasicDBObject filter = new BasicDBObject("work_id", work_id).append("catagory", catagory)
					.append("name", name).append("info.pr_num", pr_num).append("info.pr_idx", pr_idx);
			List<WorkPackage> wps = Services.get(WorkService.class)
					.listWorkPackage(new BasicDBObject("filter", filter), br.getDomain());
			if (!wps.isEmpty()) {
				d.put("_id", wps.get(0).get_id());
				d.put("completeQty", wps.get(0).info.get("completeQty"));
			}
			result.add(d);
		});

		return result;
	}

	private String buildSql(String so) {
		StringBuffer sb = new StringBuffer();
		sb.append( "select * from V_PMS_SO_PR_PO_IN where so_num='" + so + "'");
//		sb.append(
//				"select a.so_num,a.pr_num,a.pr_idx,a.prt_num,a.prt_desc,a.unit,a.qty,sum(b.qty) poqty,sum(b.aqty) as aqty, a.rdate ");
//		sb.append("from V_PMS_SO_PR as a left join ( ");
//		
//		sb.append("Select c.pr_num,c.pr_idx,c.po_num,c.po_idx,c.prt_num,c.qty,sum(d.qty) aqty ");
//		sb.append(" from V_PMS_PR_PO as c left join V_PMS_PO_IN as d ");
//		sb.append("on c.po_num = d.po_num and c.po_idx = d.po_idx and c.prt_num = d.prt_num ");
//		sb.append("group by c.pr_num,c.pr_idx,c.po_num,c.po_idx,c.prt_num,c.qty) as b ");
//		
//		sb.append("on (a.pr_num = b.pr_num and a.pr_idx = b.pr_idx) where so_num='" + so
//				+ "' group by a.so_num,a.pr_num,a.pr_idx,a.prt_num,a.prt_desc,a.unit,a.qty,a.rdate ");
		return sb.toString();
	}

}
