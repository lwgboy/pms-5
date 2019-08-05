package com.bizvisionsoft.pms.vault;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class ContainerExplorer extends VaultExplorer {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	public ContainerExplorer() {
	}

	@Init
	protected void init() {
		setContext(context);
		setBruiService(br);
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	public ObjectId[] getRootFolder() {
		return new ObjectId[] {new ObjectId("5c679e4a1bd81d4bb0206064"),new ObjectId("5c679e4a1bd81d4bb0206065"),new ObjectId("5c679e4a1bd81d4bb0206066")};
	}

}
