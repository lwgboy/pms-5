package com.bizvisionsoft.pms.formdef;

import java.util.List;
import java.util.Map;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.Model;
import com.bizvisionsoft.bruiengine.ui.InformationDialog;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.Result;

public class FormDefTools {

	public static boolean checkExportDocRule(IBruiService br, ExportDocRule exportDocRule) {
		Assembly formDAssy = Model.getAssembly(exportDocRule.getFormDef().getEditorId());
		if (formDAssy == null) {
			Layer.error("无法获取表单定义所选编辑器");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = exportDocRule.check(formDFieldMap);

		return showResult(br, result, "表单定义检查", "检查表单定义完整性");
	}

	public static boolean checkFormDef(IBruiService br, FormDef formDef) {
		Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
		if (formDAssy == null) {
			Layer.error("无法获取表单定义所选编辑器");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = formDef.check(formDFieldMap);

		return showResult(br, result, "导出文档规则检查", "检查导出文档规则完整性");
	}

	private static boolean showResult(IBruiService br, List<Result> results, String title, String message) {
		
		//TODO 修改弹出提示
		InformationDialog.openConfirm(br.getCurrentShell(), title, message, results);

		// boolean error = false;
		// boolean warning = false;
		// Map<String, String> map = new HashMap<String, String>();
		// for (Result r : result) {// 循环检查结果
		// BasicDBObject data = r.data;
		// switch (r.type) {
		// case Result.TYPE_ERROR:
		// error = true;
		// if ("nullField".equals(data.getString("type"))) {
		// String message = map.get("nullField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("nullField", message);
		// } else if ("errorSameField".equals(data.getString("type"))) {
		// String message = map.get("errorSameField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorSameField", message);
		// } else if ("errorField".equals(data.getString("type"))) {
		// String message = map.get("errorField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorField", message);
		// } else if ("errorExportableField".equals(data.getString("type"))) {
		// String message = map.get("errorExportableField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorExportableField", message);
		// }
		// break;
		// case Result.TYPE_WARNING:
		// warning = true;
		// String message = map.get("warning");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorExportableField", message);
		// break;
		// }
		// }
		// if (error) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("<span class='layui-badge'>错误</span><br/>");
		// for (String key : map.keySet()) {
		// if ("nullField".equals(key)) {
		// sb.append("文档导出规则: ");
		// sb.append(map.get(key));
		// sb.append(" 中存在字段名为空的字段。<br/>");
		// } else if ("errorSameField".equals(key)) {
		// sb.append("文档导出规则: ");
		// sb.append(map.get(key));
		// sb.append(" 存在重名的字段设置。<br/>");
		// } else if ("errorField".equals(key)) {
		// sb.append("文档导出规则: ");
		// sb.append(map.get(key));
		// sb.append(" 字段设置中存在表单定义中没有的字段，无法导出文档。<br/>");
		// } else if ("errorExportableField".equals(key)) {
		// sb.append("文档导出规则: ");
		// sb.append(map.get(key));
		// sb.append(" 字段设置与导出配置不一致,无法导出到文件。<br/>");
		// }
		// }
		// br.error("表单定义检查", sb.toString());
		// return false;
		// } else if (warning) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("<span class='layui-badge layui-bg-blue'>警告</span><br/>文档导出规则: ");
		// sb.append(map.get("warning"));
		// sb.append(" 表单定义中的字段没有完全映射到文档。");
		// return br.confirm("表单定义检查", sb.toString() + "<br>是否继续？");
		// }

		return true;
	}

}
