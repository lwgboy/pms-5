package com.bizvisionsoft.pms.work.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bson.Document;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.bruicommons.factory.assembly.EditorFactory;
import com.bizvisionsoft.bruicommons.factory.fields.BannerFieldFactory;
import com.bizvisionsoft.bruicommons.factory.fields.LineFactory;
import com.bizvisionsoft.bruicommons.factory.fields.RadioFieldFactory;
import com.bizvisionsoft.bruicommons.factory.fields.TextFieldFactory;
import com.bizvisionsoft.bruicommons.model.FormField;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.CheckItem;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public interface WorkAction {

	public default Logger logger() {
		return LoggerFactory.getLogger(getClass());
	}

	public IBruiService brui();

	public default WorkService service() {
		return Services.get(WorkService.class);
	}

	/**
	 * 开始工作
	 * 
	 * @param work
	 * @param callback
	 */
	public default void startWork(Work work, Consumer<Work> callback) {
		if (brui().confirm("启动工作", "请确认启动工作" + work + "。\n系统将记录现在时刻为工作的实际开始时间。")) {
			List<Result> result = service().startWork(brui().command(work.get_id(), getDate(), ICommand.Start_Work));
			if (result.isEmpty()) {
				Layer.message("工作已启动");
				if (callback != null) {
					Work w = service().getWork(work.get_id());
					callback.accept(w);
				}
			} else {
				String msg = result.stream().filter(r -> r.type == Result.TYPE_ERROR).map(r -> r.message + "<br>").reduce("",
						String::concat);
				brui().error("开始工作", msg);
			}
		}
	}

	/**
	 * 完成工作
	 * 
	 * @param work
	 * @param callback
	 */
	public default void finishWork(Work work, Consumer<Work> callback) {
		if (brui.confirm("完成工作", "请确认完成工作：" + work + "。")) {
			List<Result> result = service.finishWork(brui.command(work.get_id(), getDate("选择工作完成时间", "请选择工作完成时间"), ICommand.Finish_Work));
			if (result.isEmpty()) {
				Layer.message("工作已完成");
				if (callback != null) {
					Work w = service.getWork(work.get_id());
					callback.accept(w);
				}
			} else {
				String msg = result.stream().filter(r -> r.type == Result.TYPE_ERROR).map(r -> r.message + "<br>").reduce("",
						String::concat);
				brui.error("完成工作", msg);
			}
		}
	}

	/**
	 * 指派工作
	 * 
	 * @param work
	 * @param context
	 * @param callback
	 */
	public void assignWork(Work work, IBruiContext context, Consumer<Work> callback) {
		Selector.open("指派用户选择器", context, work, l -> {
			service.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", work.get_id()))
					.set(new BasicDBObject("chargerId", ((User) l.get(0)).getUserId())).bson());
			if (callback != null) {
				work.setChargerId(((User) l.get(0)).getUserId());
				callback.accept(work);
			}
		});
	}

	/**
	 * 检查工作
	 * 
	 * @param work
	 * @param context
	 * @param callback
	 */
	public void checkWork(Work work, IBruiContext context, Consumer<Work> callback) {
		Document input = new Document();
		final Map<String, CheckItem> checklistMap = new LinkedHashMap<String, CheckItem>();

		// 构建检查表
		EditorFactory ef = new EditorFactory().title("工作检查表").labelWidth(64).labelAlignment(SWT.CENTER);
		for (CheckItem ci : work.getChecklist()) {
			String name = UUID.randomUUID().toString();
			String choiseFieldName = "choise-" + name;
			String remarkFieldName = "remark-" + name;
			FormField banner = new BannerFieldFactory().text(ci.getDescription()).get();

			FormField checkField = new RadioFieldFactory().setOptionValue("通过#否决#待定").setOptionText("通过#否决#待定").pack(true).text("请选择")
					.name(choiseFieldName).get();
			FormField remarkField = new TextFieldFactory().text("说明").name(remarkFieldName).get();
			FormField lineField = new LineFactory().setFields(checkField, remarkField).get();
			ef.appendField(banner).appendField(lineField);

			checklistMap.put(name, ci);
			input.put(choiseFieldName, ci.getChoise());
			input.put(remarkFieldName, ci.getRemark());
		}

		// 打开检查编辑器
		new Editor<Document>(ef.get(), context).setEditable(true).setInput(input).ok((d, t) -> {
			BasicDBObject filter = new BasicDBObject("_id", work.get_id());

			Iterator<String> iter = t.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (key.startsWith("remark-")) {
					String remark = t.getString(key);
					CheckItem checkItem = checklistMap.get(key.substring(7));
					checkItem.setRemark(remark);
				} else if (key.startsWith("choise-")) {
					String choise = t.getString(key);
					CheckItem checkItem = checklistMap.get(key.substring(7));
					checkItem.setChoise(choise);
					String signInfo = brui.getCurrentUserInfo().getName() + " " + Formatter.getString(new Date());
					checkItem.setSignInfo(signInfo);
				}
			}
			Object checkItems = BsonTools.encodeBsonValue(new ArrayList<>(checklistMap.values()));
			FilterAndUpdate fu = new FilterAndUpdate().filter(filter).set(new BasicDBObject("checklist", checkItems));
			service.updateWork(fu.bson());
			if (callback != null) {
				Work w = service.getWork(work.get_id());
				callback.accept(w);
			}
		});
	}

	public default Date getInputDate() {
		Date date = new Date();
		if (logger().isDebugEnabled()) {
			DateTimeInputDialog dt = new DateTimeInputDialog(brui().getCurrentShell(), "请选择时间", "", null);
			if (dt.open() == DateTimeInputDialog.OK) {
				date = dt.getValue();
			}
		}
		return date;
	}

}
