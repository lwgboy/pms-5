package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.mongodb.BasicDBObject;

@PersistenceCollection("formDef")
public class FormDef {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "表单定义";

	@ImageURL("name")
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
	private String editorTypeId;

	@ReadValue
	@WriteValue
	private boolean activated;


	private List<ObjectId> exportDocRule_ids;

	public String getName() {
		return name;
	}

	@WriteValue("editorTypeId")
	public void setEditorTypeId(String editorTypeId) {
		Pattern editorIdText = Pattern.compile("_(v|V)(.*?)(.editorassy)");
		Matcher matcher = editorIdText.matcher(editorTypeId);
		if (matcher.find()) {
			this.editorTypeId = editorTypeId.replaceAll(matcher.group(), "");
		} else {
			this.editorTypeId = editorTypeId.replaceAll(".editorassy", "");
		}
	}

	public String getEditorTypeId() {
		return editorTypeId;
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

}
