package com.awesometech.leoco.action;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class SelectOA {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];
		//OA流程选择器   用户选择器
		new Selector(br.getAssembly("OA流程选择器.selectorassy"), context).setTitle("选择需要关联的流程").open(r -> {
			Document row = (Document)r.get(0);
			String inst_ID = row.get("INST_ID").toString();
			
			@SuppressWarnings("unchecked")
			List<String> instList = (List<String>) tv.getParameter("WF_INSTS");
			if(null == instList) {
				instList = new ArrayList<String>();
			}
			if(!instList.contains(inst_ID)) {
				instList.add(inst_ID);
			}
			
			Services.get(WorkService.class)
					.updateWork(new FilterAndUpdate()
							.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id",
									tv.get_id()))
							.set(new BasicDBObject("workPackageSetting.$.parameter",
									new BasicDBObject("WF_INSTS", instList)))
							.bson(), br.getDomain());
			//////////////////////////////////////////////////
			// 刷新表格
			tv.setParameter("WF_INSTS", instList);
			GridPart grid = (GridPart) context.getChildContextByAssemblyName("工作包-OA流程.gridassy").getContent();
			grid.setViewerInput();
		});
		
	}

}
