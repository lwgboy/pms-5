package com.bizvisionsoft.pms.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.CBSSubject;
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

	static String[] name1 = { "变形机器人", "超级飞侠控制塔", "卡尔叔叔救援车厢套装", "巨神战击队", "疾速系列-风暴猎鹰", "铠甲勇士铠传", "铠甲勇士积木",
			"可调回旋悠悠球-天极战虎", "可动公仔-萌鸡大宇", "弹射变形双车空翻炼狱" };

	@Inject
	private IBruiService brui;

	private ProjectService projectService;

	private WorkSpaceService workSpaceService;

	private WorkService workService;

	private CommonService commonService;

	private CBSService cbsService;

	@Execute
	public void execute() {
		projectService = Services.get(ProjectService.class);

		workSpaceService = Services.get(WorkSpaceService.class);

		workService = Services.get(WorkService.class);

		commonService = Services.get(CommonService.class);

		cbsService = Services.get(CBSService.class);

		Project template = projectService.get(new ObjectId("5b1c2413e37dab4080433de6"));

		int year = 2017;

		for (int i = 0; i < 120; i++) {
			ObjectId project_id = new ObjectId();

			createProject(template, year, i, project_id);

			// 创建
			Project project = projectService.insert(template);
			System.out.println("创建项目:" + project);

			// 检出
			Workspace workspace = project.getWorkspace();
			workSpaceService.checkout(workspace, "zh", true);
			workspace = project.getWorkspace();
			System.out.println("检出项目:" + project);

			// 创建阶段
			Date start = project.getPlanStart();
			Date finish = project.getPlanFinish();
			WorkInfo stage1 = createStage("立项策划", project, workspace, start, null);
			System.out.println("创建" + stage1);
			WorkInfo stage2 = createStage("方案阶段", project, workspace, stage1.getPlanFinish(), finish);
			System.out.println("创建" + stage2);
			createWorkLinkInfo(project, workspace, stage1, stage2);
			System.out.println("创建阶段关联");
			WorkInfo work1 = createWork("编制立项策划书", workspace, stage1, start, null);
			WorkInfo work2 = createWork("编制总体方案", workspace, stage2, stage1.getPlanFinish(), finish);

			// 检入工作
			workSpaceService.checkin(workspace);
			System.out.println("检入工作");

			// 启动
			projectService.startProject(Command.newInstance("开始项目", "zh", start, project_id));
			System.out.println("开始项目" + project);

			projectService.distributeProjectPlan(Command.newInstance("下达项目计划", "zh", start, project_id));
			System.out.println("下达项目计划");

			workService.startStage(Command.newInstance("启动阶段", "zh", stage1.getPlanStart(), stage1.get_id()));
			workService.distributeWorkPlan(Command.newInstance("下达阶段计划", "zh", stage1.getPlanStart(), stage1.get_id()));
			workService.startWork(Command.newInstance("开始工作", "zh", start, work1.get_id()));
			System.out.println("开始工作" + work1);
			workService.finishWork(Command.newInstance("完成工作", "zh", finish, work1.get_id()));
			System.out.println("完成工作" + work1);
			workService.finishStage(Command.newInstance("完成阶段", "zh", stage1.getPlanFinish(), stage1.get_id()));
			workService.closeStage(Command.newInstance("关闭阶段", "zh", stage1.getPlanFinish(), stage1.get_id()));

			workService.startStage(Command.newInstance("启动阶段", "zh", stage2.getPlanStart(), stage2.get_id()));
			workService.distributeWorkPlan(Command.newInstance("下达阶段计划", "zh", stage2.getPlanStart(), stage2.get_id()));
			workService.startWork(Command.newInstance("开始工作", "zh", start, work2.get_id()));
			System.out.println("开始工作" + work2);
			workService.finishWork(Command.newInstance("完成工作", "zh", finish, work2.get_id()));
			System.out.println("完成工作" + work2);
			workService.finishStage(Command.newInstance("完成阶段", "zh", stage2.getPlanFinish(), stage2.get_id()));
			workService.closeStage(Command.newInstance("关闭阶段", "zh", stage2.getPlanFinish(), stage2.get_id()));

			createProjectBudgetAndCost(project, start, finish);

			projectService.finishProject(Command.newInstance("完成项目", "zh", finish, project_id));
			System.out.println("完成项目:" + project);

			projectService.closeProject(Command.newInstance("关闭项目:", "zh", finish, project_id));
			System.out.println("关闭项目:" + project);

		}

	}

	private void createProjectBudgetAndCost(Project project, Date start, Date finish) {
		List<AccountItem> accounts = commonService.getAccoutItemRoot();
		// 测试只考虑一级
		List<String> anos = new ArrayList<>();
		accounts.forEach(a -> anos.add(a.getId()));
		//考虑seed
		List<Double> seed = new ArrayList<Double>();
		accounts.forEach(a -> {
			if(a.getName().contains("财务")) {
				seed.add(0.5);
			}else if(a.getName().contains("制造")) {
				seed.add(3.0);
			}else if(a.getName().contains("研发")) {
				seed.add(1.5);
			}else if(a.getName().contains("设计")) {
				seed.add(1.5);
			}else if(a.getName().contains("管理")) {
				seed.add(0.1);
			}else {
				seed.add(1.0);
			}
		});

		Calendar _s = Calendar.getInstance();
		_s.setTime(start);

		Calendar _f = Calendar.getInstance();
		_f.setTime(finish);
		_f.add(Calendar.MONTH, 1);
		_f.set(Calendar.DATE, 1);
		_f.set(Calendar.HOUR_OF_DAY, 0);
		_f.set(Calendar.MINUTE, 0);
		_f.set(Calendar.SECOND, 0);
		_f.add(Calendar.SECOND, -1);
		
		while (_s.getTime().before(_f.getTime())) {
			String id = new SimpleDateFormat("yyyyMM").format(_s.getTime());

			ObjectId cbsItem_id = project.getCBS_id();

			for (int i = 0; i < anos.size(); i++) {
				CBSSubject cbsSubject = new CBSSubject();
				cbsSubject.setCBSItem_id(cbsItem_id).setBudget((double) new Random().nextInt(50)*seed.get(i))
						.setSubjectNumber(anos.get(i)).setId(id);
				cbsService.upsertCBSSubjectBudget(cbsSubject);
			}

			for (int i = 0; i < anos.size(); i++) {
				CBSSubject cbsSubject = new CBSSubject();
				cbsSubject.setCBSItem_id(cbsItem_id).setCost((double) new Random().nextInt(50)*seed.get(i))
						.setSubjectNumber(anos.get(i)).setId(id);
				cbsService.upsertCBSSubjectCost(cbsSubject);
			}
			_s.add(Calendar.MONTH, 1);
		}
		System.out.println("创建预算和成本数据");
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
