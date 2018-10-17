package com.bizvisionsoft.pms.cbs.action;

import java.util.Optional;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IExportable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;

public class ExportCBSSubjectACT {

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Optional<Object> optional = context.searchContent(
				c -> c instanceof BruiAssemblyContext && "cbssubject".equals(((BruiAssemblyContext) c).getName()),
				IBruiContext.SEARCH_DOWN);
		Object obj = optional.orElse(null);
		if (obj instanceof IExportable)
			((IExportable) obj).export();
	}
}
