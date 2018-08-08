package com.bizvisionsoft.service.model;

public interface ICommand {

	public static final String Approve_Project = "项目启动批准";

	public static final String Start_Project_Ignore_Warrning = "项目启动（忽略警告）";

	public static final String Start_Project = "项目启动";

	public static final String Finish_Project_Ignore_Warrning = "项目收尾（忽略警告）";

	public static final String Finish_Project = "项目收尾";

	public static final String Close_Project_Ignore_Warrning = "项目关闭（忽略警告）";

	public static final String Close_Project = "项目关闭";

	public static final String Distribute_Project_Plan = "项目计划下达";

	public static final String Finish_Work = "工作完成";

	public static final String Start_Work = "工作开始";

	public static final String Close_Stage = "阶段关闭";

	public static final String Close_Stage_Ignore_Warrning = "阶段关闭（忽略警告）";

	public static final String Start_Stage = "阶段启动";

	public static final String Finish_Stage = "阶段收尾";
	
	public static final String Finish_Stage_Ignore_Warrning = "阶段收尾（忽略警告）";



}
