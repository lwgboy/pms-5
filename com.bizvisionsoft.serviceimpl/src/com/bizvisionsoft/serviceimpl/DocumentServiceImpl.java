package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.DocuSetting;
import com.bizvisionsoft.service.model.DocuTemplate;
import com.bizvisionsoft.service.model.Folder;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class DocumentServiceImpl extends BasicServiceImpl implements DocumentService {

	@Override
	public Folder createFolder(Folder folder) {
		return insert(folder);
	}

	@Override
	public FolderInTemplate createFolderInTemplate(FolderInTemplate folder) {
		return insert(folder);
	}

	@Override
	public List<Folder> listProjectRootFolder(ObjectId project_id) {
		return c(Folder.class).find(new Document("project_id", project_id).append("parent_id", null))
				.into(new ArrayList<>());
	}

	@Override
	public List<FolderInTemplate> listProjectTemplateRootFolder(ObjectId template_id) {
		return c(FolderInTemplate.class).find(new Document("template_id", template_id).append("parent_id", null))
				.into(new ArrayList<>());
	}

	@Override
	public long countProjectRootFolder(ObjectId project_id) {
		return c("folder").countDocuments(new Document("project_id", project_id).append("parent_id", null));
	}

	@Override
	public List<Folder> listChildrenProjectFolder(ObjectId parent_id) {
		return c(Folder.class).find(new Document("parent_id", parent_id))// .sort(new Document("name", 1))
				.into(new ArrayList<>());
	}
	
	@Override
	public List<FolderInTemplate> listChildrenFolderTemplate(ObjectId parent_id) {
		return c(FolderInTemplate.class).find(new Document("parent_id", parent_id))// .sort(new Document("name", 1))
				.into(new ArrayList<>());
	}

	@Override
	public long countChildrenProjectFolder(ObjectId parent_id) {
		return c("folder").countDocuments(new Document("parent_id", parent_id));
	}

	@Override
	public long countChildrenFolderTemplate(ObjectId parent_id) {
		return c("folderInTemplate").countDocuments(new Document("parent_id", parent_id));
	}

	
	@Override
	public boolean deleteProjectFolder(ObjectId folder_id) {
		List<ObjectId> ids = getDesentItems(Arrays.asList(folder_id), "folder", "parent_id");
		c("folder").deleteMany(new Document("_id", new Document("$in", ids)));
		c("docu").deleteMany(new Document("folder_id", new Document("$in", ids)));
		// TODO 删除文档的时候需要删除文件
		
		
		return true;
	}
	
	@Override
	public boolean deleteProjectTemplateFolder(ObjectId folder_id) {
		List<ObjectId> ids = getDesentItems(Arrays.asList(folder_id), "folder", "parent_id");
		c("folderInTemplate").deleteMany(new Document("_id", new Document("$in", ids)));
		c("docu").deleteMany(new Document("folder_id", new Document("$in", ids)));
		// TODO 删除文档的时候需要删除文件
		
		
		return true;
	}

	@Override
	public boolean renameProjectFolder(ObjectId folder_id, String name) {
		c("folder").updateOne(new Document("_id", folder_id), new Document("$set", new Document("name", name)));
		return true;
	}

	@Override
	public boolean renameProjectTemplateFolder(ObjectId folder_id, String name) {
		c("folderInTemplate").updateOne(new Document("_id", folder_id),
				new Document("$set", new Document("name", name)));
		return true;
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
			if (filter == null) {
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
	public List<Docu> listProjectDocument(BasicDBObject condition, ObjectId project_id) {
		if (condition != null) {
			Integer skip = (Integer) condition.get("skip");
			Integer limit = (Integer) condition.get("limit");
			BasicDBObject filter = (BasicDBObject) condition.get("filter");
			BasicDBObject sort = (BasicDBObject) condition.get("sort");

			List<ObjectId> folder_id = c(Folder.class)
					.distinct("_id", new Document("project_id", project_id), ObjectId.class)
					.into(new ArrayList<ObjectId>());

			ArrayList<Bson> pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(new Document("folder_id", new Document("$in", folder_id))));

			if (filter != null)
				pipeline.add(Aggregates.match(filter));

			if (sort != null)
				pipeline.add(Aggregates.sort(sort));

			if (skip != null)
				pipeline.add(Aggregates.skip(skip));

			if (limit != null)
				pipeline.add(Aggregates.limit(limit));

			return c(Docu.class).aggregate(pipeline).into(new ArrayList<>());
		}
		return new ArrayList<Docu>();
	}

	@Override
	public long countProjectDocument(BasicDBObject filter, ObjectId project_id) {
		List<ObjectId> folder_id = c(Folder.class)
				.distinct("_id", new Document("project_id", project_id), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		if (filter == null)
			filter = new BasicDBObject();

		filter.append("folder_id", new Document("$in", folder_id));

		return count(filter, Docu.class);
	}

	@Override
	public long countDocument(BasicDBObject filter) {
		return count(filter, Docu.class);
	}

	@Override
	public List<Docu> listWorkPackageDocument(BasicDBObject condition) {
		return listDocument(condition);
	}
	
	@Override
	public List<DocuSetting> listWorkPackageDocumentSetting(BasicDBObject condition) {
		return createDataSet(condition, DocuSetting.class);
	}

	@Override
	public DocuTemplate insertDocumentTemplate(DocuTemplate docuT) {
		return insert(docuT);
	}

	@Override
	public long deleteDocumentTemplate(ObjectId _id) {
		return delete(_id, DocuTemplate.class);
	}

	@Override
	public long updateDocumentTemplate(BasicDBObject fu) {
		return update(fu, DocuTemplate.class);
	}

	@Override
	public List<DocuTemplate> listDocumentTemplates(BasicDBObject condition) {
		return createDataSet(condition, DocuTemplate.class);
	}

}
