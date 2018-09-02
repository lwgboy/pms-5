package com.awesometech.leoco.ds;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlQuery;

public class AOGDataset {
	
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet(DataSet.LIST)
	private List<Document> list() {
		Document prItem = (Document) context.getInput();
		String po_num = prItem.getString("PO_NUM");
		String po_idx = prItem.getString("PO_IDX");
		return new SqlQuery("erp").sql("select * from po_ai where po_num='" + po_num + "' and po_idx='"+po_idx+"'").into(new ArrayList<>());
	}

}
