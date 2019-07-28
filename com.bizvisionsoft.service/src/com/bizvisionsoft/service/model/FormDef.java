package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.tools.Check;
import com.mongodb.BasicDBObject;

@PersistenceCollection("formDef")
public class FormDef {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "表单定义";

	@ImageURL("editorId")
	@Exclude
	public static final String icon = "/img/form_c.svg";

	public String domain;

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	@ReadValue
	@WriteValue
	@Label
	private String name;

	@ReadValue
	@WriteValue
	private String editorId;

	@ReadValue
	@WriteValue
	private boolean activated;

	@ReadValue()
	private int vid;

	private List<ObjectId> exportDocRule_ids;

	private List<ObjectId> refDef_ids;

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public List<ObjectId> getExportDocRule_ids() {
		return exportDocRule_ids;
	}

	public List<ObjectId> getRefDef_ids() {
		return refDef_ids;
	}

	public String getName() {
		return name;
	}

	public String getEditorId() {
		return editorId;
	}

	public void setVid(int vid) {
		this.vid = vid;
	}

	@Structure("表单定义/list")
	public List<ExportDocRule> listExportDocRule() {
		return Optional.ofNullable(exportDocRule_ids)
				.map(_ids -> ServicesLoader.get(CommonService.class)
						.listExportDocRule(new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", _ids))).bson(), domain))
				.orElse(new ArrayList<ExportDocRule>());
	}

	@Structure("表单定义/count")
	public long countExportDocRule() {
		return Optional.ofNullable(exportDocRule_ids).map(_ids -> ServicesLoader.get(CommonService.class)
				.countExportDocRule(new BasicDBObject("_id", new BasicDBObject("$in", _ids)), domain)).orElse(0l);
	}

	public ExportDocRule newSubItem() {
		ExportDocRule edr = new ExportDocRule();
		edr.domain = domain;
		edr.setFormDef(this);
		return edr;
	}

	public List<Result> check(Map<String, String> formDFieldMap) {
		List<Result> results = new ArrayList<Result>();
		List<String> exportDocRuleFields = new ArrayList<String>();
		Set<String> formDFieldNames = formDFieldMap.keySet();
		// 检查ExportDocRule
		if (Check.isAssigned(exportDocRule_ids)) {
			List<ExportDocRule> exportDocRules = listExportDocRule();
			exportDocRules.forEach(exportDocRule -> {
				results.addAll(exportDocRule.check(formDFieldMap, exportDocRuleFields));
				formDFieldNames.removeAll(exportDocRuleFields);
			});
		}
		// 检查formDef
		for (String fieldName : formDFieldNames) {
			String text = formDFieldMap.get(fieldName);
			Result r = Result.warning("表单定义中，字段 " + (text != null ? text : "") + "[" + fieldName + "] 没有完全映射到文档。");
			r.setResultDate(new BasicDBObject("editorId", editorId).append("type", "warningField"));
			results.add(r);
		}

		// TODO 检查RefDef
		if (Check.isAssigned(refDef_ids)) {

		}

		return results;
	}

}
