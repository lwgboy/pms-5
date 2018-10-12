package com.bizvisionsoft.pms.work.gantt.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;

public class DisplayCriticalPath {
	
	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GanttPart part){
		part.switchCriticalPathHighLight();
	}

}
