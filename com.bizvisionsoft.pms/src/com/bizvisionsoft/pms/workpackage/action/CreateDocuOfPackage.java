package com.bizvisionsoft.pms.workpackage.action;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.UniversalDataService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.DocuSetting;
import com.bizvisionsoft.service.model.DocuTemplate;
import com.bizvisionsoft.service.model.Folder;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateDocuOfPackage {

	@Inject
	private IBruiService brui;

	// �Ѿ���ģ�����Ŀ�ķֿ���û�б�Ҫ��Behavior���ã�����ע�����´���
	// @Behavior("����ĵ�/�����ĵ�")
	// private boolean behaviour(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT)
	// Object input) {
	// return !(input instanceof ProjectTemplate);
	// }

	@Execute
	public void execute(final @MethodParam(Execute.CONTEXT) IBruiContext context) {
		// ����Ƿ�����ĵ�����
		WorkPackage wp = (WorkPackage) context.getInput();
		List<DocuSetting> setting = Services.get(DocumentService.class).listDocumentSetting(wp.get_id());
		if (setting.isEmpty()) {
			createGenaricDocument(context);
		} else {
			ActionMenu am = new ActionMenu(brui);
			List<Action> actions = new ArrayList<>();

			appendGeneriDocument(am, actions, context);
			setting.forEach(ds -> appendDocuSettingAct(am, actions, ds, context));

			am.setActions(actions).open();
		}
	}

	private void appendGeneriDocument(ActionMenu am, List<Action> actions, IBruiContext context) {
		Action a = new Action();
		String name = "create";
		a.setName(name);
		a.setText("�ĵ�");
		a.setStyle("normal");
		actions.add(a);
		am.handleActionExecute(name, act -> {
			createGenaricDocument(context);
			return false;
		});
	}

	private void appendDocuSettingAct(ActionMenu am, List<Action> actions, DocuSetting ds, IBruiContext context) {

		Action a = new Action();
		String name = "create_" + ds.get_id();
		a.setName(name);
		a.setText(ds.getName());
		a.setStyle("normal");
		actions.add(a);
		am.handleActionExecute(name, act -> {
			createDocumentFromSetting(context, ds);
			return false;
		});
	}

	private void createDocumentFromSetting(IBruiContext context, DocuSetting ds) {
		WorkPackage wp = (WorkPackage) context.getInput();
		GridPart gridPart = (GridPart) context.getContent();
		ObjectId dt_id = ds.getDocuTemplate_id();
		DocuTemplate dt = Services.get(DocumentService.class).getDocumentTemplate(dt_id);
		if (dt == null)
			throw new RuntimeException("�޷�����ĵ�ģ�壬�ĵ�����Ϊ��" + ds.getName());
		Docu docu = new Docu()//
				.setCreationInfo(brui.operationInfo())//
				.addWorkPackageId(wp.get_id())//
				.setFolder_id(ds.getFolder_id())//
				.setName(wp.description)//
				.setTag(dt.getTag())//
				.setCategory(dt.getCategory());// ��Ҫ����

		String editorName = dt.getEditorName();
		if (Check.isAssigned(editorName)) {
			Document encodeDocument = docu.encodeDocument();
			Editor.open(editorName, context, encodeDocument, true, (r, t) -> {
				insert(gridPart, t);
			});
		} else {
			Editor.open("ͨ���ĵ��༭��", context, docu, (r, t) -> {
				gridPart.insert(Services.get(DocumentService.class).createDocument(t));
			});
		}

	}

	private void insert(GridPart gridPart, Document t) {
		UniversalCommand command = new UniversalCommand().setTargetClassName(Docu.class.getName())
				.addParameter(MethodParam.OBJECT, t).setTargetCollection("docu");
		UniversalResult ur = Services.get(UniversalDataService.class).insert(command);
		gridPart.insert(ur.getValue());
	}

	private void createGenaricDocument(final IBruiContext context) {
		WorkPackage wp = (WorkPackage) context.getInput();
		GridPart gridPart = (GridPart) context.getContent();
		ObjectId project_id = Services.get(WorkService.class).getProjectId(wp.getWork_id());
		// ѡ���ļ���
		Selector.open("��Ŀ�ļ���ѡ��", context, new Project().set_id(project_id), em -> {
			Docu docu = new Docu()//
					.setCreationInfo(brui.operationInfo())//
					.addWorkPackageId(wp.get_id())//
					.setFolder_id(((Folder) em.get(0)).get_id())//
					.setName(wp.description);
			Editor.open("ͨ���ĵ��༭��", context, docu, (r, t) -> {
				gridPart.insert(Services.get(DocumentService.class).createDocument(t));
			});
		});
	}

}
