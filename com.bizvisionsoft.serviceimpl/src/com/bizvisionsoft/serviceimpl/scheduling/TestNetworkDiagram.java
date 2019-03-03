package com.bizvisionsoft.serviceimpl.scheduling;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNetworkDiagram {
	
	private static Logger logger = LoggerFactory.getLogger(TestNetworkDiagram.class);


	public static void main(String[] args) {
		// 1. 首先要分析输入的task包括几张图。
//		test3();
		
		
	}

	/**
	 * 测试总成工作参与排程
	 */
	public static void test4() {
		Task a = new Task("a", 5);
		Task b = new Task("b");// 总成
		Task c = new Task("c", 13);
		Task d = new Task("d", 7);
		Task e = new Task("e", 7);
		Task f = new Task("f", 7);
		Task g = new Task("g");
		Task h = new Task("h", 7);
		Task i = new Task("i", 12);
		Task j = new Task("j", 6);
		
		b.setSubTasks(Arrays.asList(g, h));
		g.setSubTasks(Arrays.asList(j));

		Route a_b = new Route(a, b, new Relation(Relation.FTS, 6));
		Route a_c = new Route(a, c, new Relation(Relation.FTS, 7));
		Route d_b = new Route(d, b, new Relation(Relation.FTS, 6));
		Route c_e = new Route(c, e, new Relation(Relation.FTS, 7));

		Route g_h = new Route(g, h, new Relation(Relation.FTS, 7));
		Route h_f = new Route(h, f, new Relation(Relation.FTS, 2));
		
		Route i_g = new Route(i, g, new Relation(Relation.FTS, 5));
		
		Route j_e = new Route(j, e, new Relation(Relation.FTS, 2));


		Graphic gh = new Graphic(Arrays.asList(a, b, c, d, e, f, g, h, i), Arrays.asList(a_b, a_c, d_b, c_e, g_h, h_f,i_g,j_e));
		
		List<Route> start = gh.getStartRoute();
		logger.debug("起始节点：");
		logger.debug(""+start);
		// 得出的起始节点为a, d, i
		
		//假定项目开始时间为2018年6月1日
		Calendar pjStart = Calendar.getInstance();
		pjStart.set(2018, 5, 1);
		
		//a工作开始于2018年6月1日
		Calendar aStart = Calendar.getInstance();
		aStart.set(2018, 5, 1);
	
		//b工作开始于2018年6月2日
		Calendar dStart = Calendar.getInstance();
		dStart.set(2018, 5, 2);

		//i工作开始于2018年6月10日
		Calendar iStart = Calendar.getInstance();
		iStart.set(2018, 5, 10);

		//求出各个起点相对项目的延迟天数
		long timeInMillis = pjStart.getTimeInMillis();
		int intervalA = (int) ((aStart.getTimeInMillis()-timeInMillis)/(1000*60*60*24));
		int intervalD = (int) ((dStart.getTimeInMillis()-timeInMillis)/(1000*60*60*24));
		int intervalI = (int) ((iStart.getTimeInMillis()-timeInMillis)/(1000*60*60*24));
		
		gh.setStartInterval("a",intervalA);
		gh.setStartInterval("d",intervalD);
		gh.setStartInterval("i",intervalI);
		gh.schedule();
		
		//项目总工期为
		logger.debug("项目工期：");
		float pjDuration = gh.getT();
		logger.debug(""+pjDuration);
		pjStart.add(Calendar.DATE, (int)pjDuration);
		logger.debug("项目完工日期");
		logger.debug(new SimpleDateFormat("yyyy-MM-dd").format(pjStart.getTime()));
		
		//显示任务的计划
		displayTaskInfo(gh, "c");
		displayTaskInfo(gh, "d");

	}

	private static void displayTaskInfo(Graphic gh, String taskId) {
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		logger.debug("任务"+taskId+": ");
		Task taskC = gh.getTask(taskId);
		logger.debug("工期："+taskC.getD());
		
		Calendar cStart = Calendar.getInstance();
		cStart.set(2018, 5, 1);
		cStart.add(Calendar.DATE, taskC.getES().intValue());
		logger.debug("计划开始："+new SimpleDateFormat("yyyy-MM-dd").format(cStart.getTime()));

		Calendar cFinish = Calendar.getInstance();
		cFinish.set(2018, 5, 1);
		cFinish.add(Calendar.DATE, taskC.getEF().intValue());
		logger.debug("计划完成："+new SimpleDateFormat("yyyy-MM-dd").format(cFinish.getTime()));
		
		
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

	public static void test0() {
		Task start = Task.startTask();
		Task a = new Task("a", 5);
		Task b = new Task("b", 10);
		Task c = new Task("c", 13);
		Task d = new Task("d", 7);
		Task end = Task.endTask();

		Route start_a = new Route(start, a);
		Route a_b = new Route(a, b, new Relation(Relation.FTS, 6));
		Route a_c = new Route(a, c, new Relation(Relation.FTS, 7));
		Route a_d = new Route(a, d);
		Route b_d = new Route(b, d, new Relation(Relation.FTS, 3));

		List<Task> tasks = Arrays.asList(b, c, start, a, d, end);
		List<Route> routes = Arrays.asList(start_a, a_b, a_c, a_d, b_d);
		NetworkDiagram nd = new NetworkDiagram(tasks, routes);
		nd.schedule();
	}

	public static void test3() {
		// 任务
//		Task start = Task.startTask();
		Task a = new Task("a", 5);
		Task b = new Task("b", 8);
		Task c = new Task("c", 17);
		Task d = new Task("d", 12);
		Task e = new Task("e", 15);
		Task f = new Task("f", 25);
		Task g = new Task("g", 10);
//		Task end = Task.endTask();

		// 任务搭接关系
//		Route start_a = new Route(start, a);
		Route a_b = new Route(a, b, new Relation(Relation.STF, 6));
		Route b_e = new Route(b, e, new Relation(Relation.FTF, 10));
		Route e_g = new Route(e, g);
		Route a_c = new Route(a, c, new Relation(Relation.STS, 7));
		Route c_f = new Route(c, f, new Relation(Relation.FTF, 15));
		Route f_g = new Route(f, g, new Relation(Relation.STF, 10));
		Route a_d = new Route(a, d);
		Route b_d = new Route(b, d, new Relation(Relation.FTS, 3));
		Route d_g = new Route(d, g, new Relation(Relation.STS, 3), new Relation(Relation.FTF, 2));
//		Route g_end = new Route(g, end);

		// 风险
		Risk r1 = new Risk("R1", .3f, new Consequence(b, 12), new Consequence(c, 13));
		Risk r2 = new Risk("R2", .5f, new Consequence(d, 15));
		Risk r3 = new Risk("R3", .2f, new Consequence(g, 5), new Consequence(f, 6));
		Risk r4 = new Risk("R4", .4f, new Consequence(e, 17));
		r2.addSecondaryRisks(r3);// 添加次生风险

		List<Task> tasks = Arrays.asList( a, d, e, b, c, f, g);
		List<Route> routes = Arrays.asList( a_b, b_e, c_f, f_g, e_g, a_c, a_d, b_d, d_g);
		List<Risk> risks = Arrays.asList(r1, r2, r4);
		MonteCarloSimulate mcs = new MonteCarloSimulate(tasks, routes, risks,null);
		/////////////////////////////////////////////////// 调节模拟次数获得精确结果
		int times = 100000;
		////////////////////////////////////////////////////////////////////////////////
		logger.debug("考虑风险，进行蒙特卡罗模拟：" + times);

		long _s = System.currentTimeMillis();
		mcs.simulate(times);
		long _e = System.currentTimeMillis();
		logger.debug("完成模拟，耗时（秒）：" + ((_e - _s) / 1000));
		logger.debug("指标说明：");
		logger.debug("对于ACI 和ACP 都较高的工序,理应将其作为进度管理中的重中之重,此类工序不仅是实现工期赶工或压缩工");
		logger.debug("期的关键途经,此外,同时还应加强风险管理以消除或降低其不确定性影响;相反,而对于那些ACI 和ACP 都较低的工");
		logger.debug("序,则可相应降低管理要求。");
		logger.debug("对于ACP 高、ACI 低或是ACP 低、ACI 高的工序则应予以特殊考虑。");
		logger.debug("①对于前一种类型的工序,管理者应主要关注于如何缩短此类型工序的持续时间(比如对于那些资源驱动工期的");
		logger.debug("工序投入更多的资源) 。理由如下:ACP 高意味着此类型工序极有可能位于关键线路成为关键工序,也就是说此类型工");
		logger.debug("序往往成为影响项目总工期的“瓶颈”,是工期赶工或压缩能否实现的关键所在。另一方面,ACI 低则意味着此项工序并");
		logger.debug("非高风险,不作为工期风险管理的重点。");
		logger.debug("②后一类型的工序应列入风险防范及管理的重点,对其有必要采取强有力的进度风险管理措施。理由如下:风险");
		logger.debug("管理理论中,十分强调对于“稀少事件”的管理[7 ] ,这种类型的工序类似于“稀少事件”中被称为“零—无穷小”(Zero - In2");
		logger.debug("finity Dilemmas) 的风险情况(例如核电站的重大事故) :虽然其关键概率很小,但ACI 高很大程度上便意味着高风险,一旦");
		logger.debug("该工序位于关键线路上成为关键工序,它便会严重影响到项目总工期。");
		logger.debug("对于RCI指标高的风险意味着他的发生对项目进度的影响较大，应当重点关注");

		tasks.forEach(t -> logger.debug(t.getId() + "  ACI:" + t.getACI() + ", ACP:" + t.getACP()));
		risks.forEach(r -> logger.debug(r.id + " RCI:" + r.RCI + " ,T:" + r.T));

		logger.debug("无风险时，项目工期为：" + mcs.noRiskT);
		logger.debug("工期为：" + mcs.noRiskT + "天的概率为：");
		Integer cnt = mcs.TMap.get(mcs.noRiskT);
		if (cnt != null) {
			logger.debug(""+1f * cnt / times);
		}
	}

	public static void test1() {
		Task start = Task.startTask();
		Task a = new Task("a", 5);
		Task b = new Task("b", 10);
		Task c = new Task("c", 13);
		Task d = new Task("d", 17);
		Task e = new Task("e", 18);
		Task f = new Task("f", 27);
		Task g = new Task("g", 12);
		Task end = Task.endTask();

		Route start_a = new Route(start, a);
		Route a_b = new Route(a, b, new Relation(Relation.STF, 6));
		Route b_e = new Route(b, e, new Relation(Relation.FTF, 10));
		Route e_g = new Route(e, g);
		Route a_c = new Route(a, c, new Relation(Relation.STS, 7));
		Route c_f = new Route(c, f, new Relation(Relation.FTF, 15));
		Route f_g = new Route(f, g, new Relation(Relation.STF, 10));
		Route a_d = new Route(a, d);
		Route b_d = new Route(b, d, new Relation(Relation.FTS, 3));
		Route d_g = new Route(d, g, new Relation(Relation.STS, 3), new Relation(Relation.FTF, 2));
		Route g_end = new Route(g, end);

		List<Task> tasks = Arrays.asList(b, c, e, start, a, d, f, g, end);
		List<Route> routes = Arrays.asList(start_a, a_b, b_e, c_f, f_g, e_g, a_c, a_d, b_d, d_g, g_end);
		NetworkDiagram nd = new NetworkDiagram(tasks, routes);
		nd.schedule();
	}

	public static void test2() {
		// 任务
		Task start = Task.startTask();
		Task a = new Task("a", 5);
		Task b = new Task("b", 8);
		Task c = new Task("c", 10);
		Task d = new Task("d", 12);
		Task e = new Task("e", 15);
		Task f = new Task("f", 25);
		Task g = new Task("g", 10);
		Task end = Task.endTask();

		// 任务的前后搭接关系
		Route start_a = new Route(start, a);
		Route a_b = new Route(a, b, new Relation(Relation.STF, 6));
		Route b_e = new Route(b, e, new Relation(Relation.FTF, 10));
		Route e_g = new Route(e, g);
		Route a_c = new Route(a, c, new Relation(Relation.STS, 7));
		Route c_f = new Route(c, f, new Relation(Relation.FTF, 15));
		Route f_g = new Route(f, g, new Relation(Relation.STF, 10));
		Route a_d = new Route(a, d);
		Route b_d = new Route(b, d, new Relation(Relation.FTS, 3));
		Route d_g = new Route(d, g, new Relation(Relation.STS, 3), new Relation(Relation.FTF, 2));
		Route g_end = new Route(g, end);

		List<Task> tasks = Arrays.asList(start, a, d, e, b, c, f, g, end);
		List<Route> routes = Arrays.asList(start_a, a_b, b_e, c_f, f_g, e_g, a_c, a_d, b_d, d_g, g_end);

		// 进行排程
		// NetworkDiagram nd = new NetworkDiagram(tasks, routes);
		// nd.schedule();

		// 添加风险
		Risk r1 = new Risk("R1", .3f, new Consequence(b, 12), new Consequence(c, 13));
		Risk r2 = new Risk("R2", .5f, new Consequence(d, 15));
		Risk r3 = new Risk("R3", .2f, new Consequence(g, 5), new Consequence(f, 6));
		Risk r4 = new Risk("R4", .4f, new Consequence(e, 17));
		r2.addSecondaryRisks(r3);// r3是r2的次生风险。

		List<Risk> risks = Arrays.asList(r1, r2, r4);

		// 开始模拟
		MonteCarloSimulate mcs = new MonteCarloSimulate(tasks, routes, risks,null);
		mcs.simulate(1000);
	}
	

}
