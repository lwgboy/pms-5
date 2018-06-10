package com.bizvisionsoft.pms.action;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class CommitProjectChange {

	static ObjectId[] eps = new ObjectId[] { new ObjectId("5b1a93a8e37dab2ce441384f"),
			new ObjectId("5b1a9397e37dab2ce441384e"), new ObjectId("5b1a93afe37dab2ce4413850"),
			new ObjectId("5b1a93f7e37dab2ce4413852"), new ObjectId("5b1a9400e37dab2ce4413853"),
			new ObjectId("5b1a9400e37dab2ce4413854"), new ObjectId("5b1a9400e37dab2ce4413855"),
			new ObjectId("5b1a9400e37dab2ce4413856"), new ObjectId("5b1a9400e37dab2ce4413857") };

	static String[] name1 = { "���λ�����", "��������������", "���������Ԯ������װ", "����ս����", "����ϵ��-�籩��ӥ", "������ʿ����", "������ʿ��ľ",
			"�ɵ�����������-�켫ս��", "�ɶ�����-�ȼ�����", "�������˫���շ�����" };

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute() {
		ProjectService projectService = Services.get(ProjectService.class);

		WorkSpaceService workSpaceService = Services.get(WorkSpaceService.class);

		WorkService workService = Services.get(WorkService.class);

		Project template = projectService.get(new ObjectId("5b1c2413e37dab4080433de6"));

		int year = 2017;

		for (int i = 0; i < 120; i++) {
			ObjectId project_id = new ObjectId();

			createProject(template, year, i, project_id);

			// ����
			Project project = projectService.insert(template);
			System.out.println("������Ŀ");

			// ���
			Workspace workspace = project.getWorkspace();
			workSpaceService.checkout(workspace, "zh", true);
			System.out.println("�����Ŀ");
			workspace = project.getWorkspace();
			// ��ӹ���
			WorkInfo workInfo = WorkInfo.newInstance(project).setPlanStart(project.getPlanStart())
					.setPlanFinish(project.getPlanFinish()).setText("��ʾ����").setChargerId("zh")
					.setSpaceId(workspace.getSpace_id());
			workSpaceService.insertWork(workInfo);
			System.out.println("��������");

			workSpaceService.checkin(workspace);
			System.out.println("���빤��");

			// ����
			projectService.startProject(project_id, "zh");
			System.out.println("��ʼ��Ŀ");

			projectService.distributeProjectPlan(project_id, "zh");
			System.out.println("�´���Ŀ�ƻ�");

			workService.startWork(workInfo.get_id());
			System.out.println("��ʼ����");
			workService.finishWork(workInfo.get_id());
			System.out.println("��ɹ���");

			projectService.finishProject(project_id, "zh");
			System.out.println("�����Ŀ");

			projectService.closeProject(project_id, "zh");
			System.out.println("�ر���Ŀ");

		}

	}

	private void createProject(Project project, int year, int i, ObjectId _id) {
		project.set_id(_id);
		project.setPmId("zh");
		project.setEps_id(eps[i % eps.length]);

		Calendar cal = Calendar.getInstance();
		cal.set(year, i % 12, new Random().nextInt(29), 0, 0, 0);
		Date start = cal.getTime();
		project.setPlanStart(start);

		cal.add(Calendar.MONTH, 6);
		project.setPlanFinish(cal.getTime());

		project.setId(null);
		project.setName(name1[new Random().nextInt(name1.length)] + " - " + new Random().nextInt(9));
	}

}
