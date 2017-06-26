package com.bizvisionsoft.pms.filecabinet;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateDocuOfPackageACT {
	
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		WorkPackage wp = (WorkPackage) context.getInput();
		ObjectId project_id = Services.get(WorkService.class).getProjectId(wp.getWork_id());
		//ѡ���ļ���
		Selector.open("��Ŀ�ļ���ѡ��", context,project_id , em->{
			System.out.println(em);
		});
		
		Docu docu = new Docu().setCreationInfo(brui.creationInfo()).addWorkPackageId(wp.get_id());
		Editor.open("�������ĵ��༭��", context, docu,  (r, o) -> {
			
		});
	}

}
