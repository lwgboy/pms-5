package com.bizvisionsoft.pms.formdef.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	}

	private void doActivate(Object element) {
		if (element instanceof FormDef) {
			FormDef formDef = (FormDef) element;
			if (formDef.get_id() == null)
				return;

			if (!formDef.isActivated()) {
				// TODO 单独方法
				Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
				if (formDAssy == null) {
					Layer.error("无法获取表单定义所选编辑器");
					return;
				}

				Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

				boolean error = false;
				boolean warning = false;
				List<Result> result = Services.get(SystemService.class).formDefCheck(formDFieldMap, formDef.get_id(), br.getDomain());
				Map<String, String> map = new HashMap<String, String>();
				for (Result r : result) {
					BasicDBObject data = r.data;
					switch (r.type) {
					case Result.TYPE_ERROR:
						error = true;
						if ("nullField".equals(data.getString("type"))) {
							String message = map.get("nullField");
							if (message == null)
								map.put("nullField", message);
							else
								message += ",";
							message += data.getString("editorId");
						} else if ("errorSameField".equals(data.getString("type"))) {
							String message = map.get("errorSameField");
							if (message == null)
								map.put("errorSameField", message);
							else
								message += ",";
							message += data.getString("editorId");
						} else if ("errorCompleteField".equals(data.getString("type"))) {
							String message = map.get("errorCompleteField");
							if (message == null)
								map.put("errorCompleteField", message);
							else
								message += ",";
							message += data.getString("editorId");
						} else if ("errorField".equals(data.getString("type"))) {
							String message = map.get("errorField");
							if (message == null)
								map.put("errorField", message);
							else
								message += ",";
							message += data.getString("editorId");
						} else if ("errorExportableField".equals(data.getString("type"))) {
							String message = map.get("errorExportableField");
							if (message == null)
								map.put("errorExportableField", message);
							else
								message += ",";
							message += data.getString("editorId");
						}
						break;
					case Result.TYPE_WARNING:
						warning = true;
						String message = map.get("warning");
						if (message == null)
							map.put("errorExportableField", message);
						else
							message += ",";
						message += data.getString("editorId");
						break;
					}
				}
				if (error) {
					StringBuffer sb = new StringBuffer();
					sb.append("<span class='layui-badge'>错误</span><br/>");
					for (String key : map.keySet()) {
						if ("nullField".equals(key)) {
							sb.append("编辑器: ");
							sb.append(map.get(key));
							sb.append(" 字段设置中存在未确定字段名的字段.<br/>");
						} else if ("errorSameField".equals(key)) {
							sb.append("编辑器: ");
							sb.append(map.get(key));
							sb.append(" 存在重名的字段设置.<br/>");
						} else if ("errorCompleteField".equals(key)) {
							sb.append("编辑器: ");
							sb.append(map.get(key));
							sb.append(" 字段设置中存在未设置类型和值的字段.<br/>");
						} else if ("errorField".equals(key)) {
							sb.append("编辑器: ");
							sb.append(map.get(key));
							sb.append(" 字段设置中存在表单定义中没有的字段，无法导出文档.<br/>");
						} else if ("errorExportableField".equals(key)) {
							sb.append("编辑器: ");
							sb.append(map.get(key));
							sb.append(" 字段设置与导出配置不一致,无法导出到文件.<br/>");
						}
					}
					br.error("表单定义检查", sb.toString());
					return;
				} else if (warning) {
					StringBuffer sb = new StringBuffer();
					sb.append("<span class='layui-badge layui-bg-blue'>警告</span><br/>编辑器: ");
					sb.append(map.get("warning"));
					sb.append(" 表单定义中的字段未找到文档映射字段.");
					if (!br.confirm("表单定义检查", sb.toString() + "<br>是否继续？"))
						return;
				}
			}
			Services.get(CommonService.class).updateFormDef(new FilterAndUpdate().filter(new BasicDBObject("_id", formDef.get_id()))
					.set(new BasicDBObject("activated", !formDef.isActivated())).bson(), br.getDomain());
			formDef.setActivated(!formDef.isActivated());
			viewer.update(formDef, null);
			viewer.refresh(formDef);
		}
	}

	private void doUpgrade(Object element) {
		if (element instanceof FormDef) {
			Services.get(CommonService.class).upgradeFormDef(((FormDef) element).get_id(), br.getDomain());
			((IQueryEnable) context.getContent()).doRefresh();
			Layer.message("表单定义已升版。");
		}
	}

	/**
	 * 创建
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
	 * 控制创建按钮
	 * 
	 * @param element
	 * @return
	 */
	@Behavior({ "create", "upgrade", "activate" })
	private boolean enable(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef;
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
