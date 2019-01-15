package com.bizvisionsoft.pms.project.action;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class FavoriteProjectACT {
	@Inject
	private IBruiService br;

	@SuppressWarnings("unchecked")
	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Project project, @MethodParam(Execute.CURRENT_USER_ID) String userId) {
		String name = "关注的项目";
		List<ObjectId> projectIds;
		// 获取当前用户关注的项目的配置
		Document setting = Services.get(SystemService.class).getClientSetting(userId, name);
		if (setting != null)// 如果用户没有配置过关注的项目，则获取的可能为null
			projectIds = (List<ObjectId>) setting.get("value");
		else
			projectIds = new ArrayList<ObjectId>();

		ObjectId project_id = project.get_id();
		String message;
		// 如果所选项目已经关注过，则取消关注；没有关注过，则关注该项目。
		if (projectIds.contains(project_id)) {
			projectIds.remove(project_id);
			message = "已取消项目： " + project + " 的关注。";
		} else {
			projectIds.add(project_id);
			message = "已关注项目： " + project + " 。";
		}
		// 添加当前用户关注的项目配置。添加到用户配置中。
		Services.get(SystemService.class)
				.updateClientSetting(new Document("userId", userId).append("name", name).append("value", projectIds));
		Layer.message(message);
	}
}
