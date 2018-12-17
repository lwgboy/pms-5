package com.bizvisionsoft.serviceimpl.projectsetting;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceimpl.Service;
import com.mongodb.client.MongoCollection;

public class ProjectStartSetting {

	public static String SETTING_NAME = "��Ŀ��������";

	/**
	 * ��ĿԤ��
	 */
	private static String SETTING_FIELD_BUDGET = "budget";
	/**
	 * ��Ŀ��Դ
	 */
	private static String SETTING_FIELD_RESOURCE = "resource";
	/**
	 * ��Ŀ�Ŷ�
	 */
	private static String SETTING_FIELD_OBS = "obs";
	/**
	 * ��Ŀ����
	 */
	private static String SETTING_FIELD_RBS = "rbs";

	/**
	 * ��Ŀ�ƻ�
	 */
	private static String SETTING_FIELD_PLAN = "plan";
	/**
	 * һ������ڵ�
	 */
	private static String SETTING_FIELD_CHARGER_L1 = "asgnL1";
	/**
	 * ��������ڵ�
	 */
	private static String SETTING_FIELD_CHARGER_L2 = "asgnL2";
	/**
	 * ��������ڵ�
	 */
	private static String SETTING_FIELD_CHARGER_L3 = "asgnL3";
	/**
	 * �ǹ���ڵ�
	 */
	private static String SETTING_FIELD_CHARGER_NoL = "asgnNoL";
	/**
	 * ������׼
	 */
	private static String SETTING_FIELD_APPROVEONSTART = "appvOnStart";

	private static String SETTING_VALUE_REQUIREMENT = "Ҫ��";
	private static String SETTING_VALUE_WARNING = "����";
	private static String SETTING_VALUE_IGNORE = "����";

	private Project project;
	private Document setting;
	private List<Result> result;

	public ProjectStartSetting(Project project, Document setting) {
		this.project = project;
		this.setting = setting;
		result = new ArrayList<Result>();
	}

	public static List<Result> setting(Project project, Document setting) {
		return new ProjectStartSetting(project, setting).setting();
	}

	private List<Result> setting() {

		List<Result> result = new ArrayList<Result>();

		// �����ĿԤ��
		checkBudget(setting.get(SETTING_FIELD_BUDGET), result);

		// �����Ŀ��Դ
		checkResource(setting.get(SETTING_FIELD_RESOURCE), result);

		// �����Ŀ�Ŷ�
		checkOBS(setting.get(SETTING_FIELD_OBS), result);

		// �����Ŀ����
		checkRBS(setting.get(SETTING_FIELD_RBS), result);

		// �����Ŀ�ƻ������������ڵ㸺���˺�ָ���ߵļ�飩
		checkPlan(setting.get(SETTING_FIELD_PLAN), setting.get(SETTING_FIELD_CHARGER_L1), setting.get(SETTING_FIELD_CHARGER_L2),
				setting.get(SETTING_FIELD_CHARGER_L3), setting.get(SETTING_FIELD_CHARGER_NoL), result);

		return result;
	}

	private void checkPlan(Object object, Object object2, Object object3, Object object4, Object object5, List<Result> result) {
		// TODO Auto-generated method stub
		
	}

	private void checkRBS(Object object, List<Result> result) {
		// TODO Auto-generated method stub

	}

	private void checkOBS(Object object, List<Result> result) {
		// TODO Auto-generated method stub

	}

	private void checkResource(Object object, List<Result> result) {
		// TODO Auto-generated method stub

	}

	private void checkBudget(Object object, List<Result> result) {
		ObjectId cbs_id = project.getCBS_id();

	}

	private MongoCollection<Document> c(String name) {
		return Service.col(name);
	}
}
