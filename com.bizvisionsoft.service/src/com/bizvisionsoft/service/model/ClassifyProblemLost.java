package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
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

	@Structure("new")
	public ClassifyProblemLost newSubItem() {
		ClassifyProblemLost ci = new ClassifyProblemLost();
		ci.parent_id = _id;
		return ci;
	}

	@Structure("list")
	public List<ClassifyProblemLost> listSubItems() {
		return ServicesLoader.get(ProblemService.class).listClassifyProblemLost(new BasicDBObject("parent_id", _id));
	}

	@Structure("count")
	public long countSubItems() {
		return ServicesLoader.get(ProblemService.class).countClassifyProblemLost(_id);
	}

}
