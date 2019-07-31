package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("formDef")
public class FormDef {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "������";

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

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
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

	@Structure("������/list")
	public List<ExportDocRule> listExportDocRule() {
		return ServicesLoader.get(CommonService.class).listExportDocRule(new Query().filter(new BasicDBObject("formDef_id", _id)).bson(),
				domain);
	}

	@Structure("������/count")
	public long countExportDocRule() {
		return ServicesLoader.get(CommonService.class).countExportDocRule(new BasicDBObject("formDef_id", _id), domain);
	}

	public List<RefDef> listRefDef() {
		return ServicesLoader.get(CommonService.class).listRefDef(new Query().bson(), _id, domain);
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
		// ���ExportDocRule
		List<ExportDocRule> exportDocRules = listExportDocRule();
		if (exportDocRules.size() > 0) {
			exportDocRules.forEach(exportDocRule -> {
				results.addAll(exportDocRule.check(formDFieldMap, exportDocRuleFields));
				formDFieldNames.removeAll(exportDocRuleFields);
			});
		}
		// ���formDef
		for (String fieldName : formDFieldNames) {
			String text = formDFieldMap.get(fieldName);
			Result r = Result.warning("�������ֶΡ�" + (text != null ? text : "") + "[" + fieldName + "]��û��ӳ�䵽�ĵ���");
			r.code = Result.CODE_CHECK_FORMDEF;
			r.setResultDate(new BasicDBObject("editorId", editorId).append("type", "warningField"));
			results.add(r);
		}

		// ���RefDef
		List<RefDef> refDefs = listRefDef();
		if (refDefs.size() > 0)
			refDefs.forEach(refDef -> results.addAll(refDef.check(formDFieldMap)));

		return results;
	}

	/**
	 * ���Ʋ��հ�ť
	 * 
	 * @param element
	 * @return
	 */
	@Behavior({ "����" })
	private boolean enableRef() {
		return true;
	}

}
