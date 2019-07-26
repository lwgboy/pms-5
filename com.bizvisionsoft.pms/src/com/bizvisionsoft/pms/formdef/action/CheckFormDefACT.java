package com.bizvisionsoft.pms.formdef.action;

import java.util.List;
import java.util.Map;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.Model;
import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class CheckFormDefACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_INPUT_OBJECT) Object element) {
		if (element instanceof FormDef) {
			FormDef formDef = (FormDef) element;
			if (formDef.get_id() == null)
				return;
			Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
			if (formDAssy == null) {
				Layer.error("�޷���ȡ��������ѡ�༭��");
				return;
			}

			Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

			List<Result> result = Services.get(SystemService.class).formDefCheck(formDFieldMap, formDef.get_id(), br.getDomain());
			String content = result.stream().map(r -> {
				BasicDBObject data = r.data;
				switch (r.type) {
				case Result.TYPE_ERROR:
					if ("nullField".equals(data.getString("type"))) {
						return "<span class='layui-badge'>����</span>: " + data.getString("editorId") + " �༭��  " + r.message + "<br>";
					} else if ("errorSameField".equals(data.getString("type"))) {
						return "<span class='layui-badge'>����</span>: " + data.getString("editorId") + " �༭��  " + r.message + "�����ֶ����ֶ��������ظ�."
								+ "<br>";
					} else if ("errorCompleteField".equals(data.getString("type"))) {
						return "<span class='layui-badge'>����</span>: " + data.getString("editorId") + " �༭��  " + r.message
								+ "�����ֶ����ֶ�����δ�������ͺ�ֵ." + "<br>";
					} else if ("errorField".equals(data.getString("type"))) {
						return "<span class='layui-badge'>����</span>: " + data.getString("editorId") + " �༭��  " + r.message
								+ "�ֶ�δ�ڱ��ж��壬�޷������ĵ�." + "<br>";
					} else if ("errorExportableField".equals(data.getString("type"))) {
						return "<span class='layui-badge'>����</span>: " + data.getString("editorId") + " �༭��  " + r.message + "�ֶ��޷��������ļ�."
								+ "<br>";
					}
				case Result.TYPE_WARNING:
					return "<span class='layui-badge layui-bg-blue'>����</span>:" + data.getString("editorId") + " �༭��  " + r.message
							+ "���е��ֶ�δ�ҵ��ĵ�ӳ���ֶ�." + "<br>";
				}
				return "";
			}).reduce(String::concat).orElse(null);
			if (content != null) {
				Layer.alert("��������", content, 600, 400, false);
			} else {
				Layer.message("��ͨ����顣");
			}
		}
	}
}
