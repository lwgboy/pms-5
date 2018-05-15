package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("trackView")
public class TrackView {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String catagory;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String viewName;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "ÏîÄ¿¸ú×ÙÊÓÍ¼";

	@Override
	@Label
	public String toString() {
		return catagory + "/" + name;
	}
}
