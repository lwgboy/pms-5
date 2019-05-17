package com.bizvisionsoft.pms.obs.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class RemoveOBSMember extends AbstractChangeOBSItemMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,
			@MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (br.confirm("�Ƴ�", "��ȷ�Ͻ�Ҫ����Ŀ�Ŷ����Ƴ�ѡ��ĳ�Ա��")) {
			OBSItem obsItem = (OBSItem) context.getInput();
			String userId = ((User) em).getUserId();
			if (checkChange(br, obsItem.get_id(), obsItem.getScope_id(), userId, "removeobsitemmember@" + userId,
					"�Ƴ��Ŷӳ�Ա")) {
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", obsItem.get_id()))
						.update(new BasicDBObject("$pull", new BasicDBObject("member", userId))).bson();
				Services.get(OBSService.class).update(fu);
				GridPart grid = (GridPart) context.getContent();
				grid.remove(em);
			}
		}
	}
}
