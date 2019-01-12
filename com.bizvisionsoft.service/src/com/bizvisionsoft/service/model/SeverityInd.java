package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("severityInd")
public class SeverityInd {
	
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	public String text;

	@ReadValue
	@WriteValue 
	public String value;
	
	@ReadValue
	@WriteValue 
	public String desc;
	
	@ReadValue
	@WriteValue 
	public int index;
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "严重性指标";

	@Override
	@Label
	public String toString() {
		return index+"." + value +" （"+ text+"）";
	}

}
