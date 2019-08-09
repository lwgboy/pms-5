package com.bizvisionsoft.pms.vault;

import org.bson.types.ObjectId;
import org.eclipse.swt.SWT;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IFolder;

public class SelectFolderOrDocACT {

	@Inject
	private IBruiService br;

	@Inject
	private IBruiContext context;

	@Execute
	public void execute() {
		// 测试打开文件夹选择器
		IFolder initialFolder = IFolder.newInstance(new ObjectId("5886f6859da5e3b0884fafb3"));

		FolderDocSelector.select(context, initialFolder, SWT.SINGLE,
				VaultExplorer.ADDRESS_BAR | VaultExplorer.FILETABLE |VaultExplorer.NAVIGATOR| VaultExplorer.SEARCH_FILE | VaultExplorer.SEARCH_FOLDER, "选择某某文件夹测试",
				false, false, o -> {
					System.out.println(o);
				});
	}
}
