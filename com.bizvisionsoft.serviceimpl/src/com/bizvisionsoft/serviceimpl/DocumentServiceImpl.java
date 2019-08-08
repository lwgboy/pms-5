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
		// TODO ɾ���ĵ���ʱ����Ҫɾ���ļ�

		return true;
	}

	@Override
	public boolean deleteProjectTemplateFolder(ObjectId folder_id, String domain) {
		List<ObjectId> ids = getDesentItems(Arrays.asList(folder_id), "folder", "parent_id", domain);
		c("folderInTemplate", domain).deleteMany(new Document("_id", new Document("$in", ids)));
		c("docu", domain).deleteMany(new Document("folder_id", new Document("$in", ids)));
		// TODO ɾ���ĵ���ʱ����Ҫɾ���ļ�

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
	public long countDocument(BasicDBObject filter, String userId, String domain) {
		if (Check.isNotAssigned(filter))
			return 0;
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		long count = countDocument(input, filter, null, "docu", domain);
		return count;
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
				Domain.getJQ(domain, "׷��-�ĵ�����-�ļ��к��ĵ�ģ��").array());
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
	public VaultFolder getProjectRootFolder(ObjectId project_id, String userId, String domain) {
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		List<VaultFolder> result = query(input, null, 1, new BasicDBObject("project_id", project_id).append("isflderroot", true), null,
				null, VaultFolder.class, domain);
		if (Check.isAssigned(result))
			return result.get(0);
		return null;
	}

	@Override
	public List<VaultFolder> listFolderWithPath(BasicDBObject condition, ObjectId initialFolder_id, String userId, String domain) {
		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) condition.get("filter")).orElse(null);
		if (Check.isNotAssigned(filter))
			return new ArrayList<>();

		filter = getFolderFilterWithInitialFolder(filter, initialFolder_id, domain);

		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(new BasicDBObject("_id", 1));
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		Consumer<List<Bson>> output = p -> {
			appendFolderPath(p, domain);
		};
		return query(input, skip, limit, filter, sort, output, VaultFolder.class, domain);
	}

	private void appendFolderPath(List<Bson> pipeline, String domain) {
		pipeline.addAll(Domain.get(domain).jq("׷��-Ŀ¼-·��").array());
	}

	private void appendFolderAdditionInfoQueryPipeline(List<Bson> p, String userId) {
		// TODO ����Ŀ¼���������Բ�ѯ�ܵ�
	}

	private void appendFolderAuthQueryPipeline(List<Bson> pipeling, String userId) {
		// TODO ��Ȩ�ޡ� ��Ҫ����Ȩ�޲�ѯ�Ĺܵ�

	}

	private void appendDocumentAuthQueryPipeline(List<Bson> pipeling, String userId) {
		// TODO ��Ȩ�ޡ� ��Ҫ����Ȩ�޲�ѯ�Ĺܵ�

	}

	@Override
	public long countFolder(BasicDBObject filter, ObjectId parent_id, String userId, String domain) {
		parent_id = parent_id.equals(Formatter.ZeroObjectId()) ? null : parent_id;
		filter = Optional.ofNullable(filter).orElse(new BasicDBObject()).append("parent_id", parent_id);
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		return countDocument(input, filter, null, "folder", domain);
	}

	@Override
	public long countFolderWithPath(BasicDBObject filter, ObjectId initialFolder_id, String userId, String domain) {
		if (Check.isNotAssigned(filter))
			return 0;
		filter = getFolderFilterWithInitialFolder(filter, initialFolder_id, domain);
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
		pipeline.addAll(Domain.get(domain).jq("׷��-�ĵ�-�汾�ʹ�����Ϣ").array());
	}

	private void appendDocuDescriptorPath(List<Bson> pipeline, String domain) {
		pipeline.addAll(Domain.get(domain).jq("׷��-�ĵ�-·��").array());
	}

	private void appendDocuDescriptorOwner(List<Bson> pipeline, String domain) {
		pipeline.addAll(Domain.get(domain).jq("׷��-�ĵ�-������").array());
	}

	@Override
	public List<DocuDescriptor> listDocumentWithPath(BasicDBObject condition, ObjectId initialFolder_id, String userId, String domain) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (Check.isNotAssigned(filter))
			return new ArrayList<>();
		filter = getDocumentFilterWithInitialFolder(filter, initialFolder_id, domain);
		// getFDocumentFilterWithInitialFolder
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
	public long countDocumentWithPath(BasicDBObject filter, ObjectId initialFolder_id, String userId, String domain) {
		if (Check.isNotAssigned(filter))
			return 0;
		filter = getDocumentFilterWithInitialFolder(filter, initialFolder_id, domain);
		Consumer<List<Bson>> input = p -> appendFolderAuthQueryPipeline(p, userId);
		long count = countDocument(input, filter, null, "docu", domain);
		return count;
	}

	private BasicDBObject getDocumentFilterWithInitialFolder(BasicDBObject filter, ObjectId initialFolder_id, String domain) {
		filter = Optional.ofNullable(filter).orElse(new BasicDBObject());

		if (!initialFolder_id.equals(Formatter.ZeroObjectId()) // �ж�initialFolder_id�Ƿ�Ϊ��ʱ��Ϊ��ʱ��ʾȫ���Ͽ��ѯ������Ӳ�ѯ��Χ��
				&& !Optional.ofNullable(filter.get("folder_id")).isPresent()) {// �ж��жϵ�ǰ��ѯ�Ƿ��޶����ļ��в�ѯ��Χ������޶�������Ӳ�ѯ��Χ��
			Document doc = c("folder", domain).find(new BasicDBObject("_id", initialFolder_id))
					.projection(new BasicDBObject("iscontainer", 1).append("isflderroot", 1).append("project_id", 1)).first();// ����initialFolder_id��ȡ�ļ��е�iscontainer��isflderroot��project_id����

			if (doc.getBoolean("isflderroot", false)) {// �жϸ��ļ����Ƿ�Ϊ��Ŀ�ļ���
				filter.append("project_id", doc.get("project_id"));// �ڲ�ѯ�����������Ŀid�Ĳ�ѯ������
			} else {
				List<ObjectId> desentItems;
				if (doc.getBoolean("iscontainer", false)) {// �жϸ��ļ����Ƿ�Ϊ����
					desentItems = c("folder", domain).distinct("_id", new BasicDBObject("root_id", initialFolder_id), ObjectId.class)
							.into(new ArrayList<ObjectId>());// ��ȡinitialFolder�����µ��ļ���_id������������
				} else {
					desentItems = getDesentItems(Arrays.asList(initialFolder_id), "folder", "parent_id", domain);// ��ȡinitialFolder�µ����ļ���_id����������
				}
				filter.append("folder_id", new BasicDBObject("$in", desentItems));// �ڲ�ѯ����������ļ��з�Χ�Ĳ�ѯ������
			}
		}
		return filter;

	}

	private BasicDBObject getFolderFilterWithInitialFolder(BasicDBObject filter, ObjectId initialFolder_id, String domain) {
		filter = Optional.ofNullable(filter).orElse(new BasicDBObject());

		if (!initialFolder_id.equals(Formatter.ZeroObjectId()) // �ж�initialFolder_id�Ƿ�Ϊ��ʱ��Ϊ��ʱ��ʾȫ���Ͽ��ѯ������Ӳ�ѯ��Χ��
				&& !Optional.ofNullable(filter.get("folder_id")).isPresent()) {// �ж��жϵ�ǰ��ѯ�Ƿ��޶����ļ��в�ѯ��Χ������޶�������Ӳ�ѯ��Χ��
			Document doc = c("folder", domain).find(new BasicDBObject("_id", initialFolder_id))
					.projection(new BasicDBObject("iscontainer", 1).append("isflderroot", 1).append("project_id", 1)).first();// ����initialFolder_id��ȡ�ļ��е�iscontainer��isflderroot��project_id����
			if (doc.getBoolean("iscontainer", false)) {// �жϸ��ļ����Ƿ�Ϊ����
				filter.append("root_id", initialFolder_id);// �ڲ�ѯ���������������ѯ������
			} else if (doc.getBoolean("isflderroot", false)) {// �жϸ��ļ����Ƿ�Ϊ��Ŀ�ļ���
				filter.append("project_id", doc.get("project_id"));// �ڲ�ѯ�����������Ŀid�Ĳ�ѯ������
			} else {
				List<ObjectId> desentItems = getDesentItems(Arrays.asList(initialFolder_id), "folder", "parent_id", domain);// ��ȡinitialFolder�µ����ļ���_id����������
				filter.append("_id", new BasicDBObject("$in", desentItems));// �ڲ�ѯ����������ļ��з�Χ�Ĳ�ѯ������
			}
		}
		return filter;
	}

	@Override
	public List<VaultFolder> getPath(ObjectId _id, String domain) {
		return queryJQ(VaultFolder.class, "folder", "��ѯ-Ŀ¼-·��", new Document("_id", _id), domain);
	}

	@Override
	public long deleteVaultFolder(ObjectId _id, String domain) {
		// ����ļ����Ƿ��ܱ�ɾ��
		// ����Ƿ�����¼��ļ���
		if (count(new BasicDBObject("parent_id", _id), VaultFolder.class, domain) > 0)
			throw new ServiceException("�����¼��ļ��У�������ɾ����");

		// ����Ƿ�����ĵ�
		if (count(new BasicDBObject("folder_id", _id), Docu.class, domain) > 0)
			throw new ServiceException("�ļ����д����ĵ�����ȫ���Ƴ����ٽ���ɾ����");

		// ɾ���ļ���
		return delete(_id, VaultFolder.class, domain);
	}

	@Override
	public long moveVaultFolder(ObjectId _id, ObjectId newParent_id, String domain) {
		Document folderParentInfo = c("folder", domain).find(new BasicDBObject("_id", newParent_id))
				.projection(new BasicDBObject("root_id", 1).append("project_id", 1).append("projectworkorder", 1).append("projectdesc", 1)
						.append("projectnumber", 1))
				.first();
		Document folder = c("folder", domain).find(new BasicDBObject("_id", _id))
				.projection(new BasicDBObject("iscontainer", 1).append("isflderroot", 1).append("project_id", 1)).first();

		List<ObjectId> desentItems = getDesentItems(Arrays.asList(_id), "folder", "parent_id", domain);

		// �ж��Ƿ��ƶ������ļ����¼�
		if (desentItems.contains(newParent_id))
			throw new ServiceException("���ܽ�Ŀ¼�ƶ�����Ŀ¼����Ŀ¼�С�");


		if (folder.getBoolean("iscontainer", false))
			throw new ServiceException("�����ƶ�������");

		// �޸����ļ��е�root_id
		c("folder", domain).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)),
				new BasicDBObject("$set", new BasicDBObject("root_id", folderParentInfo.get("root_id"))));

		BasicDBObject updateChild = new BasicDBObject().append("project_id", folderParentInfo.getOrDefault("project_id", null))
				.append("projectworkorder", folderParentInfo.getOrDefault("projectworkorder", null))
				.append("projectdesc", folderParentInfo.getOrDefault("projectdesc", null))
				.append("projectnumber", folderParentInfo.getOrDefault("projectnumber", null));
		// ��Ҫ�ж�Ҷ���ļ����ǲ�����Ŀ���ļ��У�������ǣ������޸���Ŀ���ԡ�

		doUpdateChildFolderProjectInfo(Arrays.asList(_id), updateChild, domain);

		return c("folder", domain)
				.updateMany(new BasicDBObject("_id", _id), new BasicDBObject("$set", new BasicDBObject("parent_id", newParent_id)))
				.getModifiedCount();
	}

	private void doUpdateChildFolderProjectInfo(List<ObjectId> _ids, BasicDBObject updateChild, String domain) {
		List<ObjectId> childId = c("folder", domain).distinct("_id",
				new BasicDBObject("parent_id", new BasicDBObject("$in", _ids)).append("isflderroot", new BasicDBObject("$ne", true)),
				ObjectId.class).into(new ArrayList<>());

		if (childId.size() > 0) {
			c("folder", domain).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", childId)),
					new BasicDBObject("$set", updateChild));
			doUpdateChildFolderProjectInfo(childId, updateChild, domain);
		}
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
