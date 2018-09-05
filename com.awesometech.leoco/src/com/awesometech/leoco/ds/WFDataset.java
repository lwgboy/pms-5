package com.awesometech.leoco.ds;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlQuery;

public class WFDataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet("node")
	private List<Document> nodes() {
		String wf_id = "WF01";
		return new SqlQuery("oa").sql("select * from wf_node where wf_id='" + wf_id + "'").changeKeyCase(true)
				.into(new ArrayList<>());
	}

	@DataSet("link")
	private List<Document> links() {
		String wf_id = "WF01";
		return new SqlQuery("oa").sql("select * from wf_link where wf_id='" + wf_id + "'").changeKeyCase(true)
				.into(new ArrayList<>());
	}

}
