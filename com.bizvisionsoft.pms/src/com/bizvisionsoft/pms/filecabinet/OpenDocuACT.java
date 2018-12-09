package com.bizvisionsoft.pms.filecabinet;

import org.bson.Document;

import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.UniversalDataService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class OpenDocuACT {

	@Inject
	private IBruiService brui;

	// 已经把模板和项目的分开，没有必要用Behavior设置，所以注释以下代码
	// @Behavior("输出文档/创建工作包文档模板设置")
	// private boolean behaviour(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT)
	// Object input) {
	// return input instanceof ProjectTemplate;
	// }

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Docu docu,
			@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.CONTEXT_CONTENT) GridPart part) {
		UniversalCommand getCmd = new UniversalCommand().setTargetCollection("docu").addParameter(MethodParam._ID,
				docu.get_id());
		UniversalResult ur1 = Services.get(UniversalDataService.class).get(getCmd);
		Document doc = (Document) ur1.getValue();
		// 注意此处必须是使用传入doc, 不做编辑clone
		// TODO 需要修改deepClone的方法解决该问题。
		Editor.open(docu.getEditorName(), context, doc, true, (r, t) -> {
			r.remove("_id");
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", t.get("_id"))).set(r)
					.bson();
			UniversalCommand updCmd = new UniversalCommand().setTargetClassName(Docu.class.getName())
					.setTargetCollection("docu").addParameter(MethodParam.FILTER_N_UPDATE, fu);
			UniversalResult ur2 = Services.get(UniversalDataService.class).update(updCmd);
			Object newValue = ur2.getValue();
			part.replaceItem(docu, newValue);
		});

	}

}
