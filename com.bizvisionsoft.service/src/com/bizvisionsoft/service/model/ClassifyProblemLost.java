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

@PersistenceCollection("classifyProblemLost")
public class ClassifyProblemLost {

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
	
	@Exclude
	public String domain;

	@Structure("new")
	public ClassifyProblemLost newSubItem() {
		ClassifyProblemLost ci = new ClassifyProblemLost();
		ci.parent_id = _id;
		return ci;
	}

	@Structure("list")
	public List<ClassifyProblemLost> listSubItems() {
		return ServicesLoader.get(ProblemService.class).listClassifyProblemLost(new BasicDBObject("parent_id", _id), domain);
	}

	@Structure("count")
	public long countSubItems() {
		return ServicesLoader.get(ProblemService.class).countClassifyProblemLost(_id, domain);
	}


	@ReadValue
	public String path;
	
	@ReadValue
	public List<ObjectId> _ids;
	
	@ReadValue
	@SetValue
	public boolean isLeaf;

	@Override
	@Label
	public String toString() {
		return ""+path;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "问题损失类别";
}
