package com.awesometech.leoco.ds;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlQuery;

public class PODataset {
	
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	@DataSet(DataSet.LIST)
	private List<Document> list() {
		Document prItem = (Document) context.getInput();
		String pr_num = prItem.getString("PR_NUM");
		String pr_idx = prItem.getString("PR_IDX");
		return new SqlQuery("ecology").sql("select * from V_PMS_PR_PO where pr_num='" + pr_num + "' and pr_idx='"+pr_idx+"'").into(new ArrayList<>());
	}

}
