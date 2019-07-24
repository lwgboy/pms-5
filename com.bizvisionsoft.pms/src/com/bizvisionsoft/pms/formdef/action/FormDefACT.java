package com.bizvisionsoft.pms.formdef.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.pms.formdef.EditExportDocRuleDialog;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class FormDefACT {

	private static final String FORMDEF_EDITORASSY = "vault/表单定义编辑器.editorassy";

	private CommonService service;

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;
	private GridTreeViewer viewer;

	@Init
	private void init() {
		service = Services.get(CommonService.class);
		viewer = (GridTreeViewer) context.getContent("viewer");
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		if ("edit".equals(a.getName()) || "edit".equals(e.text))
			doEdit(element);
		else if ("delete".equals(a.getName()) || "delete".equals(e.text))
			doDelete(element);
		else if ("create".equals(a.getName()) || "create".equals(e.text))
			doCreate(element);
	}

	/**
	 * 创建
	 * 
	 * @param element
	 */
	private void doCreate(Object element) {
		ExportDocRule exportDocRule = ((FormDef) element).newSubItem();
		if (IDialogConstants.OK_ID == EditExportDocRuleDialog.open(br, context, exportDocRule)) {
			service.insertExportDocRule(exportDocRule, br.getDomain());
			((IQueryEnable) context.getContent()).doRefresh();
		}
	}

	/**
	 * 删除
	 * 
	 * @param element
	 */
	private void doDelete(Object element) {
		if (element instanceof FormDef)
			service.deleteFormDef(((FormDef) element).get_id(), br.getDomain());
		else
			service.deleteExportDocRule(((ExportDocRule) element).get_id(), br.getDomain());

		((IQueryEnable) context.getContent()).doRefresh();
	}

	/**
	 * 编辑
	 * 
	 * @param element
	 */
	private void doEdit(Object element) {
		if (element instanceof FormDef)
			Editor.open(FORMDEF_EDITORASSY, context, element, (r, d) -> {
				service.updateFormDef(new FilterAndUpdate().filter(new BasicDBObject("_id", ((FormDef) d).get_id()))
						.set(BsonTools.getBasicDBObject((FormDef) d, "_id")).bson(), br.getDomain());
				viewer.update(AUtil.simpleCopy(d, element), null);
			});
		else if (element instanceof ExportDocRule) {
			if (IDialogConstants.OK_ID == EditExportDocRuleDialog.open(br, context, (ExportDocRule) element)) {
				service.updateExportDocRule(new FilterAndUpdate().filter(new BasicDBObject("_id", ((ExportDocRule) element).get_id()))
						.set(BsonTools.getBasicDBObject((ExportDocRule) element, "_id")).bson(), br.getDomain());
				viewer.update(element, null);
			}
		}

	}

	/**
	 * 控制创建按钮能否编辑
	 * 
	 * @param element
	 * @return
	 */
	@Behavior("create")
	private boolean enableCreate(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef;
	}
}
