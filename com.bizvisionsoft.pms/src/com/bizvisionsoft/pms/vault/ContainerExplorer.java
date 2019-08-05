package com.bizvisionsoft.pms.vault;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.serviceconsumer.Services;

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
	public IFolder getCurrentFolder() {
		return IFolder.Null;
	}

	@Override
	protected IFolder[] getPath(IFolder folder) {
		if (IFolder.Null.equals(folder)) {
			return null;
		} else {
			List<VaultFolder> result = Services.get(DocumentService.class).getPath(folder.get_id(),br.getDomain());
			return result.toArray(new IFolder[0]);
		}
	}

	@Override
	protected Assembly getNavigatorAssembly() {
		return br.getAssembly("vault/Ä¿Â¼µ¼º½.gridassy");
	}

}
