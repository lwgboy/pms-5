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

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
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

public interface IWorkAction {

	public default Logger logger() {
		return LoggerFactory.getLogger(getClass());
	}

	public IBruiService getBruiService();

	public default WorkService getService() {
		return Services.get(WorkService.class);
	}

	/**
	 * ��ʼ����
	 * 
	 * @param work
	 * @param callback
	 */
	public default void startWork(Work work, Consumer<Work> callback) {
		if (getBruiService().confirm("��ʼ����", "��ȷ�Ͽ�ʼ����" + work + "��<br>ϵͳ����¼����ʱ��Ϊ������ʵ�ʿ�ʼʱ�䡣")) {
			Date date = getInputDate();
			if (date == null)
				return;
			List<Result> result = getService().startWork(getBruiService().command(work.get_id(), date, ICommand.Start_Work));
			if (result.isEmpty()) {
				Layer.message("����������");
				if (callback != null) {
					Work w = getService().getWork(work.get_id());
					callback.accept(w);
				}
			} else {
				String msg = result.stream().filter(r -> r.type == Result.TYPE_ERROR).map(r -> r.message + "<br>").reduce("",
						String::concat);
				getBruiService().error("��ʼ����", msg);
			}
		}
	}

	/**
	 * ��ɹ���
	 * 
	 * @param work
	 * @param callback
	 */
	public default void finishWork(Work work, Consumer<Boolean> callback) {
		if (getBruiService().confirm("��ɹ���", "��ȷ����ɹ�����" + work + "��")) {
			Date date = getInputDate();
			if (date == null)
				return;
			List<Result> result = getService().finishWork(getBruiService().command(work.get_id(), date, ICommand.Finish_Work));
			if (result.isEmpty()) {
				Layer.message("���������");
				if (callback != null) {
					callback.accept(true);
				}
			} else {
				String msg = result.stream().filter(r -> r.type == Result.TYPE_ERROR).map(r -> r.message + "<br>").reduce("",
						String::concat);
				getBruiService().error("��ɹ���", msg);
			}
		}
	}

	/**
	 * ָ�ɹ���
	 * 
	 * @param work
	 * @param context
	 * @param callback
	 */
	public default void assignWork(Work work, IBruiContext context, Consumer<Work> callback) {
		Selector.open("ָ���û�ѡ����", context, work, l -> {
			User user = (User) l.get(0);
			Work w = getService().assignUserToWorkChager(work.get_id(), user.getUserId());
			if (callback != null) {
				callback.accept(w);
			}
		});
	}

	/**
	 * ��鹤��
	 * 
	 * @param work
	 * @param context
	 * @param callback
	 */
	public default void checkWork(Work work, IBruiContext context, Consumer<Work> callback) {
		Document input = new Document();
		final Map<String, CheckItem> checklistMap = new LinkedHashMap<String, CheckItem>();

		// ��������
		EditorFactory ef = new EditorFactory().title("��������").labelWidth(64).labelAlignment(SWT.CENTER);
		for (CheckItem ci : work.getChecklist()) {
			String name = UUID.randomUUID().toString();
			String choiseFieldName = "choise-" + name;
			String remarkFieldName = "remark-" + name;
			FormField banner = new BannerFieldFactory().text(ci.getDescription()).get();

			FormField checkField = new RadioFieldFactory().setOptionValue("ͨ��#���#����").setOptionText("ͨ��#���#����").pack(true).text("��ѡ��")
					.name(choiseFieldName).get();
			FormField remarkField = new TextFieldFactory().text("˵��").name(remarkFieldName).get();
			FormField lineField = new LineFactory().setFields(checkField, remarkField).get();
			ef.appendField(banner).appendField(lineField);

			checklistMap.put(name, ci);
			input.put(choiseFieldName, ci.getChoise());
			input.put(remarkFieldName, ci.getRemark());
		}

		// �򿪼��༭��
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
					String signInfo = getBruiService().getCurrentUserInfo().getName() + " " + Formatter.getString(new Date());
					checkItem.setSignInfo(signInfo);
				}
			}
			Object checkItems = BsonTools.encodeBsonValue(new ArrayList<>(checklistMap.values()));
			FilterAndUpdate fu = new FilterAndUpdate().filter(filter).set(new BasicDBObject("checklist", checkItems));
			getService().updateWork(fu.bson());
			if (callback != null) {
				Work w = getService().getWork(work.get_id());
				callback.accept(w);
			}
		});
	}

	public default Date getInputDate() {
		Date date = new Date();
		if (logger().isDebugEnabled()) {
			DateTimeInputDialog dt = new DateTimeInputDialog(getBruiService().getCurrentShell(), "��ѡ��ʱ��", "", null)
					.setDateSetting(DateTimeSetting.dateTime());
			if (dt.open() == DateTimeInputDialog.OK) {
				date = dt.getValue();
			} else
				return null;
		}
		return date;
	}

}
