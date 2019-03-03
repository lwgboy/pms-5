package com.bizvisionsoft.serviceimpl.scheduling;

/**
 * 后果
 * 
 * @author hua
 *
 */
public class Consequence {

	/**
	 * 受到影响的任务
	 */
	public final Task task;

	/**
	 * 影响值
	 */
	public final float value;

	/**
	 * @param task
	 *            收到影响的任务
	 * @param value
	 *            影响值
	 */
	public Consequence(Task task, float value) {
		this.task = task;
		this.value = value;
	}
}
