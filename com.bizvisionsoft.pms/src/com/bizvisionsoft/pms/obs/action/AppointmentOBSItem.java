package com.bizvisionsoft.pms.obs.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.mongodb.BasicDBObject;

@Deprecated
public class AppointmentOBSItem extends AbstractChangeOBSItemMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,
			@MethodParam(Execute.CONTEXT) IBruiContext context) {
		OBSItem element = (OBSItem) em;
		new Selector(br.getAssembly("用户选择器—单选"), context).setTitle("指定担任者").open(r -> {
			if (checkChange(br, element.get_id(), element.getScope_id(), element.getManagerId(), "appointmentobsitem",
					"指定担任者")) {
				element.setManager((User) r.get(0));
				String userId = ((User) r.get(0)).getUserId();
				BasicDBObject data = new BasicDBObject("_id", element.get_id()).append("managerId", userId);
				Check.instanceThen(context.getContent(), IStructuredDataPart.class,
						part -> part.doModify(element, element, data));
			}
		});
	}

}
