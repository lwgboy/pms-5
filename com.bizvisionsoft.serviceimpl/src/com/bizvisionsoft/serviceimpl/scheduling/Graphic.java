package com.bizvisionsoft.serviceimpl.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graphic {

	private List<NetworkDiagram> nets;
	private List<Task> tasks;
	private float T;
	private Task start;
	private Task end;
	private Date startDate;

	public Graphic(List<Task> tasks, List<Route> routes) {
		this.tasks = tasks;
		start = Task.startTask();
		end = Task.endTask();
		nets = listConnectedSubgraphic(tasks, routes);
	}

	private <T> boolean addToList(List<T> src, T target) {
		if (!src.contains(target) && target != null) {
			src.add(target);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 计算图的多个连通子图
	 * 
	 * @param tasks
	 * @param routes
	 * @return
	 */
	public List<NetworkDiagram> listConnectedSubgraphic(List<Task> inputTasks, List<Route> inputRoutes) {
		final List<Route> routes = new ArrayList<>(inputRoutes);
		final List<Task> tasks = new ArrayList<>(inputTasks);
		final Set<Task> removedIn = new HashSet<Task>();
		final Set<Task> removedOut = new HashSet<Task>();

		// 路由降级
		inputTasks.stream().filter(t -> !t.getSubTasks().isEmpty()).forEach(sum -> {
			// 获得连入sumTaskd的连接
			inputRoutes.stream().filter(r -> sum.equals(r.end2)).forEach(in -> {
				linkToLeafTask(sum, in, routes);
				removedIn.add(in.end2);
			});
		});

		ArrayList<Route> _temp = new ArrayList<>(routes);

		inputTasks.stream().filter(t -> !t.getSubTasks().isEmpty()).forEach(sum -> {
			// 获得连出sumTaskd的连接
			_temp.stream().filter(r -> sum.equals(r.end1)).forEach(out -> {
				linkFromLeafTask(sum, out, routes);
				removedOut.add(out.end1);
			});
		});

		// 去掉含总成的连接
		routes.removeIf(r -> !r.end1.getSubTasks().isEmpty() || !r.end2.getSubTasks().isEmpty());

		List<NetworkDiagram> result = new ArrayList<NetworkDiagram>();
		// 处理子图
		for (int i = 0; i < routes.size(); i++) {
			Route route = routes.get(i);
			// 如果存在一个网络图包含这个路由的任意节点，需要跳过
			boolean contains = result.stream()
					.anyMatch(n -> !route.end1.equals(Task.startTask()) && !route.end2.equals(Task.endTask())
							&& (n.tasks.contains(route.end1) || n.tasks.contains(route.end2)));
			if (contains) {
				continue;
			}

			final ArrayList<Route> _routes = new ArrayList<Route>();
			_routes.add(route);

			Set<Task> nodes = new HashSet<Task>();
			searchNextFromRoute(routes, route, _routes, nodes);

			nodes = new HashSet<Task>();
			searchNextToRoute(routes, route, _routes, nodes);
			NetworkDiagram net = result.stream().filter(nd -> {
				ArrayList<Route> temp = new ArrayList<>(nd.routes);
				temp.retainAll(_routes);
				return temp.size() > 0;
			}).findFirst().orElse(null);
			if (net == null) {
				ArrayList<Task> _tasks = new ArrayList<Task>();
				_routes.forEach(r -> {
					addToList(_tasks, r.end1);
					addToList(_tasks, r.end2);
				});
				result.add(new NetworkDiagram(new ArrayList<Task>(_tasks), _routes));
			} else {
				_routes.forEach(r -> {
					addToList(net.tasks, r.end1);
					addToList(net.tasks, r.end2);
					addToList(net.routes, r);
				});
			}
		}
		// 处理孤立点
		tasks.stream().filter(t -> t.getSubTasks().isEmpty() && !removedIn.contains(t) && !removedOut.contains(t)
				&& !result.stream().anyMatch(n -> n.tasks.contains(t))).forEach(_t -> {
					NetworkDiagram nd = new NetworkDiagram(Arrays.asList(start, _t, end),
							Arrays.asList(new Route(start, _t), new Route(_t, end)));
					result.add(nd);
				});

		return result;
	}

	public List<Route> getStartRoute() {
		Set<Route> result = new HashSet<Route>();
		nets.forEach(
				n -> n.routes.stream().filter(r -> Task.START.equals(r.end1.getId())).forEach(_r -> result.add(_r)));
		return new ArrayList<Route>(Arrays.asList(result.toArray(new Route[0])));
	}

	public List<Task> getEndTask() {
		Set<Task> result = new HashSet<Task>();
		nets.forEach(
				n -> n.routes.stream().filter(r -> Task.END.equals(r.end2.getId())).forEach(_r -> result.add(_r.end1)));
		return new ArrayList<Task>(Arrays.asList(result.toArray(new Task[0])));
	}

	private void linkToLeafTask(Task parent, Route in, List<Route> routes) {
		parent.getSubTasks().forEach(t -> {
			if (t.getSubTasks().isEmpty()) {
				Route r = new Route(in.end1, t, in.relations.toArray(new Relation[0]));
				addRoute(routes, r);
			} else {
				linkToLeafTask(t, in, routes);
			}
		});
	}

	private void linkFromLeafTask(Task parent, Route out, List<Route> routes) {
		parent.getSubTasks().forEach(t -> {
			if (t.getSubTasks().isEmpty()) {
				Route r = new Route(t, out.end2, out.relations.toArray(new Relation[0]));
				addRoute(routes, r);
			} else {
				linkFromLeafTask(t, out, routes);
			}
		});

	}

	private void addRoute(List<Route> routes, Route route) {
		int idx = routes.indexOf(route);
		if (idx == -1) {
			routes.add(route);
		} else {
			routes.get(idx).relations.addAll(route.relations);
		}
	}

	private void searchNextFromRoute(List<Route> routes, Route inputRoute, ArrayList<Route> _routes, Set<Task> nodes) {
		Task to = inputRoute.end1;
		nodes.add(inputRoute.end2);

		Set<Route> _rt = new HashSet<Route>();
		for (int i = 0; i < routes.size(); i++) {
			Route r = routes.get(i);
			if (r.end2.equals(to) && !nodes.contains(to)) {
				if (addToList(_routes, r)) {
					_rt.add(r);
				}
			}
		}

		if (_rt.isEmpty()) {
			addToList(_routes, new Route(start, to));
		} else {
			_rt.stream().forEach(r -> searchNextFromRoute(routes, r, _routes, nodes));
		}

	}

	private void searchNextToRoute(List<Route> routes, Route inputRoute, ArrayList<Route> _routes, Set<Task> nodes) {
		Task from = inputRoute.end2;
		nodes.add(inputRoute.end1);

		Set<Route> _rt = new HashSet<Route>();
		for (int i = 0; i < routes.size(); i++) {
			Route r = routes.get(i);
			if (r.end1.equals(from) && !nodes.contains(from)) {
				if (addToList(_routes, r)) {
					_rt.add(r);
				}
			}
		}

		if (_rt.isEmpty()) {
			addToList(_routes, new Route(from, end));
		} else {
			_rt.stream().forEach(r -> searchNextToRoute(routes, r, _routes, nodes));
		}
	}

	public void schedule() {
		// 只对叶子排程
		nets.stream().filter(nd -> nd.tasks.stream().allMatch(t -> t.getSubTasks().isEmpty()))
				.forEach(pureNd -> pureNd.schedule());

		// 计算总成工期
		tasks.stream().filter(t -> !t.getSubTasks().isEmpty()).forEach(st -> calculteSummaryTask(st));

		// 总工期
		T = (float) nets.stream().mapToDouble(nd -> nd.T).max().getAsDouble();

	}

	private void calculteSummaryTask(Task task) {
		List<Task> subTasks = task.getSubTasks();
		for (int i = 0; i < subTasks.size(); i++) {
			Task sub = subTasks.get(i);
			if (!sub.getSubTasks().isEmpty()) {
				calculteSummaryTask(sub);
			}

			if (task.getES() == null || task.getES() > sub.getES()) {
				task.setES(sub.getES());
			}

			if (task.getEF() == null || task.getEF() < sub.getEF()) {
				task.setEF(sub.getEF());
			}

			if (task.getLS() == null || task.getLS() > sub.getLS()) {
				task.setLS(sub.getLS());
			}

			if (task.getLF() == null || task.getLF() < sub.getLF()) {
				task.setLF(sub.getLF());
			}

			task.setTF(task.getLS() - task.getES());

			task.setFF(0f);// 总成工作无需计算FF

		}

		task.setD(task.getLF() - task.getLS());
	}

	public void setStartInterval(String id, int interval) {
		Route route = getStartRoute().stream().filter(r -> r.end2.getId().equals(id)).findFirst().orElse(null);
		if (route == null) {
			throw new RuntimeException("任务" + id + ", 不是起始节点");
		}
		route.relations.get(0).interval = interval;
	}

	public float getT() {
		return T;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public Task getTask(String taskId) {
		return tasks.stream().filter(t -> t.getId().equals(taskId)).findFirst().orElse(null);
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setStartDate(String id, Date start) {
		int interval = (int) ((start.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
		setStartInterval(id, interval);
	}

	public Date getTaskEFDate(String taskId) {
		Task task = getTask(taskId);
		if (task == null) {
			throw new RuntimeException("任务" + taskId + "不存在。");
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.DATE, task.getEF().intValue());
		return cal.getTime();
	}

	public Date getFinishDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.DATE, (int) T);
		return cal.getTime();
	}

}
