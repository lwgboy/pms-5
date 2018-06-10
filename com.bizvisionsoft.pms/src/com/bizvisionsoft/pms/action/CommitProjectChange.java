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
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
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
	private IBruiService brui;

	private ProjectService projectService;

	private WorkSpaceService workSpaceService;

	private WorkService workService;

	@Execute
	public void execute() {
		projectService = Services.get(ProjectService.class);

		workSpaceService = Services.get(WorkSpaceService.class);

		workService = Services.get(WorkService.class);

		Project template = projectService.get(new ObjectId("5b1c2413e37dab4080433de6"));

		int year = 2017;

		for (int i = 0; i < 120; i++) {
			ObjectId project_id = new ObjectId();

			createProject(template, year, i, project_id);

			// ����
			Project project = projectService.insert(template);
			System.out.println("������Ŀ:" + project);

			// ���
			Workspace workspace = project.getWorkspace();
			workSpaceService.checkout(workspace, "zh", true);
			workspace = project.getWorkspace();
			System.out.println("�����Ŀ:" + project);

			// �����׶�
			Date start = project.getPlanStart();
			Date finish = project.getPlanFinish();
			WorkInfo stage1 = createStage("����߻�", project, workspace, start, null);
			System.out.println("����" + stage1);
			WorkInfo stage2 = createStage("�����׶�", project, workspace, stage1.getPlanFinish(), finish);
			System.out.println("����" + stage2);
			createWorkLinkInfo(project, workspace, stage1, stage2);
			System.out.println("�����׶ι���");
			WorkInfo work1 = createWork("��������߻���", workspace, stage1, start, null);
			WorkInfo work2 = createWork("�������巽��", workspace, stage2, stage1.getPlanFinish(), finish);

			// ���빤��
			workSpaceService.checkin(workspace);
			System.out.println("���빤��");

			// ����
			projectService.startProject(Command.newInstance("��ʼ��Ŀ", "zh", start, project_id));
			System.out.println("��ʼ��Ŀ"+project);

			projectService.distributeProjectPlan(Command.newInstance("�´���Ŀ�ƻ�", "zh", start, project_id));
			System.out.println("�´���Ŀ�ƻ�");
			
			workService.startStage(Command.newInstance("�����׶�", "zh", stage1.getPlanStart(), stage1.get_id()));
			workService.distributeWorkPlan(Command.newInstance("�´�׶μƻ�", "zh", stage1.getPlanStart(), stage1.get_id()));
			workService.startWork(Command.newInstance("��ʼ����", "zh", start, work1.get_id()));
			System.out.println("��ʼ����"+work1);
			workService.finishWork(Command.newInstance("��ɹ���", "zh", finish, work1.get_id()));
			System.out.println("��ɹ���"+work1);
			workService.finishStage(Command.newInstance("��ɽ׶�", "zh", stage1.getPlanFinish(), stage1.get_id()));
			workService.closeStage(Command.newInstance("�رս׶�", "zh", stage1.getPlanFinish(), stage1.get_id()));

			workService.startStage(Command.newInstance("�����׶�", "zh", stage2.getPlanStart(), stage1.get_id()));
			workService.distributeWorkPlan(Command.newInstance("�´�׶μƻ�", "zh", stage2.getPlanStart(), stage2.get_id()));
			workService.startWork(Command.newInstance("��ʼ����", "zh", start, work2.get_id()));
			System.out.println("��ʼ����"+work2);
			workService.finishWork(Command.newInstance("��ɹ���", "zh", finish, work2.get_id()));
			System.out.println("��ɹ���"+work2);
			workService.finishStage(Command.newInstance("��ɽ׶�", "zh", stage2.getPlanFinish(), stage2.get_id()));
			workService.closeStage(Command.newInstance("�رս׶�", "zh", stage2.getPlanFinish(), stage2.get_id()));

			projectService.finishProject(Command.newInstance("�����Ŀ", "zh", finish, project_id));
			System.out.println("�����Ŀ:" + project);

			projectService.closeProject(Command.newInstance("�ر���Ŀ:", "zh", finish, project_id));
			System.out.println("�ر���Ŀ:" + project);

		}

	}

	private WorkInfo createWork(String name, Workspace space, WorkInfo parent, Date start, Date finish) {
		if (finish == null) {
			Calendar c = Calendar.getInstance();
			c.setTime(start);
			c.add(Calendar.MONTH, 1);
			finish = c.getTime();
		}
		WorkInfo workInfo = WorkInfo.newInstance(parent).setPlanStart(start).setPlanFinish(finish).setText(name)
				.setChargerId("zh").setSpaceId(space.getSpace_id()).setStage(false);
		return workSpaceService.insertWork(workInfo);
	}

	private void createWorkLinkInfo(Project project, Workspace workspace, WorkInfo stage1, WorkInfo stage2) {
		WorkLinkInfo wi = WorkLinkInfo.newInstance(project.get_id()).setTarget(stage2).setSource(stage1)
				.setSpaceId(workspace.getSpace_id()).setLag(0);
		workSpaceService.insertLink(wi);
	}

	private WorkInfo createStage(String name, Project project, Workspace workspace, Date start, Date finish) {
		if (finish == null) {
			Calendar c = Calendar.getInstance();
			c.setTime(start);
			c.add(Calendar.MONTH, 1);
			finish = c.getTime();
		}

		WorkInfo workInfo = WorkInfo.newInstance(project).setPlanStart(start).setPlanFinish(finish).setText(name)
				.setChargerId("zh").setSpaceId(workspace.getSpace_id()).setStage(true);
		return workSpaceService.insertWork(workInfo);
	}

	private void createProject(Project project, int year, int i, ObjectId _id) {
		project.set_id(_id);
		project.setPmId("zh");
		project.setEps_id(eps[i % eps.length]);
		project.setStageEnable(true);

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
