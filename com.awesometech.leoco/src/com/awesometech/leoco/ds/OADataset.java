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
		String inst_id = (String) view.getParameter("oa_wf_inst_id");
		List<Document> result = new ArrayList<>();
//		if (inst_id == null) {
//			return result;
//		}

		///////////////////////////////////////////////////////
		String sql = buildSql(inst_id);
		new SqlQuery("oa").sql(sql).forEach(d -> {
			result.add(d);
		});

		return result;
	}

	private String buildSql(String inst_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("select inst.id as inst_id,wf.type_name,wf.wf_name,inst.inst_name,inst.status,inst.create_date,inst.creater from  wf wf,wf_inst inst " );
		sb.append(" where wf.id = inst.wf_id ");
//		sb.append(" and inst.id = '" + inst_id + "'");
		return sb.toString();
	}

}
