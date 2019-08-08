package com.bizvisionsoft.pms.vault;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.service.model.IFolder;

public class PathActionEvent extends Event {
	
	/**
	 * ��ַ����
	 */
	public static final int SetData = 1000;
	
	/**
	 * ��ַ����
	 */
	public static final int Modify = 10001;
	
	/**
	 * ��ťѡ��
	 */
	public static final int Selection = 10002;

	/**
	 * ��ַ����ѯ
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

