package com.bizvisionsoft.pms.vault;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

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

	@Override
	protected BasicDBObject getFileQuery(IFolder folder) {
		if (folder != null) {
			return new BasicDBObject("folder_id", folder.get_id());
		} else {
			return new BasicDBObject("folder_id", host_id);
		}
	}

	@Override
	protected void selectFolderQueryFolder(IFolder folder) {
		// TODO 添加权限控制
		folderPane.setViewerInput(service.listContainer(getFolderQuery(folder), br.getDomain()));
	}

	@Override
	protected BasicDBObject getFolderQuery(IFolder folder) {
		return new Query().filter(new BasicDBObject("_id", host_id)).bson();
	}

	protected String getFolderGridassy() {
		return "vault\\项目资料库文件夹.gridassy";
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
