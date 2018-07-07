package com.bizvisionsoft.pms.filecabinet;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkPackage;
import com.mongodb.BasicDBObject;

public class ProjectDocuDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private ObjectId project_id;

	@Init
	private void init() {
		WorkPackage rootInput = (WorkPackage) context.getRootInput();
		if (rootInput != null) {
			ObjectId work_id = rootInput.getWork_id();
			Work work = ServicesLoader.get(WorkService.class).getWork(work_id);
			if (work != null)
				project_id = work.getProject_id();
		}
	}

	@DataSet("list")
	private List<Docu> listProjectDocument(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		if (project_id != null)
			return ServicesLoader.get(DocumentService.class).listProjectDocument(condition, project_id);
		else
			return new ArrayList<Docu>();
	}

	@DataSet("count")
	private long countProjectDocument(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		if (project_id != null)
			return ServicesLoader.get(DocumentService.class).countProjectDocument(filter, project_id);
		else
			return 0;
	}
}
