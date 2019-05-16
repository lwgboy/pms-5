package com.bizvisionsoft.pms.cbs.action;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
/**
 * 取消了CBS分解功能，该操作无效
 * @author gdiyang
 * @date 2018/10/27
 *
 */
@Deprecated
public class DistributeCBSBudget {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			new Selector(br.getAssembly("阶段选择器"), context).setInput(context.getRootInput()).setTitle("分配预算到指定阶段")
					.open(r -> {
						// TODO 在CBS节点上显示分配到哪个阶段
						// TODO 阶段选择器上显示分配情况
						// TODO 控制哪些预算可以分配
						// TODO 取消阶段的预算分配
						Work workInfo = (Work) r.get(0);
						ObjectId cbs_id = workInfo.getCBS_id();
						if (cbs_id != null) {
							br.error( "错误", "已经为该阶段分配预算，无法再次分配。");
						} else {
							CBSItem cbsItem = Services.get(CBSService.class).allocateBudget(((CBSItem) parent).get_id(),
									workInfo.get_id(), workInfo.toString(), br.getDomain());
							// TODO 错误返回
							// TODO 成功提示
							BudgetCBS grid = (BudgetCBS) context.getContent();
							grid.replaceItem(parent, cbsItem);
							grid.refresh(parent);
						}
					});

		});
	}

}
