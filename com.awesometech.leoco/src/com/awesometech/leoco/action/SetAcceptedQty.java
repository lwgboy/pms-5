package com.awesometech.leoco.action;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.InputDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.serviceconsumer.Services;

public class SetAcceptedQty {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Document prItem = (Document) context.getFirstElement();
		String pr_num = prItem.getString("PR_NUM");
		String pr_idx = prItem.getString("PR_IDX");
		ObjectId _id = prItem.getObjectId("_id");

		InputDialog id = new InputDialog(br.getCurrentShell(), "设置备料数量", "PR: " + pr_num + "[" + pr_idx + "] 备料数量：", null,
				t -> {
					if (t.trim().isEmpty()) {
						return "请输入数量";
					}
					try {
						double v = Double.parseDouble(t);
						if (v < 0) {
							return "数量不能小于零";
						}
					} catch (Exception e) {
						return "请输入数量";
					}
					return null;
				});
		if (InputDialog.OK == id.open()) {
			String input = id.getValue();
			try {
				Object[] data = (Object[]) context.getParentContext().getInput();
				IWorkPackageMaster work = (IWorkPackageMaster) data[0];
				TrackView tv = (TrackView) data[1];
				double qty = Double.parseDouble(input);
				ObjectId work_id = work.get_id();// 主关键字
				String catagory = tv.getCatagory();// 主关键字
				String name = tv.getName();
				Document info = new Document("pr_num", pr_num).append("pr_idx", pr_idx).append("completeQty", qty)
						.append("work_id", work_id).append("catagory", catagory).append("name", name)
						.append("_id", _id);
				_id = Services.get(WorkService.class).updateWorkPackageInfo(info, br.getDomain());
				prItem.put("_id", _id);
				prItem.put("completeQty", qty);
				GridPart grid = (GridPart) context.getContent();
				grid.update(prItem);
			} catch (Exception e) {
			}
		}
	}

}
