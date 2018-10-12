package com.bizvisionsoft.pms.work.gantt.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;

/**
 * not fully support until version 5.2
 * 
 * 5.2版本以前不完全支持
 */
public class GanttEditUndo {

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GanttPart part) {
		part.undo();
	}

}
