package com.bizvisionsoft.pms.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.pms.project.SwitchPage;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public abstract class AbstractWorkCardRender {

	private IBruiService br;

	private GridTreeViewer viewer;

	private BruiAssemblyContext context;

	protected void init() {
		br = getBruiService();
		context = getContext();
	}

	protected void uiCreated() {
		viewer = getViewer();
		viewer.getGrid().getParent().setBackground(null);
		viewer.getGrid().setData(RWT.CUSTOM_VARIANT, "board");
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				Work work = (Work) ((GridItem) e.item).getData();
				if (e.text.startsWith("startWork/")) {
					startWork(work);
				} else if (e.text.startsWith("finishWork/")) {
					finishWork(work);
				} else {
					if (e.text.startsWith("openWorkPackage/")) {
						String idx = e.text.split("/")[1];
						openWorkPackage(work, idx);
					} else if (e.text.startsWith("assignWork/")) {
						assignWork(work);
					} else if (e.text.startsWith("openProject/")) {
						openProject(work);
					}
				}
			}
		});
	}

	private void openProject(Work work) {
		SwitchPage.openProject(br, work.getProject_id());
	}

	protected GridTreeViewer getViewer() {
		return (GridTreeViewer) context.getContent("viewer");
	}

	protected abstract BruiAssemblyContext getContext();

	protected abstract IBruiService getBruiService();

	private void openWorkPackage(Work work, String idx) {
		if ("default".equals(idx)) {
			br.openContent(br.getAssembly("工作包计划"), new Object[] { work, null });
		} else {
			List<TrackView> wps = work.getWorkPackageSetting();
			br.openContent(br.getAssembly("工作包计划"), new Object[] { work, wps.get(Integer.parseInt(idx)) });
		}
	}

	private void assignWork(Work work) {
		Selector.open("指派用户选择器", context, work, l -> {
			WorkService workService = ServicesLoader.get(WorkService.class);
			workService.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", work.get_id()))
					.set(new BasicDBObject("chargerId", ((User) l.get(0)).getUserId())).bson());

			work.setChargerId(((User) l.get(0)).getUserId());
			viewer.remove(work);
			br.updateSidebarActionBudget("指派工作");

			Optional<Object> optional = context.getParentContext().searchContent(new Predicate<IBruiContext>() {
				@Override
				public boolean test(IBruiContext t) {
					return Check.equals("planned", ((BruiAssemblyContext) t).getName());
				}
			}, IBruiContext.SEARCH_DOWN);
			if (optional.isPresent()) {
				Work w = Services.get(WorkService.class).getWork(work.get_id());
				((GridPart) optional.get()).insert(w);
			}
		});
	}

	private void finishWork(Work work) {
		if (br.confirm("完成工作", "请确认完成工作：" + work + "</span>。<br>系统将记录现在时刻为工作的实际完成时间。")) {
			WorkService workService = Services.get(WorkService.class);
			List<Result> result = workService.finishWork(br.command(work.get_id(), new Date(), ICommand.Finish_Work));
			if (result.isEmpty()) {
				Layer.message("工作已完成");
				viewer.remove(work);
				br.updateSidebarActionBudget("处理工作");

				Optional<Object> optional = context.getParentContext().searchContent(new Predicate<IBruiContext>() {
					@Override
					public boolean test(IBruiContext t) {
						return Check.equals("finished", ((BruiAssemblyContext) t).getName());
					}
				}, IBruiContext.SEARCH_DOWN);
				if (optional.isPresent()) {
					work = Services.get(WorkService.class).getWork(work.get_id());
					((GridPart) optional.get()).insert(work);
				}
			}
		}
	}

	private void startWork(Work work) {
		if (br.confirm("启动工作", "请确认启动工作" + work + "。<br>系统将记录现在时刻为工作的实际开始时间。")) {
			WorkService workService = Services.get(WorkService.class);
			List<Result> result = workService.startWork(br.command(work.get_id(), new Date(), ICommand.Start_Work));
			if (result.isEmpty()) {
				Layer.message("工作已启动");
				viewer.remove(work);

				Optional<Object> optional = context.getParentContext().searchContent(new Predicate<IBruiContext>() {
					@Override
					public boolean test(IBruiContext t) {
						return Check.equals("executing", ((BruiAssemblyContext) t).getName());
					}
				}, IBruiContext.SEARCH_DOWN);
				if (optional.isPresent()) {
					work = Services.get(WorkService.class).getWork(work.get_id());
					((GridPart) optional.get()).insert(work);
				}
			}
		}
	}

	protected void renderNoticeBudgets(StringBuffer sb, Work work) {
		sb.append("<div style='margin-top:8px;padding:4px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>");
		Double value = work.getTF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>TF " + (int) Math.ceil(value) + "</div>";
			sb.append(MetaInfoWarpper.warpper(label,
					"总时差（TF）：<br>在不影响总工期的前提下，本工作可以利用的机动时间，即工作的最迟开始时间与最早开始时间之差。利用这段时间延长工作的持续时间或推迟其开工时间，不会影响计划的总工期。", 3000));
		}

		value = work.getFF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>FF " + (int) Math.ceil(value) + "</div>";
			sb.append(
					MetaInfoWarpper.warpper(label, "自由时差（FF）：<br>在不影响其紧后工作最早开始时间的条件下，本工作可以利用的机动时间。即该工作的所有紧后工作的最早开始时间，减去该工作的最早结束时间。", 3000));
		}

		value = work.getTF();
		if (value != null && value.doubleValue()==0) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>CP</div>";
			sb.append(
					MetaInfoWarpper.warpper(label, "本工作处于项目关键路径", 3000));
		}

		Check.isAssigned(work.getManageLevel(), l -> {
			if ("1".equals(l)) {
				String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>&#8544;</div>";
				sb.append(MetaInfoWarpper.warpper(label, "这是一个1级管理级别的工作。", 3000));
			}

			if ("2".equals(l)) {
				String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>&#8545;</div>";
				sb.append(MetaInfoWarpper.warpper(label, "这是一个2级管理级别的工作。", 3000));
			}
		});
		// 警告
		Check.isAssigned(work.getWarningIcon(), sb::append);
		//

		sb.append("</div>");
	}

	protected void renderButtons(CardTheme theme, StringBuffer sb, Work work, String label, String href) {
		renderButtons(theme, sb, work, true, label, href);
	}

	protected void renderButtons(CardTheme theme, StringBuffer sb, Work work, boolean showActionButton, String label, String href) {
		List<TrackView> wps = work.getWorkPackageSetting();
		List<String[]> btns = new ArrayList<>();
		if (Check.isNotAssigned(wps)) {
			btns.add(new String[] { "openWorkPackage/default", "工作包" });
		} else if (wps.size() == 1) {
			btns.add(new String[] { "openWorkPackage/0", wps.get(0).getName() });
		} else {
			for (int i = 0; i < wps.size(); i++) {
				btns.add(new String[] { "openWorkPackage/" + i, wps.get(i).getName() });
			}
		}
		// sb.append("<div
		// style='margin-top:12px;width:100%;background:#d0d0d0;height:1px;'></div>");
		sb.append("<div style='margin-top:16px;padding:4px;display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		btns.forEach(e -> {
			sb.append("<a class='label_card' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		if (showActionButton)
			sb.append(
					"<a class='label_card' style='color:#" + theme.headBgColor + ";' href='" + href + "' target='_rwt'>" + label + "</a>");
		sb.append("</div>");
	}

	protected void renderIndicators(CardTheme theme, StringBuffer sb, String label1, double ind1, String label2, double ind2) {
		sb.append("<div style='padding:4px;display:flex;width:100%;justify-content:space-evenly;align-items:center;'>");

		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind1 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label1 + "</div></div>");

		sb.append("<div style='background:#d0d0d0;width:1px;height:80px'></div>");
		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind2 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label2 + "</div></div>");

		sb.append("</div>");
	}

	protected void renderTitle(CardTheme theme, StringBuffer sb, Work work) {
		String name = work.getFullName();
		// name = "详细设计系统模型设计和测试";
		sb.append("<div class='label_title brui_card_head' style='display:flex;height:64px;background:#" + theme.headBgColor + ";color:#"
				+ theme.headFgColor + ";padding:8px'>" + "<div style='word-break:break-word;white-space:pre-line;'>" + name
				+ "</div></div>");
	}

	protected void renderIconTextLine(StringBuffer sb, String text, String icon, String color) {
		sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'><img src='" + br.getResourceURL(icon)
				+ "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#" + color
				+ ";margin-left:8px;width:100%'>" + text + "</div></div>");
	}

	protected void renderProjectLine(CardTheme theme, StringBuffer sb, Work work) {
		sb.append("<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='"
				+ br.getResourceURL("img/project_c.svg")
				+ "' width='20' height='20'><a href='openProject/' target='_rwt' class='label_caption brui_text_line' style='color:#"
				+ theme.lightText + ";margin-left:8px;width:100%'>项目：" + work.getProjectName() + "</a></div>");
	}

	protected void renderCharger(CardTheme theme, StringBuffer sb, Work work) {
		sb.append("<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='"
				+ br.getResourceURL("img/user_c.svg") + "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#"
				+ theme.emphasizeText + ";margin-left:8px;width:100%;display:flex;'>负责：<span style='cursor:pointer;'>"
				+ work.warpperChargerInfo() + "</span></div></div>");
	}

}
