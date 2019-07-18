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

	private static final String ExportDocDef_EDITORASSY = "vault/文档导出规则编辑器.editorassy";

	private static final String FormDef_EDITORASSY = "vault/表单定义编辑器.editorassy";

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
	 * 创建
	 * 
	 * @param element
	 */
	private void doCreate(Object element) {
		if (element instanceof FormDef) {
			openEditor(ExportDocDef_EDITORASSY, ((FormDef) element).newSubItem(), false);
		} else if (element instanceof ExportDocRule) {
			ExportDocRule edr = (ExportDocRule) element;
			// 创建不同类型的生成文档字段规则
			new ActionMenu(br).setActions(Arrays.asList(
					//
					new ActionFactory().name("value").text("字段值").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "value").append("_id", new ObjectId()).append("parent_id",
								edr.get_id());
						openEditor("vault/生成文档字段规则-字段值编辑器.editorassy", doc, false);
					}).get(),
					//
					new ActionFactory().name("string").text("字符串常量").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "constant").append("_id", new ObjectId())
								.append("parent_id", edr.get_id()).append("valueType", "string");
						openEditor("vault/生成文档字段规则-字符串编辑器.editorassy", doc, false);
					}).get(),
					//
					new ActionFactory().name("boolean").text("布尔常量").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "constant").append("_id", new ObjectId())
								.append("parent_id", edr.get_id()).append("valueType", "boolean");
						openEditor("vault/生成文档字段规则-布尔编辑器.editorassy", doc, false);
					}).get(),
					//
					new ActionFactory().name("listString").text("字符串数组常量").normalStyle().exec((e, c) -> {
						Document doc = new Document().append("type", "constant").append("_id", new ObjectId())
								.append("parent_id", edr.get_id()).append("valueType", "listString");
						openEditor("vault/生成文档字段规则-字符串数组编辑器.editorassy", doc, false);
					}).get()
			// TODO 没考虑好文档数组常量如何编辑
			// new ActionFactory().name("listDocument").text("文档数组常量").infoStyle().exec((e,
			// c) -> {}).get()
			)).open();

		} else {
			FormDef fd = new FormDef();
			((FormDef) fd).domain = br.getDomain();
			openEditor(FormDef_EDITORASSY, fd, false);
		}

	}

	/**
	 * 打开编辑器
	 * 
	 * @param editorId
	 *            编辑器id
	 * @param element
	 *            编辑器打开的对象
	 * @param edit
	 *            是否为编辑
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
				// 获取默认报表设置
				ExportDocRule edr = (ExportDocRule) d;
				try {
					ExportableForm buildForm = ExportableFormBuilder.buildForm(edr.getEditorId());
					edr.setExportableForm(BsonTools.encodeDocument(buildForm));
				} catch (IOException e) {
					br.error("编辑", "获取默认报表设置错误。");
				}

				if (edit)
					service.updateExportDocRule(new FilterAndUpdate().filter(new BasicDBObject("_id", edr.get_id()))
							.set(BsonTools.getBasicDBObject(edr, "_id")).bson(), br.getDomain());
				else
					service.insertExportDocRule(edr, br.getDomain());
			} else if (d instanceof Document) {
				Document doc = (Document) d;
				// TODO 应该有更好的处理方式， 将表格的内容转换成字符串数组
				if ("listString".equals(doc.get("valueType"))) {
					List<Document> value = (List<Document>) doc.get("value");
					List<String> result = new ArrayList<String>();
					value.forEach(v -> result.add(v.getString("name")));
					doc.put("value", result);
				}
				// 获取生成文档字段规则所在的导出规则_id
				ObjectId edr_id = doc.getObjectId("parent_id");
				ExportDocRule edr = service.getExportDocRule(edr_id, br.getDomain());
				List<Document> fieldMap = edr.getFieldMap();

				// 修改ExportDocRule的fieldMap字段
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
	 * 删除
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
	 * 编辑
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
				openEditor("vault/生成文档字段规则-字段值编辑器.editorassy", doc, true);
			} else if ("constant".equals(type)) {
				String valueType = doc.getString("valueType");
				if ("string".equals(valueType))
					openEditor("vault/生成文档字段规则-字符串编辑器.editorassy", doc, true);
				else if ("boolean".equals(valueType))
					openEditor("vault/生成文档字段规则-布尔编辑器.editorassy", doc, true);
				else if ("listString".equals(valueType)) {
					// TODO 应该有更好的处理方式，将字符串数组转换成document，用于编辑器打开
					List<String> value = (List<String>) doc.get("value");
					List<Document> result = new ArrayList<Document>();
					value.forEach(v -> result.add(new Document("name", v)));
					doc.put("value", result);

					openEditor("vault/生成文档字段规则-字符串数组编辑器.editorassy", doc, true);
				}
			}
		}

	}

	/**
	 * 控制创建按钮能否编辑
	 * 
	 * @param element
	 * @return
	 */
	@Behavior("create")
	private boolean enableCreate(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element) {
		return element instanceof FormDef || element instanceof ExportDocRule;
	}
}
