package com.bizvisionsoft.pms.filecabinet;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.Folder;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class FileCabinetASM extends CabinetASM {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private ObjectId host_id;

	private Object input;

	@Init
	private void init() {
		setContext(context);
		setBruiService(br);
		input = context.getRootInput();
		if (input instanceof Project) {
			this.host_id = ((Project) input).get_id();
		} else if (input instanceof ProjectTemplate) {
			this.host_id = ((ProjectTemplate) input).get_id();
		} else {
			throw new RuntimeException("文件夹组件只支持Project/ProjectTemplate类型的根上下文");
		}
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	protected List<?> getInput() {
		if (input instanceof Project) {
			return Services.get(DocumentService.class).listProjectRootFolder(host_id, br.getDomain());
		} else if (input instanceof ProjectTemplate) {
			return Services.get(DocumentService.class).listProjectTemplateRootFolder(host_id, br.getDomain());
		}
		return new ArrayList<>();
	}

	@Override
	protected boolean doCreateFolder(IFolder parentFolder, String folderName) {
		if (input instanceof Project) {
			return doCreateProjectFolder((Folder) parentFolder, folderName);
		} else if (input instanceof ProjectTemplate) {
			return doCreateProjectTemplateFolder((FolderInTemplate) parentFolder, folderName);
		}
		return false;
	}

	private boolean doCreateProjectTemplateFolder(FolderInTemplate parentFolder, String folderName) {
		FolderInTemplate folder = br.newInstance(FolderInTemplate.class);
		folder.setName(folderName);
		if (parentFolder != null) {
			folder.setParent_id(parentFolder.get_id());
		}
		folder.setTempalte_id(host_id);
		folder = Services.get(DocumentService.class).createFolderInTemplate(folder, br.getDomain());
		return true;
	}

	private boolean doCreateProjectFolder(Folder parentFolder, String folderName) {
		Folder folder = br.newInstance(Folder.class);
		folder.setName(folderName);
		if (parentFolder != null) {
			folder.setParent_id(parentFolder.get_id());
		}
		folder.setProject_id(host_id);
		folder = Services.get(DocumentService.class).createFolder(folder, br.getDomain());
		return true;
	}

	@Override
	protected boolean doRenameFolder(IFolder folder, String name) {
		folder.setName(name);
		if (input instanceof Project) {
			return Services.get(DocumentService.class).renameProjectFolder(folder.get_id(), name, br.getDomain());
		} else if (input instanceof ProjectTemplate) {
			return Services.get(DocumentService.class).renameProjectTemplateFolder(folder.get_id(), name, br.getDomain());
		}
		return false;
	}

	@Override
	protected boolean doDeleteFolder(IFolder folder) {
		if (input instanceof Project) {
			Services.get(DocumentService.class).deleteProjectFolder(folder.get_id(), br.getDomain());
		} else if (input instanceof ProjectTemplate) {
			Services.get(DocumentService.class).deleteProjectTemplateFolder(folder.get_id(), br.getDomain());
		}
		return false;
	}

}
