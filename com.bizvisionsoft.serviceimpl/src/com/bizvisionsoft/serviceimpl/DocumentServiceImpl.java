package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.DocuDescriptor;
import com.bizvisionsoft.service.model.DocuSetting;
import com.bizvisionsoft.service.model.DocuTemplate;
import com.bizvisionsoft.service.model.Folder;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
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

			// appendUserInfo(pipeline, "createBy", "createByInfo", domain);

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
	public List<VaultFolder> listContainer(BasicDBObject condition, String domain) {
		if (condition != null)
			return createDataSet(condition, VaultFolder.class, domain);
		return new ArrayList<VaultFolder>();
	}

	@Override
	public long countContainer(BasicDBObject filter, String domain) {
		if (filter != null)
			return count(filter, VaultFolder.class, domain);
		return 0;
	}

	@Override
	public List<VaultFolder> listFolder(BasicDBObject condition, ObjectId parent_id, String userId, String domain) {
		Integer skip = Optional.ofNullable(condition).map(c -> c.getInt("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> c.getInt("limit")).orElse(null);
		parent_id = parent_id.equals(Formatter.ZeroObjectId()) ? null : parent_id;
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) condition.get("filter")).orElse(new BasicDBObject())//
				.append("parent_id", parent_id);

		BasicDBObject sort = Optional.ofNullable((BasicDBObject) condition.get("sort")).orElse(new BasicDBObject("_id", 1));
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		Consumer<List<Bson>> output = p -> appendFolderAdditionInfoQueryPipeline(p, userId);
		return query(input, skip, limit, filter, sort, output, VaultFolder.class, domain);
	}

	@Override
	public List<VaultFolder> listFolderWithPath(BasicDBObject condition, String userId, String domain) {
		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) condition.get("filter")).orElse(null);
		if (Check.isNotAssigned(filter))
			return new ArrayList<>();

		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(new BasicDBObject("_id", 1));
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		Consumer<List<Bson>> output = p -> {
			appendFolderPath(p, domain);
		};
		return query(input, skip, limit, filter, sort, output, VaultFolder.class, domain);
	}

	private void appendFolderPath(List<Bson> pipeline, String domain) {
		pipeline.addAll(Domain.get(domain).jq("追加-目录-路径").array());
	}

	private void appendFolderAdditionInfoQueryPipeline(List<Bson> p, String userId) {
		// TODO 补充目录的其他属性查询管道
	}

	private void appendFolderAuthQueryPipeline(List<Bson> pipeling, String userId) {
		// TODO 【权限】 需要补充权限查询的管道

	}

	private void appendDocumentAuthQueryPipeline(List<Bson> pipeling, String userId) {
		// TODO 【权限】 需要补充权限查询的管道

	}

	@Override
	public long countFolder(BasicDBObject filter, ObjectId parent_id, String userId, String domain) {
		parent_id = parent_id.equals(Formatter.ZeroObjectId()) ? null : parent_id;
		filter = Optional.ofNullable(filter).orElse(new BasicDBObject()).append("parent_id", parent_id);
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		return countDocument(input, filter, null, "folder", domain);
	}

	@Override
	public long countFolder(BasicDBObject filter, String userId, String domain) {
		if (Check.isNotAssigned(filter))
			return 0;
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		return countDocument(input, filter, null, "folder", domain);
	}

	@Override
	public List<DocuDescriptor> listDocument(BasicDBObject condition, String userId, String domain) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (Check.isNotAssigned(filter))
			return new ArrayList<>();

		Integer skip = Optional.ofNullable(condition).map(c -> c.getInt("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> c.getInt("limit")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(new BasicDBObject("_id", 1));
		Consumer<List<Bson>> input = p -> {
			appendDocumentAuthQueryPipeline(p, userId);
			appendDocuDescriptorOwner(p, domain);
		};
		Consumer<List<Bson>> output = p -> {
			appendDocuDescriptor(p, domain);
		};
		return query(input, skip, limit, filter, sort, output, DocuDescriptor.class, domain);
	}

	private void appendDocuDescriptor(List<Bson> pipeline, String domain) {
		pipeline.addAll(Domain.get(domain).jq("追加-文档属性-版本和创建信息").array());
	}

	private void appendDocuDescriptorPath(List<Bson> pipeline, String domain) {
		pipeline.addAll(Domain.get(domain).jq("追加-文档属性-路径").array());
	}

	private void appendDocuDescriptorOwner(List<Bson> pipeline, String domain) {
		pipeline.addAll(Domain.get(domain).jq("追加-文档属性-所有者").array());
	}

	@Override
	public List<DocuDescriptor> listDocumentWithPath(BasicDBObject condition, String userId, String domain) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (Check.isNotAssigned(filter))
			return new ArrayList<>();

		Integer skip = Optional.ofNullable(condition).map(c -> c.getInt("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> c.getInt("limit")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(new BasicDBObject("_id", 1));
		Consumer<List<Bson>> input = p -> {
			appendDocumentAuthQueryPipeline(p, userId);
			appendDocuDescriptorOwner(p, domain);
		};
		Consumer<List<Bson>> output = p -> {
			appendDocuDescriptor(p, domain);
			appendDocuDescriptorPath(p, domain);
		};
		return query(input, skip, limit, filter, sort, output, DocuDescriptor.class, domain);
	}

	@Override
	public long countDocument(BasicDBObject filter, String userId, String domain) {
		if (Check.isNotAssigned(filter))
			return 0;
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		long count = countDocument(input, filter, null, "docu", domain);
		return count;
	}

	@Override
	public List<VaultFolder> getPath(ObjectId _id, String domain) {
		return queryJQ(VaultFolder.class, "folder", "查询-目录-路径", new Document("_id", _id), domain);
	}

	@Override
	public long deleteVaultFolder(ObjectId _id, String domain) {
		// 检查文件夹是否能被删除
		// 检查是否存在下级文件夹
		if (count(new BasicDBObject("parent_id", _id), VaultFolder.class, domain) > 0)
			throw new ServiceException("存在下级文件夹，不允许删除。");

		// 检查是否存在文档
		if (count(new BasicDBObject("folder_id", _id), Docu.class, domain) > 0)
			throw new ServiceException("文件夹中存在文档，请全部移除后再进行删除。");

		// 删除文件夹
		return delete(_id, VaultFolder.class, domain);
	}

	@Override
	public long moveVaultFolder(ObjectId _id, ObjectId newParent_id, String domain) {
		return c(VaultFolder.class, domain)
				.updateMany(new BasicDBObject("_id", _id), new BasicDBObject("$set", new BasicDBObject("parent_id", newParent_id)))
				.getModifiedCount();
	}

	@Override
	public long renameVaultFolder(ObjectId _id, String newName, String domain) {
		return c(VaultFolder.class, domain)
				.updateMany(new BasicDBObject("_id", _id), new BasicDBObject("$set", new BasicDBObject("desc", newName)))
				.getModifiedCount();

	}

	@Override
	public VaultFolder insertFolder(VaultFolder vf, String domain) {
		return insert(vf, VaultFolder.class, domain);
	}

}
