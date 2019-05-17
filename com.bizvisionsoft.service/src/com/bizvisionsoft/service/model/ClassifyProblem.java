package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.ServicesLoader;
import com.mongodb.BasicDBObject;

@PersistenceCollection("classifyProblem")
public class ClassifyProblem {

	@ReadValue
	@WriteValue
	public ObjectId _id;
	
	@ReadValue
	@WriteValue
	public String id;

	@ReadValue
	@WriteValue
	public String name;

	@ReadValue
	@WriteValue
	public String description;

	@ReadValue
	@WriteValue
	public ObjectId parent_id;
	
	@Structure("new")
	public ClassifyProblem newSubItem() {
		ClassifyProblem ci = new ClassifyProblem();
		ci.domain = domain;
		ci.parent_id = _id;
		return ci;
	}
	
	public String domain;

	@Structure("list")
	public List<ClassifyProblem> listSubItems() {
		return ServicesLoader.get(ProblemService.class).listClassifyProblem(new BasicDBObject("parent_id", _id), domain);
	}

	@Structure("count")
	public long countSubItems() {
		return ServicesLoader.get(ProblemService.class).countClassifyProblem(_id, domain);
	}


	@ReadValue
	public String path;
	
	@ReadValue
	@SetValue
	public boolean isLeaf;

	@ReadValue
	public List<ObjectId> _ids;
	
	
	@Override
	@Label
	public String toString() {
		return ""+path;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "问题类别";

}
