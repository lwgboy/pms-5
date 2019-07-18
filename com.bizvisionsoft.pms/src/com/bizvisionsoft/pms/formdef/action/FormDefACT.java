package com.bizvisionsoft.pms.formdef.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.assembly.exporter.ExportableFormBuilder;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class FormDefACT {

	private static final String ExportDocDef_EDITORASSY = "vault/�ĵ���������༭��.editorassy";

	private static final String FormDef_EDITORASSY = "vault/������༭��.editorassy";

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
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		if ("edit".equals(a.getName()) || "edit".equals(e.text))
			doEdit(element);
		else if ("delete".equals(a.getName()) || "delete".equals(e.text))
			doDelete(element);
		else if ("create".equals(a.getName()) || "create".equals(e.text))
			doCreate(element);
	}

	/**
	 * ����
	 * 
	 * @param element
	 */
	private void doCreate(Object element) {
		if (element instanceof FormDef) {
			openEditor(ExportDocDef_EDITORASSY, ((FormDef) element).newSubItem(), false);
		} else if (element instanceof ExportDocRule) {
			ExportDocRule edr = (ExportDocRule) element;
			// ������ͬ���͵������ĵ��ֶι���
			new ActionMenu(br).setActions(Arrays.asList(
					//
					new ActionFactory().name("value").text("�ֶ�ֵ").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "value").append("_id", new ObjectId()).append("parent_id",
								edr.get_id());
						openEditor("vault/�����ĵ��ֶι���-�ֶ�ֵ�༭��.editorassy", doc, false);
					}).get(),
					//
					new ActionFactory().name("string").text("�ַ�������").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "constant").append("_id", new ObjectId())
								.append("parent_id", edr.get_id()).append("valueType", "string");
						openEditor("vault/�����ĵ��ֶι���-�ַ����༭��.editorassy", doc, false);
					}).get(),
					//
					new ActionFactory().name("boolean").text("��������").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "constant").append("_id", new ObjectId())
								.append("parent_id", edr.get_id()).append("valueType", "boolean");
						openEditor("vault/�����ĵ��ֶι���-�����༭��.editorassy", doc, false);
					}).get(),
					//
					new ActionFactory().name("listString").text("�ַ������鳣��").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "constant").append("_id", new ObjectId())
								.append("parent_id", edr.get_id()).append("valueType", "listString");
						openEditor("vault/�����ĵ��ֶι���-�ַ�������༭��.editorassy", doc, false);
					}).get()
			// TODO û���Ǻ��ĵ����鳣����α༭
			// new ActionFactory().name("listDocument").text("�ĵ����鳣��").infoStyle().exec((e,
			// c) -> {}).get()
			)).open();

		} else {
			FormDef fd = new FormDef();
			((FormDef) fd).domain = br.getDomain();
			openEditor(FormDef_EDITORASSY, fd, false);
		}

	}

	/**
	 * �򿪱༭��
	 * 
	 * @param editorId
	 *            �༭��id
	 * @param element
	 *            �༭���򿪵Ķ���
	 * @param edit
	 *            �Ƿ�Ϊ�༭
	 */
	@SuppressWarnings("unchecked")
	private void openEditor(String editorId, Object element, boolean edit) {
		Editor.open(editorId, context, element, (r, d) -> {
			if (d instanceof FormDef)
				if (edit)
					service.updateFormDef(new FilterAndUpdate().filter(new BasicDBObject("_id", ((FormDef) d).get_id()))
							.set(BsonTools.getBasicDBObject((FormDef) d, "_id")).bson(), br.getDomain());
				else
					service.insertFormDef((FormDef) d, br.getDomain());
			else if (d instanceof ExportDocRule) {
				// ��ȡĬ�ϱ�������
				ExportDocRule edr = (ExportDocRule) d;
				try {
					ExportableForm buildForm = ExportableFormBuilder.buildForm(edr.getEditorId());
					edr.setExportableForm(BsonTools.encodeDocument(buildForm));
				} catch (IOException e) {
					br.error("�༭", "��ȡĬ�ϱ������ô���");
				}

				if (edit)
					service.updateExportDocRule(new FilterAndUpdate().filter(new BasicDBObject("_id", edr.get_id()))
							.set(BsonTools.getBasicDBObject(edr, "_id")).bson(), br.getDomain());
				else
					service.insertExportDocRule(edr, br.getDomain());
			} else if (d instanceof Document) {
				Document doc = (Document) d;
				// TODO Ӧ���и��õĴ���ʽ�� ����������ת�����ַ�������
				if ("listString".equals(doc.get("valueType"))) {
					List<Document> value = (List<Document>) doc.get("value");
					List<String> result = new ArrayList<String>();
					value.forEach(v -> result.add(v.getString("name")));
					doc.put("value", result);
				}
				// ��ȡ�����ĵ��ֶι������ڵĵ�������_id
				ObjectId edr_id = doc.getObjectId("parent_id");
				ExportDocRule edr = service.getExportDocRule(edr_id, br.getDomain());
				List<Document> fieldMap = edr.getFieldMap();

				// �޸�ExportDocRule��fieldMap�ֶ�
				if (edit) {
					fieldMap.forEach(field -> {
						if (doc.get("_id").equals(field.get("_id"))) {
							field.putAll(doc);
						}
					});
				} else {
					if (fieldMap == null) {
						fieldMap = new ArrayList<Document>();
						edr.setFieldMap(fieldMap);
					}
					fieldMap.add(doc);
				}
				service.updateExportDocRule(new FilterAndUpdate().filter(new BasicDBObject("_id", edr.get_id()))
						.set(BsonTools.getBasicDBObject(edr, "_id")).bson(), br.getDomain());

			}
			if (edit)
				viewer.update(AUtil.simpleCopy(d, element), null);
			else
				((IQueryEnable) context.getContent()).doRefresh();
		});
	}

	/**
	 * ɾ��
	 * 
	 * @param element
	 */
	private void doDelete(Object element) {
		if (element instanceof FormDef)
			service.deleteFormDef(((FormDef) element).get_id(), br.getDomain());
		else if (element instanceof ExportDocRule)
			service.deleteExportDocRule(((ExportDocRule) element).get_id(), br.getDomain());

		((IQueryEnable) context.getContent()).doRefresh();
	}

	/**
	 * �༭
	 * 
	 * @param element
	 */
	@SuppressWarnings("unchecked")
	private void doEdit(Object element) {
		if (element instanceof FormDef)
			openEditor(FormDef_EDITORASSY, element, true);
		else if (element instanceof ExportDocRule)
			openEditor(ExportDocDef_EDITORASSY, element, true);
		else if (element instanceof Document) {
			Document doc = (Document) element;
			String type = doc.getString("type");
			if ("value".equals(type)) {
				openEditor("vault/�����ĵ��ֶι���-�ֶ�ֵ�༭��.editorassy", doc, true);
			} else if ("constant".equals(type)) {
				String valueType = doc.getString("valueType");
				if ("string".equals(valueType))
					openEditor("vault/�����ĵ��ֶι���-�ַ����༭��.editorassy", doc, true);
				else if ("boolean".equals(valueType))
					openEditor("vault/�����ĵ��ֶι���-�����༭��.editorassy", doc, true);
				else if ("listString".equals(valueType)) {
					// TODO Ӧ���и��õĴ���ʽ�����ַ�������ת����document�����ڱ༭����
					List<String> value = (List<String>) doc.get("value");
					List<Document> result = new ArrayList<Document>();
					value.forEach(v -> result.add(new Document("name", v)));
					doc.put("value", result);

					openEditor("vault/�����ĵ��ֶι���-�ַ�������༭��.editorassy", doc, true);
				}
			}
		}

	}

	/**
	 * ���ƴ�����ť�ܷ�༭
	 * 
	 * @param element
	 * @return
	 */
	@Behavior("create")
	private boolean enableCreate(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef || element instanceof ExportDocRule;
	}
}
