package com.bizvisionsoft.serviceimpl.update;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.serviceimpl.Service;
import com.mongodb.client.MongoCollection;

/**
 * �������ݣ�
 * <p>
 * 1. ������֯ģ�壬����Ŀ�����󣬿�ֱ��ͨ��������֯ģ��ķ�ʽ������Ŀ�Ŷӡ�
 * <p>
 * 2. �޸���֯����Ŀ�е���λ�ֶ�����Ϊ��qualifiedContractor��
 * <p>
 * 3. ���ӱ�����������п��Բ鿴�ҹ������Ŀ���ձ����ܱ����±���
 * <p>
 * 4.
 * ������Ŀ��ɫ����Ŀ�����飨��ɫ���Ϊ��PMO�������ܽ�ɫ����Ӧ���������쾭����������ͨ��PMO��ɫ��������Ŀ���ɹ��������������ɱ�����ͱ����������ʾ�����ݽ���ϸ�֡�
 * <p>
 * ������Ŀ��������Ŀ�ܼ����Ŀ����ԱȨ�޵��˻����Է��ʣ�������Ŀ�ܼ�Ȩ�޵��û������п��Բ鿴������Ŀ��Ϣ����Ŀ����ԱȨ�޵��˻�ֻ�ܿ�������Ϊ��ĿPMO�Ŷӳ�Ա����Ŀ��
 * <p>
 * �����������������������쾭��Ȩ�޵��˻����Է��ʣ������������Ȩ�޵��û������п��Բ鿴������Ŀ���������������쾭��Ȩ�޵��˻�ֻ�ܿ�������Ϊ��ĿPMO�Ŷӳ�Ա�ĵ�����������
 * <p>
 * �ɱ��������гɱ�����Ͳ�����Ȩ�޵��˻����Է��ʣ����гɱ�����Ȩ�޵��û������п��Բ鿴������Ŀ�ĳɱ����ݡ�������Ȩ�޵��˻�ֻ�ܿ�������Ϊ��ĿPMO�Ŷӳ�Ա����Ŀ�ɱ����ݡ�
 * <p>
 * �������������Ŀ�ܼ����Ŀ����ԱȨ�޵��˻����Է��ʣ�������Ŀ�ܼ�Ȩ�޵��û������п��Բ鿴������Ŀ�ı��档��Ŀ����ԱȨ�޵��˻�ֻ�ܿ�������Ϊ��ĿPMO�Ŷӳ�Ա����Ŀ�ı��档
 * <p>
 * ע�� ���¸ù���ʱ��ϵͳ���Զ����Ѵ�������Ŀ�����PMO�Ŷӡ��ù��ܸ�����ɺ����ڷ������˴��js��ѯ��Ŀ����������������ļ���
 * <p>
 * 1.��ѯ-��ĿPMO��Ա.js��
 * <p>
 * 2.׷��-CBS-CBSҶ�ӽڵ�ID.js��
 * <p>
 * 3.׷��-CBSScope-CBSҶ�ӽڵ�ID.js��
 * 
 * @author gdiyang
 *
 */
public class PMS0501_pmo implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void run() {
		// 1.��Ӵʵ䣨ע�������ظ���
		insertDictionary("��ɫ����", "PMO", "��Ŀ������");
		insertDictionary("���ܽ�ɫ", "��Ŀ�ܼ�", "��Ŀ�ܼ�");
		insertDictionary("���ܽ�ɫ", "��Ŀ����Ա", "��Ŀ����Ա");
		insertDictionary("���ܽ�ɫ", "��Ӧ������", "��Ӧ������");
		insertDictionary("���ܽ�ɫ", "��Ӧ������", "��Ӧ������");
		insertDictionary("���ܽ�ɫ", "�������", "�������");
		insertDictionary("���ܽ�ɫ", "���쾭��", "���쾭��");
		insertDictionary("���ܽ�ɫ", "�ɱ�����", "�ɱ�����");
		insertDictionary("���ܽ�ɫ", "������", "������");

		// 2.�޸���֯�����qualifiedContractor ԭ��projectBuilder
		c("organization").updateMany(new Document("projectBuilder", new Document("$ne", null)),
				new Document("$rename", new Document("projectBuilder", "qualifiedContractor")));
		logger.info("�����֯�ֶ�projectBuilder�ֶ����޸ģ��޸�ΪqualifiedContractor��");

		// 3.��ʷ��Ŀ���PMO
		List<OBSItem> insertOBSItem = new ArrayList<OBSItem>();
		// ��ȡ��ǰOBS�Ŷ��д���PMO�ķ�ΧID
		List<ObjectId> scope_ids = c("OBS").distinct("scope_id", new Document("roleId", "PMO"), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		// ��ȡ��Ҫ����PMO�Ŷӵ���Ŀ����������Ҫ���뵽OBS�е�PMO�Ŷ�
		c("project").find(new Document("_id", new Document("$nin", scope_ids)))
				.projection(new Document("_id", true).append("obs_id", true)).forEach((Document doc) -> {
					insertOBSItem.add(new OBSItem().setIsRole(false).generateSeq().setRoleId("PMO").setName("��Ŀ������")
							.setScopeRoot(false).setParent_id(doc.getObjectId("obs_id"))
							.setScope_id(doc.getObjectId("_id")));
				});

		if (insertOBSItem.size() > 0)
			try {
				Service.col(OBSItem.class).insertMany(insertOBSItem);
				logger.info("�����Ŀ���PMO�Ŷӡ�");
			} catch (Exception e) {
				logger.info("Ϊ��Ŀ���PMO�Ŷ�ʱ���ִ���:"+e.getMessage());
			}
	}

	/**
	 * �������д�����ɫ
	 * 
	 * @param type
	 * @param id
	 * @param name
	 */
	private void insertDictionary(String type, String id, String name) {
		try {
			c("dictionary").insertOne(new Document("type", type).append("id", id).append("name", name));
			logger.info("��� " + type + " " + id + " �����");
		} catch (Exception e) {
			logger.error("��� " + type + " " + id + " ʱ���ִ���:" + e.getMessage());
		}
	}

	private MongoCollection<Document> c(String name) {
		return Service.col(name);
	}

}
