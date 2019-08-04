package com.bizvisionsoft.pms.vault.xxx;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.Project;
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
	private void init() {
		setContext(context);
		setBruiService(br);
		input = context.getRootInput();
		if (input instanceof Project) {
			this.host_id = ((Project) input).getFolder_id();
		} else {
			throw new RuntimeException("文件夹组件只支持Project类型的根上下文");
		}

		service = Services.get(DocumentService.class);
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}


}
