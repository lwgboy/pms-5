package com.bizvisionsoft.pms.vault;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class SelectFolderOrDocACT {

	@Inject
	private IBruiService br;

	@Inject
	private IBruiContext context;

	@Execute
	public void execute() {
		// 测试打开文件夹选择器
		FolderDocSelector.selectDocument(context, o -> {

		});
	}
}
