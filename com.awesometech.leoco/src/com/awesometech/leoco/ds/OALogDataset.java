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

public class OALogDataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet(DataSet.LIST)
	private List<Document> list() {
		Document prItem = (Document) context.getInput();
		String inst_ID = prItem.get("INST_ID").toString();
		List<Document> result = new ArrayList<>();
		if (inst_ID == null) {
			return result;
		}

		///////////////////////////////////////////////////////
		String sql = buildSql(inst_ID);
		new SqlQuery("ecology").sql(sql).forEach(d -> {
			result.add(d);
		});

		return result;
	}

	private String buildSql(String inst_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("select inst_id,node_id,node_name,opr_dat,operator,tgt,comment from V_PMS_wf_log " );
		sb.append(" where inst_id = '" + inst_id + "'");
		return sb.toString();
	}

}
