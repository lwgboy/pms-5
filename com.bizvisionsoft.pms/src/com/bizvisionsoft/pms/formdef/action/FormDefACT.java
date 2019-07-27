package com.bizvisionsoft.pms.formdef.action;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.Model;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.pms.formdef.EditExportDocRuleDialog;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class FormDefACT {

	private static final String FORMDEF_EDITORASSY = "vault/������༭��.editorassy";

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
	}

	private void doActivate(Object element) {
		if (element instanceof FormDef) {
			FormDef formDef = (FormDef) element;
			if (formDef.get_id() == null)
				return;

			if (!formDef.isActivated()) {

				Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
				if (formDAssy == null) {
					Layer.error("�޷���ȡ��������ѡ�༭��");
					return;
				}

				Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

				boolean error = false;
				boolean warning = false;
				List<Result> result = Services.get(SystemService.class).formDefCheck(formDFieldMap, formDef.get_id(), br.getDomain());
				StringBuffer sb = new StringBuffer();
				for (Result r : result) {
					BasicDBObject data = r.data;
					switch (r.type) {
					case Result.TYPE_ERROR:
						error = true;
						if ("nullField".equals(data.getString("type"))) {
							sb.append("<span class='layui-badge'>����</span>: ");
							sb.append(data.getString("editorId"));
							sb.append(" �༭��  ");
							sb.append(r.message);
							sb.append("<br>");
						} else if ("errorSameField".equals(data.getString("type"))) {
							sb.append("<span class='layui-badge'>����</span>: ");
							sb.append(data.getString("editorId"));
							sb.append(" �༭��  ");
							sb.append(r.message);
							sb.append("�����ֶ����ֶ��������ظ�.");
							sb.append("<br>");
						} else if ("errorCompleteField".equals(data.getString("type"))) {
							sb.append("<span class='layui-badge'>����</span>: ");
							sb.append(data.getString("editorId"));
							sb.append(" �༭��  ");
							sb.append(r.message);
							sb.append("�����ֶ����ֶ�����δ�������ͺ�ֵ.");
							sb.append("<br>");
						} else if ("errorField".equals(data.getString("type"))) {
							sb.append("<span class='layui-badge'>����</span>: ");
							sb.append(data.getString("editorId"));
							sb.append(" �༭��  ");
							sb.append(r.message);
							sb.append("�ֶ�δ�ڱ��ж��壬�޷������ĵ�.");
							sb.append("<br>");
						} else if ("errorExportableField".equals(data.getString("type"))) {
							sb.append("<span class='layui-badge'>����</span>: ");
							sb.append(data.getString("editorId"));
							sb.append(" �༭��  ");
							sb.append(r.message);
							sb.append("�ֶ��޷��������ļ�.");
							sb.append("<br>");
						}
					case Result.TYPE_WARNING:
						warning = true;
						sb.append("<span class='layui-badge layui-bg-blue'>����</span>:");
						sb.append(data.getString("editorId"));
						sb.append(" �༭��  ");
						sb.append(r.message);
						sb.append("���е��ֶ�δ�ҵ��ĵ�ӳ���ֶ�.");
						sb.append("<br>");
					}
				}
				if (error) {
					MessageDialog.openError(br.getCurrentShell(), "��������", sb.toString());
					return;
				} else if (warning)
					if (!MessageDialog.openQuestion(br.getCurrentShell(), "��������", sb.toString() + "<br>�Ƿ������"))
						return;
			}
			Services.get(CommonService.class).updateFormDef(new FilterAndUpdate().filter(new BasicDBObject("_id", formDef.get_id()))
					.set(new BasicDBObject("activated", !formDef.isActivated())).bson(), br.getDomain());
			formDef.setActivated(!formDef.isActivated());
			viewer.update(formDef, null);
		}
	}

	private void doUpgrade(Object element) {
		if (element instanceof FormDef) {
			Services.get(CommonService.class).upgradeFormDef(((FormDef) element).get_id(), br.getDomain());
			((IQueryEnable) context.getContent()).doRefresh();
		}
	}

	/**
	 * ����
	 * 
	 * @param element
	 */
	private void doCreate(Object element) {
		EditExportDocRuleDialog dialog = EditExportDocRuleDialog.create(context, ((FormDef) element).newSubItem());
		if (IDialogConstants.OK_ID == dialog.open()) {
			service.insertExportDocRule(dialog.getExportDocRule(), br.getDomain());
			((IQueryEnable) context.getContent()).doRefresh();
		}
	}

	/**
	 * ɾ��
	 * 
	 * @param element
	 */
	private void doDelete(Object element) {
		String label = AUtil.readTypeAndLabel(element);
		String message = Optional.ofNullable(label).map(m -> "��ȷ�Ͻ�Ҫɾ�� " + m).orElse("��ȷ�Ͻ�Ҫɾ��ѡ��ļ�¼��");
		if (MessageDialog.openConfirm(br.getCurrentShell(), "ɾ��", message)) {
			if (element instanceof FormDef)
				service.deleteFormDef(((FormDef) element).get_id(), br.getDomain());
			else
				service.deleteExportDocRule(((ExportDocRule) element).get_id(), br.getDomain());

			((IQueryEnable) context.getContent()).doRefresh();
			Layer.message("��ɾ��");
		}
	}

	/**
	 * �༭
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
			EditExportDocRuleDialog dialog = EditExportDocRuleDialog.create(context, (ExportDocRule) element);
			if (IDialogConstants.OK_ID == dialog.open()) {
				ExportDocRule exportDocRule = dialog.getExportDocRule();
				service.updateExportDocRule(new FilterAndUpdate().filter(new BasicDBObject("_id", ((ExportDocRule) element).get_id()))
						.set(BsonTools.getBasicDBObject(exportDocRule, "_id")).bson(), br.getDomain());
				viewer.update(AUtil.simpleCopy(exportDocRule, element), null);
			}
		}

	}

	/**
	 * ���ƴ�����ť�ܷ�༭
	 * 
	 * @param element
	 * @return
	 */
	@Behavior({ "create", "upgrade", "activate" })
	private boolean enable(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef;
	}
}
