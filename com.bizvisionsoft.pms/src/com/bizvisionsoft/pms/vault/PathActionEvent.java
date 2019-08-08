package com.bizvisionsoft.pms.vault;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.service.model.IFolder;

public class PathActionEvent extends Event {
	
	/**
	 * 地址加载
	 */
	public static final int SetData = 1000;
	
	/**
	 * 地址更改
	 */
	public static final int Modify = 10001;
	
	/**
	 * 按钮选择
	 */
	public static final int Selection = 10002;

	/**
	 * 地址栏查询
	 */
	public static final int Search = 10003;

	
	public PathActionEvent(int eventCode, Action action, IFolder[] path) {
		type = eventCode;
		this.path = path;
		this.action = action;
	}

	public IFolder[] path;
	public Action action;
}

