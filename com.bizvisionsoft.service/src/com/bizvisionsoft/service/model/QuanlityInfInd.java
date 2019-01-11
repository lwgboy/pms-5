package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("quanlityInfoInd")
public class QuanlityInfInd {
	
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	public String text;

	@ReadValue
	@WriteValue 
	public String value;
	
	@Override
	@Label
	public String toString() {
		return text;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "����Ӱ��ȼ�";


}
