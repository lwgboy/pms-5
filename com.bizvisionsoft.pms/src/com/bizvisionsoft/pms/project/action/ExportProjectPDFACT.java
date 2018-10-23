package com.bizvisionsoft.pms.project.action;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.service.model.Project;

public class ExportProjectPDFACT {

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = context.search_sele_root(Project.class);
		if (project == null)
			// TODO 提示
			return;

		// TODO 使用JQ读取
		String rptParam = new Document("pipeline", "{'$match':{'_id':{'$oid':'" + project.get_id().toString() + "'}}},"
				+ "{'$lookup':{'from':'organization','localField':'impUnit_id','foreignField':'_id','as':'organization'}},"
				+ "{'$unwind':{'path':'$organization','preserveNullAndEmptyArrays':true}},"
				+ "{'$addFields':{'impUnitOrgFullName':'$organization.name'}},"
				+ "{'$lookup':{'from':'eps','localField':'eps_id','foreignField':'_id','as':'eps'}},"
				+ "{'$unwind':{'path':'$eps','preserveNullAndEmptyArrays':true}},"
				+ "{'$addFields':{'epsName':'$eps.name'}}," + "{'$lookup':{'from':'user','let':{'userId':'$pmId'},"
				+ "'pipeline':[{'$match':{'$expr':{'$eq':['$$userId','$userId']}}},"
				+ "{'$project':{'_id':false,'userId':true,'name':true,'headPics':true,'position':true,'tel':true,'org_id':true,'email':true}}],'as':'pmInfo_meta'}},"
				+ "{'$unwind':{'path':'$pmInfo_meta','preserveNullAndEmptyArrays':true}},"
				+ "{'$lookup':{'from':'organization','localField':'pmInfo_meta.org_id','foreignField':'_id','as':'temp_Org'}},"
				+ "{'$addFields':{'pmInfo':{'$cond':['$pmInfo_meta.name','$pmInfo_meta.name','']},"
				+ "'pmInfo_meta.orgInfo':{'$arrayElemAt':['$temp_Org.fullName',0.0]}}},"
				+ "{'$project':{'temp_Org':false,'organization':false,'eps':false}}").toJson();

		// TODO 导出后，不会自动的为fileName添加后缀
		JsonObject jo = new JsonObject().set("rptParam", rptParam).set("template", "jz_project.rptdesign")
				.set("outputType", "pdf").set("fileName", "立项申请表.pdf");
		UserSession.bruiToolkit().downloadServerFile("report", jo);
	}
}
