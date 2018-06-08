package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.Folder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class DocumentServiceImpl extends BasicServiceImpl implements DocumentService {

	@Override
	public Folder createFolder(Folder folder) {
		return insert(folder);
	}

	@Override
	public List<Folder> listRootFolder(ObjectId project_id) {
		return c(Folder.class).find(new Document("project_id", project_id).append("parent_id", null))
				.sort(new Document("name", 1)).into(new ArrayList<>());
	}

	@Override
	public long countRootFolder(ObjectId project_id) {
		return c("folder").count(new Document("project_id", project_id).append("parent_id", null));
	}

	@Override
	public List<Folder> listChildrenFolder(ObjectId parent_id) {
		return c(Folder.class).find(new Document("parent_id", parent_id)).sort(new Document("name", 1))
				.into(new ArrayList<>());
	}

	@Override
	public long countChildrenFolder(ObjectId parent_id) {
		return c("folder").count(new Document("parent_id", parent_id));
	}

	@Override
	public void deleteFolder(ObjectId folder_id) {
		List<ObjectId> ids = getDesentItems(Arrays.asList(folder_id), "folder", "parent_id");
		c("folder").deleteMany(new Document("_id", new Document("$in", ids)));
		c("docu").deleteMany(new Document("folder_id", new Document("$in", ids)));
		// TODO 删除文档的时候需要删除文件
	}

	@Override
	public void renameFolder(ObjectId folder_id, String name) {
		c("folder").updateOne(new Document("_id", folder_id), new Document("$set", new Document("name", name)));
	}

	@Override
	public Docu createDocument(Docu doc) {
		return insert(doc);
	}

	@Override
	public long deleteDocument(ObjectId _id) {
		return delete(_id, Docu.class);
	}

	@Override
	public long updateDocument(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Docu.class);
	}

	@Override
	public List<Docu> listDocument(BasicDBObject condition) {
		if (condition != null) {
			Integer skip = (Integer) condition.get("skip");
			Integer limit = (Integer) condition.get("limit");
			BasicDBObject filter = (BasicDBObject) condition.get("filter");
			if(filter == null) {
				return new ArrayList<>();
			}
			BasicDBObject sort = (BasicDBObject) condition.get("sort");
			ArrayList<Bson> pipeline = new ArrayList<Bson>();
			if (filter != null)
				pipeline.add(Aggregates.match(filter));

			if (sort != null)
				pipeline.add(Aggregates.sort(sort));

			if (skip != null)
				pipeline.add(Aggregates.skip(skip));

			if (limit != null)
				pipeline.add(Aggregates.limit(limit));

			appendUserInfo(pipeline, "createBy", "createByInfo");

			return c(Docu.class).aggregate(pipeline).into(new ArrayList<>());
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public long countDocument(BasicDBObject filter) {
		return count(filter, Docu.class);
	}

}
