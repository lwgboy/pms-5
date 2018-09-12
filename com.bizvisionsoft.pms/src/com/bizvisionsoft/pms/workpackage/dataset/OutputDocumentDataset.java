package com.bizvisionsoft.pms.workpackage.dataset;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class OutputDocumentDataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private boolean inTemplate;

	@Init
	private void init() {
		inTemplate = context.getRootInput() instanceof ProjectTemplate;
	}

	@DataSet(DataSet.INSERT)
	private WorkPackage insert(@MethodParam(MethodParam.OBJECT) WorkPackage wp) {
		return Services.get(WorkService.class).insertWorkPackage(wp);
	}

	@DataSet(DataSet.DELETE)
	private long delete(@MethodParam(MethodParam._ID) ObjectId _id) {
		return Services.get(WorkService.class).deleteWorkPackage(_id);
	}

	@DataSet(DataSet.UPDATE)
	private long update(BasicDBObject filterAndUpdate) {
		return Services.get(WorkService.class).updateWorkPackage(filterAndUpdate);
	}

	@DataSet(DataSet.LIST)
	private List<?> list(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId wp_id,
			@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		if (inTemplate) {
			return Services.get(DocumentService.class).listWorkPackageDocumentSetting(wp_id);
		} else {
			return Services.get(DocumentService.class).listWorkPackageDocument(wp_id);
		}
	}

}
