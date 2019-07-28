package com.bizvisionsoft.pms.formdef.action;

import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.pms.formdef.EditExportDocRuleDialog;
import com.bizvisionsoft.pms.formdef.FormDefTools;
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
		else if ("upgrade".equals(a.getName()) || "upgrade".equals(e.text))
			doUpgrade(element);
		else if ("activate".equals(a.getName()) || "activate".equals(e.text))
			doActivate(element);
		else if ("inactivate".equals(a.getName()) || "inactivate".equals(e.text))
			doInactivate(element);
	}

	private void doInactivate(Object element) {
		if (element instanceof FormDef) {
			FormDef formDef = (FormDef) element;
			if (formDef.get_id() == null)
				return;
			Services.get(CommonService.class).updateFormDef(new FilterAndUpdate().filter(new BasicDBObject("_id", formDef.get_id()))
					.set(new BasicDBObject("activated", false)).bson(), br.getDomain());
			formDef.setActivated(false);
			viewer.refresh(formDef);
		}
	}

	private void doActivate(Object element) {
		if (element instanceof FormDef) {
			FormDef formDef = (FormDef) element;
			if (formDef.get_id() == null)
				return;

			if (FormDefTools.checkFormDef(br, formDef)) {
				Services.get(CommonService.class).updateFormDef(new FilterAndUpdate().filter(new BasicDBObject("_id", formDef.get_id()))
						.set(new BasicDBObject("activated", true)).bson(), br.getDomain());
				formDef.setActivated(true);
				viewer.update(formDef, null);
				viewer.refresh(formDef);
			}
		}
	}

	private void doUpgrade(Object element) {
		if (element instanceof FormDef) {
			FormDef formDef = Services.get(CommonService.class).upgradeFormDef(((FormDef) element).get_id(), br.getDomain());
			((GridPart) context.getContent()).insert(formDef);
			viewer.refresh(formDef);
			Layer.message("表单定义已升版。");
		}
	}

	/**
	 * 创建
	 * 
	 * @param element
	 */
	private void doCreate(Object element) {
		EditExportDocRuleDialog dialog = EditExportDocRuleDialog.create(context, ((FormDef) element).newSubItem(), br);
		if (IDialogConstants.OK_ID == dialog.open()) {
			service.insertExportDocRule(dialog.getExportDocRule(), br.getDomain());
			((IQueryEnable) context.getContent()).doRefresh();
		}
	}

	/**
	 * 删除
	 * 
	 * @param element
	 */
	private void doDelete(Object element) {
		String label = AUtil.readTypeAndLabel(element);
		String message = Optional.ofNullable(label).map(m -> "请确认将要删除 " + m).orElse("请确认将要删除选择的记录。");
		if (br.confirm("删除", message)) {
			try {
				if (element instanceof FormDef)
					service.deleteFormDef(((FormDef) element).get_id(), br.getDomain());
				else
					service.deleteExportDocRule(((ExportDocRule) element).get_id(), br.getDomain());

				((IQueryEnable) context.getContent()).doRefresh();
				Layer.message("已删除");
			} catch (Exception e) {
				Layer.error(e);
			}
		}
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
			EditExportDocRuleDialog dialog = EditExportDocRuleDialog.create(context, (ExportDocRule) element, br);
			if (IDialogConstants.OK_ID == dialog.open()) {
				ExportDocRule exportDocRule = dialog.getExportDocRule();
				service.updateExportDocRule(new FilterAndUpdate().filter(new BasicDBObject("_id", ((ExportDocRule) element).get_id()))
						.set(BsonTools.getBasicDBObject(exportDocRule, "_id")).bson(), br.getDomain());
				viewer.update(AUtil.simpleCopy(exportDocRule, element), null);
			}
		}

	}

	/**
	 * 控制创建按钮
	 * 
	 * @param element
	 * @return
	 */
	@Behavior({ "create", "upgrade" })
	private boolean enable(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef;
	}

	/**
	 * 控制创建按钮
	 * 
	 * @param element
	 * @return
	 */
	@Behavior("activate")
	private boolean enableActivate(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef && !Boolean.TRUE.equals(((FormDef) element).isActivated());
	}

	/**
	 * 控制创建按钮
	 * 
	 * @param element
	 * @return
	 */
	@Behavior("inactivate")
	private boolean enableInactivate(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef && Boolean.TRUE.equals(((FormDef) element).isActivated());
	}

	/**
	 * 控制删除按钮
	 * 
	 * @param element
	 * @return
	 */
	@Behavior({ "delete", "edit" })
	private boolean enableActivated(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return (element instanceof FormDef && !((FormDef) element).isActivated())
				|| (element instanceof ExportDocRule && !((ExportDocRule) element).getFormDef().isActivated());
	}

}
