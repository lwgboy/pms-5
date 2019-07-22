package com.bizvisionsoft.pms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruiengine.assembly.exporter.ExportableFormBuilder;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class Activator implements BundleActivator {

	private Consumer<String> aml;
	private Consumer<String> acl;
	private Consumer<String> adl;

	@Override
	public void start(BundleContext context) throws Exception {
		// ע��༭���޸ĸ���
		aml = editorId -> {
			try {
				doModifiyAssembly(editorId);
			} catch (Exception e) {
				logger.error("Ĭ�ϱ������ô���  : " + editorId, e);
			}
		};
		ModelLoader.addAssemblyModifiyListener(aml);

		// ע��༭����������
		acl = this::doCreateAssembly;
		ModelLoader.addAssemblyCreateListener(acl);

		// ע��༭��ɾ������
		adl = this::doDeleteAssembly;
		ModelLoader.addAssemblyDeleteListener(adl);
		;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// ȡ���༭������
		ModelLoader.removeAssemblyModifiyListener(aml);
	}

	static Logger logger = LoggerFactory.getLogger(Activator.class);

	private void doCreateAssembly(String editorId) {
		// TODO��Ҫ�жϵ�ǰ�����Ƿ�Ϊ���°汾�ı༭��
		Services.get(SystemService.class).checkFormDAfterCreate(editorId);
	}

	public void doModifiyAssembly(String editorId) throws Exception {
		// ��ȡĬ�ϱ�������
		ExportableForm buildForm = ExportableFormBuilder.buildForm(editorId);
		Document exportableForm = BsonTools.encodeDocument(buildForm);
		// TODO ��Ҫ�жϵ�ǰ�����Ƿ�Ϊ���°汾�ı༭��
		Services.get(SystemService.class).checkFormDAfterModifiy(exportableForm, editorId);
	}

	private void doDeleteAssembly(String editorId) {
		// TODO ��Ҫ�жϵ�ǰ�����Ƿ�Ϊ���°汾�ı༭��
		Services.get(SystemService.class).checkFormDAfterDelete(editorId);
	}
}
