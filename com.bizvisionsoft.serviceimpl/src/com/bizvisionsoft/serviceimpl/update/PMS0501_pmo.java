package com.bizvisionsoft.serviceimpl.update;
import com.bizvisionsoft.service.common.Service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.serviceimpl.SystemServiceImpl;
import com.mongodb.client.MongoCollection;

/**
 * �������ݣ�
 * <p>
 * 1. ���´������ݿ�����
 * <p>
 * 2. ������֯ģ�壬����Ŀ�����󣬿�ֱ��ͨ��������֯ģ��ķ�ʽ������Ŀ�Ŷӡ�
 * <p>
 * 3. �޸���֯����Ŀ�е���λ�ֶ�����Ϊ��qualifiedContractor��
 * <p>
 * 4. ���ӱ�����������п��Բ鿴�ҹ������Ŀ���ձ����ܱ����±���
 * <p>
 * 5.
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
		// 1.���´������ݿ�����
		new SystemServiceImpl().createIndex();

		// 2.��Ӵʵ䣨ע�������ظ���
		insertDictionary("��ɫ����", "PMO", "��Ŀ������");
		insertDictionary("���ܽ�ɫ", "��Ŀ�ܼ�", "��Ŀ�ܼ�");
		insertDictionary("���ܽ�ɫ", "��Ŀ����Ա", "��Ŀ����Ա");
		insertDictionary("���ܽ�ɫ", "��Ӧ������", "��Ӧ������");
		insertDictionary("���ܽ�ɫ", "��Ӧ������", "��Ӧ������");
		insertDictionary("���ܽ�ɫ", "�������", "�������");
		insertDictionary("���ܽ�ɫ", "���쾭��", "���쾭��");
		insertDictionary("���ܽ�ɫ", "�ɱ�����", "�ɱ�����");
		insertDictionary("���ܽ�ɫ", "������", "������");

		// 3.�޸���֯�����qualifiedContractor ԭ��projectBuilder
		c("organization").updateMany(new Document("projectBuilder", new Document("$ne", null)),
				new Document("$rename", new Document("projectBuilder", "qualifiedContractor")));
		logger.info("�����֯�ֶ�projectBuilder�ֶ����޸ģ��޸�ΪqualifiedContractor��");

		// 4.��ʷ��Ŀ���PMO
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
				logger.info("Ϊ��Ŀ���PMO�Ŷ�ʱ���ִ���:" + e.getMessage());
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
/**
 * ������������ݣ�
 * <p>
 * 1.�����-ҵ�����-������֯ģ���һ�����
 * <p>
 * 1.1��֯ģ�����: �������:��֯ģ��; �������:������;
 * ȡ����������:com.bizvisionsoft.service.ProjectTemplateService
 * ���ö����������͹�����;�������:��֯ģ�����
 * �����:id(������:id,�ı�:���,�����,��:160);name(������:name,�ı�:����,�����,��:300);description(������:description,�ı�:˵��,�����,��:320)
 * �в���:ɾ��(��������:ɾ��ѡ�ж���),�༭(��������:�༭���ѡ�ж���.�༭�����:��֯ģ��༭��),��(��������:�Զ������,ͼ��:/img/right.svg,��ʽ:Ĭ��,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.projecttemplate.OpenProjectTemplateACT)
 * ����������:������֯ģ��:(��������:�����¶���,�༭�����:��֯ģ��༭��,�����ʶ:com.bizvisionsoft.service,����:com.bizvisionsoft.service.model.OBSModule)
 * <p>
 * 1.2��֯ģ��༭�����:�������:��֯ģ��༭��;����:��֯ģ��;ʹ��խģʽ
 * �ֶ�:id(����:id,�ı�:���,����Ϊ��,�ֶ�����:�����ı�),name(����:name,�ı�:����,����Ϊ��,�ֶ�����:�����ı�),description(����:description,�ı�:˵��,�ֶ�����:�����ı�),epsInfos(����:epsInfos,�ı�:���÷�Χ,����Ϊ��,�ֶ�����:�������ѡ���,ѡ�������:EPS����)
 * <p>
 * 1.3��֯ģ��ѡ�����б����: �������:��֯ģ��ѡ�����б�; �������:������;����:������Ŀѡ����֯ģ��;
 * ȡ����������:com.bizvisionsoft.service.ProjectTemplateService
 * �����:id(������:id,�ı�:���,�����,��:160);name(������:name,�ı�:����,�����,��:300);description(������:description,�ı�:˵��,�����,��:320)
 * <p>
 * 1.4��֯ģ��ѡ�������:�������:��֯ģ��ѡ����; �������:����ʽѡ�������;����:������Ŀѡ����֯ģ��;������:��֯ģ��ѡ�����б�;
 * <p>
 * 1.5OBSģ����֯�ṹͼ���: �������:OBSģ����֯�ṹͼ; �������:�����;
 * ȡ����������:com.bizvisionsoft.service.ProjectTemplateService
 * ���ö����������͹�����;�������:��֯�ṹģ�� �ڵ����:
 * ��ӽ�ɫ:����:�Զ������,����:��ӽ�ɫ,�ı�:��ӽ�ɫ,ǿ��ʹ���ı�,ͼ��:/img/add_16_w.svg,���:һ��,������Ϊ����,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.projecttemplate.CreateOBSRoleItemACT;
 * �����Ŷ�:����:�Զ������,����:�����Ŷ�,�ı�:�����Ŷ�,ǿ��ʹ���ı�,ͼ��:/img/team_w.svg,���:һ��,������Ϊ����,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.projecttemplate.CreateOBSTeamItemACT;;
 * ָ��������:����:�Զ������,����:ָ��������,�ı�:ָ��������,ǿ��ʹ���ı�,ͼ��:/img/appointment_w.svg,���:һ��,������Ϊ����,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.projecttemplate.AppointmentOBSInTemplateACT;
 * �༭:����:�Զ������,����:�༭,�ı�:�༭,ǿ��ʹ���ı�,ͼ��:/img/edit_w.svg,���:һ��,������Ϊ����,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.projecttemplate.EditOBSItemACT;
 * ɾ��:����:ɾ��ѡ�ж���,����:ɾ��,�ı�:ɾ��,ǿ��ʹ���ı�,ͼ��:/img/minus_w.svg,���:����;
 * ��Ա:����:��������,����:��Ա,�ı�:��Ա,ǿ��ʹ���ı�,ͼ��:/img/people_list_w.svg,���:��Ϣ,���������:��֯ģ���Ŷӳ�Ա,ԭ�����������ر�;
 * <p>
 * 1.6��֯ģ���Ŷӳ�Ա���: �������:��֯ģ���Ŷӳ�Ա; �������:������;����:������֯ģ���б༭�Ŷӳ�Ա;
 * ȡ����������:com.bizvisionsoft.service.OBSService ���ö����������͹�����;�������:�Ŷӳ�Ա,��ʾ�����������,
 * �����,�в���ͬ"�Ŷӳ�Ա(id:162dcc76808)"���.
 * ����������:����û�:����:�Զ������,����:����û�,ͼ��:/img/add_16_w.svg,���:һ��,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.projecttemplate.AddOBSInTemplateMember
 * <p>
 * 2.�����-���ӱ�������һ�����
 * <p>
 * 2.1�ձ��������: �������:�ձ�����; �������:������;
 * ȡ����������:com.bizvisionsoft.service.WorkReportService ���ö����������͹�����;�������:�ձ�����
 * �����:ͬ"�ձ�(id:163f1e35a8f)"���
 * �в���:���ձ�(��������:�Զ������,ͼ��:/img/right.svg,��ʽ:Ĭ��,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 2.2�ܱ��������: �������:�ܱ�����; �������:������;
 * ȡ����������:com.bizvisionsoft.service.WorkReportService ���ö����������͹�����;�������:�ܱ�����
 * �����:ͬ"�ܱ�(id:163fe7e3c53)"���
 * �в���:���ܱ�(��������:�Զ������,ͼ��:/img/right.svg,��ʽ:Ĭ��,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 2.3�±��������: �������:�±�����; �������:������;
 * ȡ����������:com.bizvisionsoft.service.WorkReportService ���ö����������͹�����;�������:�±�����
 * �����:ͬ"�±�(id:163fe9eba7f)"���
 * �в���:���±�(��������:�Զ������,ͼ��:/img/right.svg,��ʽ:Ĭ��,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 2.4�ձ�(id:163f1e35a8f),�ܱ�(id:163fe7e3c53),�±�(id:163fe9eba7f),��ȷ�ϵı���(id:1646acf088f),��Ŀ�±�(id:16401bc7721),��Ŀ�ܱ�(id:16401bbfa5c),��Ŀ�ձ�(id:16401bb95a9)
 * �в���"���±�"�޸�:(��������:�Զ������,ͼ��:/img/right.svg,��ʽ:Ĭ��,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 3.�����-ҵ�����-��֯������֯�༭����id��162b75519b0���͸���֯�༭����id��162b7bb8236������Ŀ�е���֯�ֶ�����ΪqualifiedContractor
 * <p>
 * 4.�������������,������Ŀ��������Ŀ:����Ϊ"������Ŀ������",�����ʶ:com.bizvisionsoft.onlinedesigner,����:com.bizvisionsoft.onlinedesigner.systemupdate.SystemUpdateV0501_pmo
 * <p>
 * 5.��ҳ������޸�:
 * <p>
 * 5.1���������"�������",����"�ҵı���"��Ŀ�·�,��ɫ����Ϊ:��Ŀ����Ա#��Ŀ�ܼ�.���������Ŀ�´���3������Ŀ:1.�ձ�����:��������Ϊ��������,����������Ϊ"�ձ�����";2.�ܱ�����:��������Ϊ��������,����������Ϊ"�ܱ�����";3.�±�����:��������Ϊ��������,����������Ϊ"�±�����"
 * <p>
 * 5.2�޸ĳɱ������ɫ����Ϊ:������#�������#��Ŀ�ܼ�
 * <p>
 * 5.3�޸Ĳɹ������ɫ����Ϊ:��Ӧ������#��Ӧ������#��Ŀ�ܼ�
 * <p>
 * 5.4�޸����������ɫ����Ϊ:���쾭��#�������#��Ŀ�ܼ�
 * <p>
 * 5.5��Ҫ��ӣ���ѯ-��ĿPMO��Ա.js��׷��-CBS-CBSҶ�ӽڵ�ID.js��׷��-CBSScope-CBSҶ�ӽڵ�ID.js
 * <p>
 * 6.ҵ���������������֯ģ��,
 * <p>
 * 6.1����"WBSģ��"��Ŀ�·�.��������Ϊ��������,����������Ϊ"��֯ģ��";
 * <p>
 * 6.2�����-��ҳ-��Ŀ��ҳ-��Ŀ��������(����״̬)������������action:������֯ģ��,����"ʹ����Ŀģ��"action�·�.������֯ģ���������:�Զ������;ͼ��Ϊ:/img/org_c.svg;�����ʶcom.bizvisionsoft.pms;����:com.bizvisionsoft.pms.project.action.UseOBSModule;��ɫ:PM#PPM
 * <p>
 * 6.3�����-��Ŀ-��Ŀ�Ŷ�-��֯�ṹͼ�ڵ����������actuon�������֯ģ��,����"�����Ŷ�"action�·�.�����֯ģ���������:�Զ������;����:�����֯ģ��;�ı�:���ģ��;ͼ��Ϊ:/img/module_w.svg;�����ʶcom.bizvisionsoft.pms;����:com.bizvisionsoft.pms.obs.AddOBSModuleACT;
 * <p>
 * 6.4�����-ҵ�����-��Ŀģ�����-OBSģ��-��Ŀģ����֯�ṹͼ.���ӹ���������:���Ϊ��֯ģ��.��������:�Զ������;ǿ��ʹ���ı�;��ť���:һ��;�����ʶcom.bizvisionsoft.pms;����:com.bizvisionsoft.pms.projecttemplate.SaveAsOBSModuleACT
 * <p>
 * 6.5��Ҫ��ӣ���ѯ-OBS-��֯ģ�����ظ��Ľ�ɫ.js��׷��-��֯ģ��-��ɫ.js
 * <p>
 * 7.�򿪱�������޸�:���:��Ŀ���(id:16441e4dd33)�ʹ���������Ŀ���(id:1644f5bf5cf)
 * �в���"�򿪱��"�޸�:(��������:�Զ������,ͼ��:/img/right.svg,��ʽ:Ĭ��,�����ʶ:com.bizvisionsoft.pms,����:com.bizvisionsoft.pms.projectchange.OpenProjectChangeACT)
 * <p>
 * 8.����Զ��嵼��
 * <p>
 * 8.1��Ŀ�ʽ�ƻ����(id:162faeee5d8):���������
 * ����(��������:�Զ������,ͼ��:/img/excel_w.svg,��ʽ:һ��,�����ʶ:com.bizvisionsoft.bruiengine,����:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 8.2��Ŀʵ�ʳɱ����(id:165c2a4b23a):���������
 * ����(��������:�Զ������,ͼ��:/img/excel_w.svg,��ʽ:һ��,�����ʶ:com.bizvisionsoft.bruiengine,����:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 8.3��Դ�������(id:16370ff5184):�����������
 * ����(��������:�Զ������,ͼ��:/img/excel_w.svg,��ʽ:һ��,�����ʶ:com.bizvisionsoft.bruiengine,����:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 8.4��Դ�������(id:16396e1b1ae):�����������
 * ����(��������:�Զ������,ͼ��:/img/excel_w.svg,��ʽ:һ��,�����ʶ:com.bizvisionsoft.bruiengine,����:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 9.����undo��redo��ť�������-��Ŀ-���ȼƻ�-��Ŀ����ͼ���༭����id��1633ee05a77���Ĺ��������������ӳ����ͻָ���ť
 * <p>
 * 9.1������ť���������ͣ��Զ���������������ƣ��������ı���������ǿ��ʹ���ı������һ�㣻�����ʶ��com.bizvisionsoft.pms��������com.bizvisionsoft.pms.work.gantt.action.GanttEditUndo
 * <p>
 * 9.2�ָ���ť���������ͣ��Զ���������������ƣ��ָ����ı����ָ���ǿ��ʹ���ı������һ�㣻�����ʶ��com.bizvisionsoft.pms��������com.bizvisionsoft.pms.work.gantt.action.GanttEditRedo
 * <p>
 * 10.���ӽ׶α༭��(�ɸ���"����ͼ�׶ι����༭��(id:162ff4cd460)"�����޸�)��
 * �����-��Ŀ-�༭��-�����༭�������Ӹ���ͼ�ܳɽ׶α༭����������ƣ�����ͼ�ܳɽ׶α༭����������⣺�׶Σ����������ڽ׶ηֽ��༭�׶Σ�խ�����뵽��������
 * �ֶΣ�text���ֶ����ͣ������ı����ֶ����ƣ�text���ֶ��ı����׶����ƣ�����Ϊ�գ���
 * start_date���ֶ����ͣ�����ʱ��ѡ���ֶ����ƣ�start_date���ֶ��ı����ƻ���ʼ��ֻ�����������ͣ�����ʱ�䣩��
 * end_date���ֶ����ͣ�����ʱ��ѡ���ֶ����ƣ�end_date���ֶ��ı����ƻ���ɣ�ֻ�����������ͣ�����ʱ�䣩��
 * charger���ֶ����ͣ�����ѡ����ֶ����ƣ�charger���ֶ��ı����׶θ��𣬲���Ϊ�գ�ѡ�����������Ŀ�Ŷӣ�id��162d7e505eb����
 * <p>
 * 11.���ӱ༭����ʱ�ƻ���ʼʱ�䡢�ƻ���ɺ͹��ڵĽ�����
 * ���-��Ŀ-�༭��-�����༭���У�����ͼ�����༭��(id��1628fc969a5)�͸���ͼ�׶ι����༭��(id��162ff4cd460)����start_date�·������У�����end_date�ƶ��������е��¼�������end_date�·��������ֶΣ�duration��
 * �޸�end_date�ֶΣ�д�����������ֶΣ�duration��
 * ����duration�ֶΣ��ֶ����ͣ������ı����ֶ����ƣ�duration���ı������ڡ�������飺������д�����������ֶΣ�end_date��
 * <p>
 * 12.�޸���Դ�ƻ�ά����ʽ��������Ҫ�޸�����JS�� ��ѯ-��Դ-�ƻ���ʵ������-���������ڲ���.js ��ѯ-��Դ-�ƻ���ʵ������-��Ŀ.js
 * ��ѯ-��Դ-�ƻ�����-����.js ��ѯ-��Դ-�ƻ�����-��Ŀ.js ��ѯ-��Դ-�ƻ�����.js ��ѯ-��Դ-ʵ������.js ��ѯ-��Դ.js
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 *
 *
 *
 */
