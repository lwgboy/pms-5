package com.bizvisionsoft.pms.formdef.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruiengine.assembly.exporter.ExportableFormBuilder;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class FormDefTool {

	static Logger logger = LoggerFactory.getLogger(FormDefTool.class);

	public static void doCreateAssembly(String editorId) {
		// �����༭��id����ñ༭�����id
		String editorTypeId;
		Pattern editorIdText = Pattern.compile("_(v|V)(.*?)(.editorassy)");
		Matcher matcher = editorIdText.matcher(editorId);
		if (matcher.find()) {
			editorTypeId = editorId.replaceAll(matcher.group(), "");
		} else {
			editorTypeId = editorId.replaceAll(".editorassy", "");
		}
		// ��ȡdomain
		List<String> domains = new ArrayList<String>();
		Services.get(SystemService.class).listDomain(new Query().bson()).forEach(domain -> domains.add(domain._id));

		// ���ݱ༭�����id���±༭��id
		CommonService commonService = Services.get(CommonService.class);
		domains.forEach(domain -> commonService.updateFormDef(new FilterAndUpdate().filter(new BasicDBObject("editorTypeId", editorTypeId))
				.set(new BasicDBObject("editorId", editorId)).bson(), domain));

	}

	public static void doModifiyAssembly(String editorId) {
		try {
			// ��ȡĬ�ϱ�������
			ExportableForm buildForm = ExportableFormBuilder.buildForm(editorId);
			Document exportableForm = BsonTools.encodeDocument(buildForm);

			// ��ȡdomain
			List<String> domains = new ArrayList<String>();
			Services.get(SystemService.class).listDomain(new Query().bson()).forEach(domain -> domains.add(domain._id));

			// ���ݱ༭��id���±�������
			CommonService commonService = Services.get(CommonService.class);
			domains.forEach(
					domain -> commonService.updateExportDocRule(new FilterAndUpdate().filter(new BasicDBObject("editorId", editorId))
							.set(new BasicDBObject("exportableForm", exportableForm)).bson(), domain));

		} catch (IOException e) {
			logger.error("Ĭ�ϱ������ô���  : " + editorId, e);
		}
	}

}
