package com.awesometech.leoco.ds;

import java.util.ArrayList;
import java.util.Arrays;
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
		Document prItem = (Document) context.getInput();
		String inst_ID = prItem.getString("INST_ID");
		List<String> curNodeList  = new ArrayList<String>();
		new SqlQuery("ecology").sql("select cur_node_id as CURRENTNODE from V_PMS_wf_inst where id = '" + inst_ID + "'").forEach(d -> {
			if( null != d.getString("CURRENTNODE") && !"".equals(d.getString("CURRENTNODE"))) {
				String curNodes = d.getString("CURRENTNODE");
				Arrays.asList(curNodes.split(",")).forEach(n -> {
					curNodeList.add(n);
				});
			}
		});
		
		List<Document> list = new ArrayList<Document>();
		new SqlQuery("ecology").sql("select inst.wf_id ,node.id as id,node.text as text,'ffffff' as foreground  from V_PMS_wf_node node,V_PMS_wf_inst inst where node.wf_id = inst.wf_id and inst.id = '" + inst_ID + "' ")
			.changeKeyCase(true).forEach(n -> {
			List curList = curNodeList;
			if(null != n.getString("id") && curList.contains(n.getString("id"))) {
				n.append("background", "b0120a");
			}else {
				n.append("background", "455a64");
			}
			list.add(n);
		});
		return list;
	}

	@DataSet("link")
	private List<Document> links() {
		Document prItem = (Document) context.getInput();
		String inst_ID = prItem.getString("INST_ID");
		return new SqlQuery("ecology").sql("select inst.wf_id as WF_ID, link.src as SRC, link.tgt as TGT from V_PMS_wf_link link,V_PMS_wf_inst inst where inst.wf_id = link.wf_id and inst.id = '" + inst_ID + "'").changeKeyCase(true)
				.into(new ArrayList<>());
	}

}
