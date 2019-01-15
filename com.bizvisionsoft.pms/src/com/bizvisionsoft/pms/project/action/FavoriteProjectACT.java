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
		String name = "��ע����Ŀ";
		List<ObjectId> projectIds;
		// ��ȡ��ǰ�û���ע����Ŀ������
		Document setting = Services.get(SystemService.class).getClientSetting(userId, name);
		if (setting != null)// ����û�û�����ù���ע����Ŀ�����ȡ�Ŀ���Ϊnull
			projectIds = (List<ObjectId>) setting.get("value");
		else
			projectIds = new ArrayList<ObjectId>();

		ObjectId project_id = project.get_id();
		String message;
		// �����ѡ��Ŀ�Ѿ���ע������ȡ����ע��û�й�ע�������ע����Ŀ��
		if (projectIds.contains(project_id)) {
			projectIds.remove(project_id);
			message = "��ȡ����Ŀ�� " + project + " �Ĺ�ע��";
		} else {
			projectIds.add(project_id);
			message = "�ѹ�ע��Ŀ�� " + project + " ��";
		}
		// ��ӵ�ǰ�û���ע����Ŀ���á���ӵ��û������С�
		Services.get(SystemService.class)
				.updateClientSetting(new Document("userId", userId).append("name", name).append("value", projectIds));
		Layer.message(message);
	}
}
