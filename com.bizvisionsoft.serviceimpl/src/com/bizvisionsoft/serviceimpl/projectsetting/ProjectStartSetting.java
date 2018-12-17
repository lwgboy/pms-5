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

	public static String SETTING_NAME = "项目启动设置";

	/**
	 * 项目预算
	 */
	private static String SETTING_FIELD_BUDGET = "budget";
	/**
	 * 项目资源
	 */
	private static String SETTING_FIELD_RESOURCE = "resource";
	/**
	 * 项目团队
	 */
	private static String SETTING_FIELD_OBS = "obs";
	/**
	 * 项目风险
	 */
	private static String SETTING_FIELD_RBS = "rbs";

	/**
	 * 项目计划
	 */
	private static String SETTING_FIELD_PLAN = "plan";
	/**
	 * 一级管理节点
	 */
	private static String SETTING_FIELD_CHARGER_L1 = "asgnL1";
	/**
	 * 二级管理节点
	 */
	private static String SETTING_FIELD_CHARGER_L2 = "asgnL2";
	/**
	 * 三级管理节点
	 */
	private static String SETTING_FIELD_CHARGER_L3 = "asgnL3";
	/**
	 * 非管理节点
	 */
	private static String SETTING_FIELD_CHARGER_NoL = "asgnNoL";
	/**
	 * 启动批准
	 */
	private static String SETTING_FIELD_APPROVEONSTART = "appvOnStart";

	private static String SETTING_VALUE_REQUIREMENT = "要求";
	private static String SETTING_VALUE_WARNING = "警告";
	private static String SETTING_VALUE_IGNORE = "忽略";

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

		// 检查项目预算
		checkBudget(setting.get(SETTING_FIELD_BUDGET), result);

		// 检查项目资源
		checkResource(setting.get(SETTING_FIELD_RESOURCE), result);

		// 检查项目团队
		checkOBS(setting.get(SETTING_FIELD_OBS), result);

		// 检查项目风险
		checkRBS(setting.get(SETTING_FIELD_RBS), result);

		// 检查项目计划（包含各级节点负责人和指派者的检查）
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
