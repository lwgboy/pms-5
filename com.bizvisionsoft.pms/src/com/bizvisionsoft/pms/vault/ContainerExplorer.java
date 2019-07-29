package com.bizvisionsoft.pms.vault;

import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IFolder;
import com.mongodb.BasicDBObject;

public class ContainerExplorer extends VaultExplorer {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	public ContainerExplorer() {
	}

	@Init
	private void init() {
		setContext(context);
		setBruiService(br);
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	protected BasicDBObject getFileQuery(IFolder folder) {
		if (folder != null) {
			return new BasicDBObject("folder_id", folder.get_id());
		} else {
			return new BasicDBObject("folder_id", null);
		}
	}

	@Override
	protected BasicDBObject getFolderQuery(IFolder folder) {
		if (folder != null) {
			return new BasicDBObject("parent_id", folder.get_id());
		} else {
			return new BasicDBObject("iscontainer", true);
		}
	}

	@Override
	protected boolean doCreateFolder(IFolder parentFolder, String value) {
		// TODO
		return false;
	}

	@Override
	protected boolean doDeleteFolder(IFolder folder) {
		// TODO
		return false;
	}

	@Override
	protected boolean doRenameFolder(IFolder folder, String value) {
		// TODO
		return false;
	}

}
