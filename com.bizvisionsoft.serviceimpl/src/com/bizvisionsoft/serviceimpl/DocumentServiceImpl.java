package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.DocuSetting;
import com.bizvisionsoft.service.model.DocuTemplate;
import com.bizvisionsoft.service.model.Folder;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.mongodb.BasicDBObject;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;

public class DocumentServiceImpl extends BasicServiceImpl implements DocumentService {

	@Override
	public Folder createFolder(Folder folder, String domain) {
		return insert(folder, domain);
	}

	@Override
	public FolderInTemplate createFolderInTemplate(FolderInTemplate folder, String domain) {
		return insert(folder, domain);
	}

	@Override
	public List<Folder> listProjectRootFolder(ObjectId project_id, String domain) {
		return c(Folder.class, domain).find(new Document("project_id", project_id).append("parent_id", null)).into(new ArrayList<>());
	}

	@Override
	public List<FolderInTemplate> listProjectTemplateRootFolder(ObjectId template_id, String domain) {
		return list(FolderInTemplate.class, domain,
				new BasicDBObject("filter", new BasicDBObject("template_id", template_id).append("parent_id", null)),
				Aggregates.graphLookup("folderInTemplate", "$folderInTemplate_id", "parent_id", "_id", "parent"), //
				Aggregates.addFields(new Field<String>("path", "$parent.name")), //
				Aggregates.project(new Document("parent", false)));
	}

	@Override
	public long countProjectRootFolder(ObjectId project_id, String domain) {
		return c("folder", domain).countDocuments(new Document("project_id", project_id).append("parent_id", null));
	}

	@Override
	public List<Folder> listChildrenProjectFolder(ObjectId parent_id, String domain) {
		return c(Folder.class, domain).find(new Document("parent_id", parent_id))// .sort(new Document("name", 1))
				.into(new ArrayList<>());
	}

	@Override
	public List<FolderInTemplate> listChildrenFolderTemplate(ObjectId parent_id, String domain) {
		return list(FolderInTemplate.class, domain, new BasicDBObject("filter", new BasicDBObject("parent_id", parent_id)),
				Aggregates.graphLookup("folderInTemplate", "$folderInTemplate_id", "parent_id", "_id", "parent"), //
				Aggregates.addFields(new Field<String>("path", "$parent.name")), //
				Aggregates.project(new Document("parent", false)));
	}

	@Override
	public long countChildrenProjectFolder(ObjectId parent_id, String domain) {
		return c("folder", domain).countDocuments(new Document("parent_id", parent_id));
	}

	@Override
	public long countChildrenFolderTemplate(ObjectId parent_id, String domain) {
		return c("folderInTemplate", domain).countDocuments(new Document("parent_id", parent_id));
	}

	@Override
	public boolean deleteProjectFolder(ObjectId folder_id, String domain) {
		List<ObjectId> ids = getDesentItems(Arrays.asList(folder_id), "folder", "parent_id", domain);
		c("folder", domain).deleteMany(new Document("_id", new Document("$in", ids)));
		c("docu", domain).deleteMany(new Document("folder_id", new Document("$in", ids)));
		// TODO 删除文档的时候需要删除文件

		return true;
	}

	@Override
	public boolean deleteProjectTemplateFolder(ObjectId folder_id, String domain) {
		List<ObjectId> ids = getDesentItems(Arrays.asList(folder_id), "folder", "parent_id", domain);
		c("folderInTemplate", domain).deleteMany(new Document("_id", new Document("$in", ids)));
		c("docu", domain).deleteMany(new Document("folder_id", new Document("$in", ids)));
		// TODO 删除文档的时候需要删除文件

		return true;
	}

	@Override
	public boolean renameProjectFolder(ObjectId folder_id, String name, String domain) {
		c("folder", domain).updateOne(new Document("_id", folder_id), new Document("$set", new Document("name", name)));
		return true;
	}

	@Override
	public boolean renameProjectTemplateFolder(ObjectId folder_id, String name, String domain) {
		c("folderInTemplate", domain).updateOne(new Document("_id", folder_id), new Document("$set", new Document("name", name)));
		return true;
	}

	@Override
	public Docu createDocument(Docu doc, String domain) {
		return insert(doc, domain);
	}

	@Override
	public long deleteDocument(ObjectId _id, String domain) {
		return delete(_id, Docu.class, domain);
	}

	@Override
	public long updateDocument(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, Docu.class, domain);
	}

	@Override
	public List<Docu> listDocument(BasicDBObject condition, String domain) {
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

			appendUserInfo(pipeline, "createBy", "createByInfo", domain);

			return c(Docu.class, domain).aggregate(pipeline).into(new ArrayList<>());
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<Docu> listProjectDocument(BasicDBObject condition, ObjectId project_id, String domain) {
		if (condition != null) {
			Integer skip = (Integer) condition.get("skip");
			Integer limit = (Integer) condition.get("limit");
			BasicDBObject filter = (BasicDBObject) condition.get("filter");
			BasicDBObject sort = (BasicDBObject) condition.get("sort");

			List<ObjectId> folder_id = c(Folder.class, domain).distinct("_id", new Document("project_id", project_id), ObjectId.class)
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

			return c(Docu.class, domain).aggregate(pipeline).into(new ArrayList<>());
		}
		return new ArrayList<Docu>();
	}

	@Override
	public long countProjectDocument(BasicDBObject filter, ObjectId project_id, String domain) {
		List<ObjectId> folder_id = c(Folder.class, domain).distinct("_id", new Document("project_id", project_id), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		if (filter == null)
			filter = new BasicDBObject();

		filter.append("folder_id", new Document("$in", folder_id));

		return count(filter, Docu.class, domain);
	}

	@Override
	public long countDocument(BasicDBObject filter, String domain) {
		return count(filter, Docu.class, domain);
	}

	@Override
	public List<Docu> listWorkPackageDocument(ObjectId wp_id, String domain) {
		return listDocument(new BasicDBObject("filter", new BasicDBObject("workPackage_id", wp_id)), domain);
	}

	@Override
	public DocuTemplate insertDocumentTemplate(DocuTemplate docuT, String domain) {
		return insert(docuT, domain);
	}

	@Override
	public long deleteDocumentTemplate(ObjectId _id, String domain) {
		return delete(_id, DocuTemplate.class, domain);
	}

	@Override
	public long updateDocumentTemplate(BasicDBObject fu, String domain) {
		return update(fu, DocuTemplate.class, domain);
	}

	@Override
	public List<DocuTemplate> listDocumentTemplates(BasicDBObject condition, String domain) {
		return createDataSet(condition, DocuTemplate.class, domain);
	}

	@Override
	public DocuTemplate getDocumentTemplate(ObjectId _id, String domain) {
		return get(_id, DocuTemplate.class, domain);
	}

	@Override
	public List<DocuSetting> listDocumentSetting(ObjectId wp_id, String domain) {
		return list(DocuSetting.class, domain, new BasicDBObject("filter", new BasicDBObject("workPackage_id", wp_id)),
				Domain.getJQ(domain, "追加-文档设置-文件夹和文档模板").array());
	}

	@Override
	public DocuSetting createDocumentSetting(DocuSetting doc, String domain) {
		return insert(doc, domain);
	}

	@Override
	public long updateDocumentSetting(BasicDBObject fu, String domain) {
		return update(fu, DocuSetting.class, domain);
	}

	@Override
	public long deleteDocumentSetting(ObjectId _id, String domain) {
		return delete(_id, DocuSetting.class, domain);
	}

	@Override
	public long updateDocumentExportConfig(ExportableForm form, String col, ObjectId _id, String domain) {
		ObjectId confId = c(col,domain).distinct("exportConfig", ObjectId.class).first();
		if(confId == null) {//没有配置
			confId = new ObjectId();
		}
		form._id = _id;
		Document result = upsert(new Document("_id", _id), form.encodeDocument(), "exportConfig", domain);
		return Optional.ofNullable(result.getInteger("matched")).orElse(0);
	}

	@Override
	public long deleteDocumentExportConfig(ObjectId _id, String domain) {
		return delete(_id, "exportConfig", domain);
	}

}
