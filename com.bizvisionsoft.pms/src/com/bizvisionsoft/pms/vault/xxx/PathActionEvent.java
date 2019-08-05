package com.bizvisionsoft.pms.vault.xxx;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.service.model.IFolder;

public class PathActionEvent extends Event {
	public PathActionEvent(int eventCode, Action action, IFolder[] path) {
		type = eventCode;
		this.path = path;
		this.action = action;
	}

	public IFolder[] path;
	public Action action;
}

