package com.bizvisionsoft.pms.resourcetype.action;

import java.util.HashSet;
import java.util.Set;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class AddResource {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context) {
		ResourceType element = (ResourceType) context.getFristElement();
		if (ResourceType.TYPE_HR.equals(element.getType())) {
			addHR(element, context);
		} else if (ResourceType.TYPE_ER.equals(element.getType())) {
			addDR(element, context);
		}
	}

	private void addDR(ResourceType rt, IBruiContext context) {
		new Selector(bruiService.getAssembly("设备设施选择器"), context).setTitle("添加设备设施资源").open(r -> {
			final Set<Object> ids = new HashSet<Object>();
			r.forEach(a -> ids.add(((Equipment) a).get_id()));
			if (!ids.isEmpty()) {
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", new BasicDBObject("$in", ids)))
						.set(new BasicDBObject("resourceType_id", rt.get_id())).bson();
				Services.get(CommonService.class).updateEquipment(fu);
				GridPart grid = (GridPart) context.getContent();
				grid.refresh(rt);
			}
		});
	}

	private void addHR(ResourceType rt, IBruiContext context) {
		new Selector(bruiService.getAssembly("用户选择器"), context).setTitle("添加人力资源").open(r -> {
			final Set<String> ids = new HashSet<String>();
			r.forEach(a -> ids.add(((User) a).getUserId()));
			if (!ids.isEmpty()) {
				BasicDBObject fu = new FilterAndUpdate()
						.filter(new BasicDBObject("userId", new BasicDBObject("$in", ids)))
						.set(new BasicDBObject("resourceType_id", rt.get_id())).bson();
				Services.get(UserService.class).update(fu);
				GridPart grid = (GridPart) context.getContent();
				grid.refresh(rt);
			}
		});
	}

}
