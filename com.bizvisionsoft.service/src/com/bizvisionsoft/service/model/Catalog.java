package com.bizvisionsoft.service.model;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;

/**
 * 通用的树形结构
 * 
 * @author gh
 *
 */
public class Catalog {

	@ReadValue
	public ObjectId _id;

	@ReadValue
	@Label
	public String label;

	@ImageURL("label")
	public String icon;

	@ReadValue(ReadValue.TYPE)
	public String type;

	public Document meta;

	public Document getDocument() {
		return new Document("_id", _id).append("label", label).append("type", type).append("meta", meta);
	}

}
