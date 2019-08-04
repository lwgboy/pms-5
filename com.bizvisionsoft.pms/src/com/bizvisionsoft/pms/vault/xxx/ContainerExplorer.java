package com.bizvisionsoft.pms.vault.xxx;

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
	private void init() {
		setContext(context);
		setBruiService(br);
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

}
