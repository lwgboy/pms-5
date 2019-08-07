package com.bizvisionsoft.service.model;

public interface DocuStatus {
	/**
	 * 已发布
	 */
	public  final String STATUS_RELEASED_ID = "released"; 
	public  final String STATUS_RELEASED_TEXT ="已发布";

	/**
	 * 工作中
	 */
	public  final String STATUS_WORKING_ID = "working"; 
	public  final String STATUS_WORKING_TEXT = "工作中";

	/**
	 * 审核中
	 */
	public  final String STATUS_APPROVING_ID = "approving";
	public  final String STATUS_APPROVING_TEXT = "审核中";

	/**
	 * 已废弃
	 */
	public  final String STATUS_DEPOSED_ID = "deposed"; 
	public  final String STATUS_DEPOSED_TEXT = "已废弃";
}
