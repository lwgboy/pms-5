package com.bizvisionsoft.pms.formdef.action;

import java.util.HashMap;
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
						// sb.append(" �༭�������������ֶ�����");
					} else if ("errorCompleteField".equals(data.getString("type"))) {
						String message = map.get("errorCompleteField");
						if (message == null)
							map.put("errorCompleteField", message);
						else
							message += ",";
						message += data.getString("editorId");
						// sb.append(" �༭���ֶ������д���δ�������ͺ�ֵ���ֶ�");
					} else if ("errorField".equals(data.getString("type"))) {
						String message = map.get("errorField");
						if (message == null)
							map.put("errorField", message);
						else
							message += ",";
						message += data.getString("editorId");
						// sb.append(" �༭���ֶ������д��ڱ�������û�е��ֶΣ��޷������ĵ�.");
					} else if ("errorExportableField".equals(data.getString("type"))) {
						String message = map.get("errorExportableField");
						if (message == null)
							map.put("errorExportableField", message);
						else
							message += ",";
						message += data.getString("editorId");
						// sb.append(" �༭���ֶ������뵼�����ò�һ��,�޷��������ļ�.");
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
				sb.append("<span class='layui-badge'>����</span><br/>");
				for (String key : map.keySet()) {
					if ("nullField".equals(key)) {
						sb.append("�༭��: ");
						sb.append(map.get(key));
						sb.append(" �ֶ������д���δȷ���ֶ������ֶ�.<br/>");
					} else if ("errorSameField".equals(key)) {
						sb.append("�༭��: ");
						sb.append(map.get(key));
						sb.append(" �����������ֶ�����.<br/>");
					} else if ("errorCompleteField".equals(key)) {
						sb.append("�༭��: ");
						sb.append(map.get(key));
						sb.append(" �ֶ������д���δ�������ͺ�ֵ���ֶ�.<br/>");
					} else if ("errorField".equals(key)) {
						sb.append("�༭��: ");
						sb.append(map.get(key));
						sb.append(" �ֶ������д��ڱ�������û�е��ֶΣ��޷������ĵ�.<br/>");
					} else if ("errorExportableField".equals(key)) {
						sb.append("�༭��: ");
						sb.append(map.get(key));
						sb.append(" �ֶ������뵼�����ò�һ��,�޷��������ļ�.<br/>");
					}
				}
				br.error("��������", sb.toString());
				return;
			} else if (warning) {
				StringBuffer sb = new StringBuffer();
				sb.append("<span class='layui-badge layui-bg-blue'>����</span><br/>�༭��: ");
				sb.append(map.get("warning"));
				sb.append(" �������е��ֶ�δ�ҵ��ĵ�ӳ���ֶ�.");
				Layer.message("��������", sb.toString());
			} else
				Layer.message("��ͨ����顣");
		}
	}
}
