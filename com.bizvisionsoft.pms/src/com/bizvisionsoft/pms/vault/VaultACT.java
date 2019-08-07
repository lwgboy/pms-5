package com.bizvisionsoft.pms.vault;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IFolder;

public class VaultACT {

	// TODO 考虑到权限需要调用服务，使用操作控制Action行为

	@Inject
	private IBruiService br;

	@Inject
	private IBruiContext context;

	@Execute
	public void execute(@MethodParam(Execute.EVENT) Event e, @MethodParam(Execute.ACTION) Action action,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		explorer.handleAction(folder, action);
	}

	@Behavior("openFolder")
	private boolean enableOpenFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableOpenFolder(folder);
	}

	@Behavior("moveFolder")
	private boolean enableMoveFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableMoveFolder(folder);
	}

	@Behavior("deleteFolder")
	private boolean enableDeleteFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableDeleteFolder(folder);
	}

	@Behavior("renameFolder")
	private boolean enableRenameFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableRenameFolder(folder);
	}
}
