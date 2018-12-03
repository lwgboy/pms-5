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

	public Document match;

	public Document getDocument() {
		return new Document("_id", _id).append("label", label).append("type", type).append("meta", meta).append("match", match);
	}
	
	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Catalog other = (Catalog) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
	
	

}
