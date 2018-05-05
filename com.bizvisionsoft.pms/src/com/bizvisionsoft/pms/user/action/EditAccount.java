package com.bizvisionsoft.pms.user.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditAccount extends EditUser{

	@Inject
	private IBruiService bruiService;

	@Execute
	private void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(elem -> {
			UserService service = Services.get(UserService.class);
			User user = service.get(((User) elem).getUserId());
			Editor.open("ÕË»§±à¼­Æ÷", context, user, true, (r, t) -> {
				FilterAndUpdate filterAndUpdate = new FilterAndUpdate()
						.filter(new BasicDBObject("userId", user.getUserId())).set(r);
				if (service.update(filterAndUpdate.bson()) == 1) {
					GridPart grid = (GridPart) context.getContent();
					grid.replaceItem(elem, t);
				}
			});
		});
		
	}

}
