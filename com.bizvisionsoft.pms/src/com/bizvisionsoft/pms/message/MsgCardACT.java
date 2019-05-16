package com.bizvisionsoft.pms.message;

import java.util.Date;
import java.util.Optional;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class MsgCardACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e) {
		ObjectId _id = element.getObjectId("_id");
		CommonService service = Services.get(CommonService.class);
		Message msg = service.getMessage(_id, br.getDomain());
		
		if ("read".equals(e.text)) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("read", true)).bson();
			service.updateMessage(fu, br.getDomain());
			GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
			viewer.remove(element);
			// ɾ��input�д洢�Ķ���
			((List<?>) viewer.getInput()).remove(element);
			return;
		}

		StringBuffer sb = new StringBuffer();

		// ͷ��
		sb.append("<div style='display:block;'>");
		String senderName = Optional.ofNullable(msg.getSenderInfo()).orElse("ϵͳ");// .substring(0, sender.indexOf("[")).trim();

		// ������
		sb.append("<div> ");

		String subject = msg.getSubject();
		sb.append("<div>�����ߣ�" + senderName + "</div>");
		Date sendDate = msg.getSendDate();
		sb.append("<div>�������ڣ�" + Formatter.getString(sendDate, "yyyy-MM-dd HH:mm:ss", RWT.getLocale()) + "</div>");
		sb.append("</div>");

		sb.append("<hr>");

		String content = msg.getContent();
		sb.append("<div style='White-space:normal;word-wrap:break-word;overflow:auto;;margin-top:8px'>" + content + "</div>");

		sb.append("</div>");
		Layer.alert(subject, sb.toString(), 460, 300);

	}

}
