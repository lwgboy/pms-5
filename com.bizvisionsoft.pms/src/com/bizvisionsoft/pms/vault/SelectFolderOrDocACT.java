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
		// ²âÊÔ´ò¿ªÎÄ¼þ¼ÐÑ¡ÔñÆ÷
		IFolder initialFolder = IFolder.newInstance(new ObjectId("5886f6859da5e3b0884fafb3"));

		FolderDocSelector.select(context, initialFolder, SWT.MULTI,
				VaultExplorer.ADDRESS_BAR | VaultExplorer.FILETABLE | VaultExplorer.SEARCH_FILE | VaultExplorer.SEARCH_FOLDER, "Ñ¡ÔñÄ³Ä³ÎÄµµ²âÊÔ",
				true, false, o -> {
					System.out.println(o);
				});
	}
}
