package com.bizvisionsoft.pms.vault;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProjectExplorer extends VaultExplorer {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private Object input;

	private ObjectId host_id;

	private DocumentService service;

	public ProjectExplorer() {
	}

	@Init
	protected void init() {
		setContext(context);
		setBruiService(br);
		input = context.getRootInput();
		if (input instanceof Project) {
			this.host_id = ((Project) input).getFolder_id();
		} else {
			throw new RuntimeException("文件夹组件只支持Project类型的根上下文");
		}

		service = Services.get(DocumentService.class);
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}


	@Override
	public IFolder getInitialFolder() {
		return VaultFolder.Null;
		// TODO Auto-generated method stub
	}

	@Override
	protected IFolder[] getPath(IFolder folder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Assembly getNavigatorAssembly() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<List<Action>> createToolbarActions() {
		// TODO Auto-generated method stub
		return null;
	}


}
