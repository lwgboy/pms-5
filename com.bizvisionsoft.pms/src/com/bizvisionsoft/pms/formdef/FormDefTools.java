package com.bizvisionsoft.pms.formdef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;

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

	public static boolean checkExportDocRule(IBruiService br, ExportDocRule exportDocRule, String title, String infoMessage,
			String confirmMessage) {
		Assembly formDAssy = Model.getAssembly(exportDocRule.getFormDef().getEditorId());
		if (formDAssy == null) {
			Layer.error("无法获取表单定义所选编辑器");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = exportDocRule.check(formDFieldMap);

		return showResult(br, result, title, infoMessage, confirmMessage);
	}

	public static boolean checkRefDef(IBruiService br, FormDef formDef, String title, String infoMessage, String confirmMessage) {
		Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
		if (formDAssy == null) {
			Layer.error("无法获取表单定义所选编辑器");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = new ArrayList<Result>();
		formDef.listRefDef().forEach(refDef -> result.addAll(refDef.check(formDFieldMap)));

		return showResult(br, result, title, infoMessage, confirmMessage);
	}

	public static boolean checkFormDef(IBruiService br, FormDef formDef, String title, String infoMessage, String confirmMessage) {
		Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
		if (formDAssy == null) {
			Layer.error("无法获取表单定义所选编辑器");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = formDef.check(formDFieldMap);

		return showResult(br, result, title, infoMessage, confirmMessage);
	}

	private static boolean showResult(IBruiService br, List<Result> results, String title, String infoMessage, String confirmMessage) {
		if (results.size() > 0) {

			boolean error = false;
			boolean warning = false;
			for (Result result : results) {
				switch (result.type) {
				case Result.TYPE_ERROR:
					error = true;
					break;
				case Result.TYPE_WARNING:
					warning = true;
					break;
				}
			}
			if (error) {
				InformationDialog.openInfo(br.getCurrentShell(), title, infoMessage, results);
				return false;
			}
			if (warning)
				return IDialogConstants.OK_ID == InformationDialog.openConfirm(br.getCurrentShell(), title, confirmMessage, results);
		}
		return true;
	}

}
