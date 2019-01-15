package com.bizvisionsoft.pms.project.action;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class FavoriteProjectACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.CONTEXT_SELECTION_1ST) Project project,
			@MethodParam(Execute.CURRENT_USER_ID) String userId) {
		String name = "关注的项目";
		Document setting = Services.get(SystemService.class).getClientSetting(userId, name);
		List<ObjectId> projectIds;
		if (setting != null)
			projectIds = (List<ObjectId>) setting.get("value");
		else
			projectIds = new ArrayList<ObjectId>();

		ObjectId project_id = project.get_id();
		String message;
		if (projectIds.contains(project_id)) {
			projectIds.remove(project_id);
			message = "已取消项目： " + project + " 的关注。";
		} else {
			projectIds.add(project_id);
			message = "已关注项目： " + project + " 。";
		}
		Services.get(SystemService.class)
				.updateClientSetting(new Document("userId", userId).append("name", name).append("value", projectIds));
		Layer.message(message);
	}
}
