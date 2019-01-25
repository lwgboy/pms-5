package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class MessageRenderer {

	public static Document render(Message msg) {
		StringBuffer sb = new StringBuffer();

		RenderTools.appendSingleLineHeader(sb, new CardTheme(CardTheme.DEEP_GREY), msg.getSubject(), 36);

		RenderTools.appendText(sb, msg.getContent(), RenderTools.STYLE_3LINE);

		RenderTools.appendUserAndText(sb, msg.getSenderHeadImageURL(), msg.getSenderInfo(), Formatter.getString(msg.getSendDate()));

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ря╤а", "read");

		RenderTools.appendCardBg(sb);

		return new Document("html", sb.toString()).append("_id", msg.get_id());
	}

}
