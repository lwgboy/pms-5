package com.bizvisionsoft.service.model;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("formDef")
public class FormDef {

	public String domain;

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	@ReadValue
	@WriteValue
	private String name;

	@WriteValue
	@ReadValue({ "editorId", "editorTypeId" })
	private String editorTypeId;

	@ReadValue
	@WriteValue
	private boolean activated;

	@ImageURL("name")
	@Exclude
	private String icon = "/img/form_c.svg";

	private List<ObjectId> exportDocRule_ids;

	public String getName() {
		return name;
	}

	@WriteValue("editorName")
	public void setEditorName(String editorName) {
		Pattern editorIdText = Pattern.compile("_(v|V)(.*?)(.editorassy)");
		Matcher matcher = editorIdText.matcher(editorName);
		if (matcher.find()) {
			this.editorTypeId = editorName.replaceAll(matcher.group(), "");
		} else {
			this.editorTypeId = editorName.replaceAll(".editorassy", "");
		}
	}

	@ReadValue("editorName")
	public String getEditorName() {
		return editorTypeId;
	}

	@Structure("表单定义/list")
	public List<ExportDocRule> listSubAccountItems() {
		return ServicesLoader.get(CommonService.class).listExportDocRule(
				new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", exportDocRule_ids))).bson(), domain);
	}

	@Structure("表单定义/count")
	public long countSubAccountItems() {
		return ServicesLoader.get(CommonService.class)
				.countExportDocRule(new BasicDBObject("_id", new BasicDBObject("$in", exportDocRule_ids)), domain);
	}

	@Structure("new")
	public ExportDocRule newSubItem() {
		ExportDocRule edr = new ExportDocRule();
		edr.domain = domain;
		return edr;
	}

}
