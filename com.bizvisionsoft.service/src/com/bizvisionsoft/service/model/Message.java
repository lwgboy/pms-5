package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("message")
public class Message {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue("标题")
	private String subject;

	@ReadValue("内容")
	private String content;

	@ReadValue("发送日期")
	private Date sendDate;

	@ReadValue("是否已读")
	private boolean read;

	@ReadValue
	@WriteValue
	private String sender;

	@ReadValue("发送者")
	private String senderInfo;

	private RemoteFile senderHeadPic;

	@ReadValue("头像")
	private String getSenderHeadImageURL() {
		if (senderHeadPic != null) {
			return senderHeadPic.getURL(ServicesLoader.url);
		}
		return null;
	}

	@ReadValue
	@WriteValue
	private String receiver;

	@ReadValue("接收者")
	private String receiverInfo;

	@ReadValue("链接")
	private String url;

	public Message setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public Message setContent(String content) {
		this.content = content;
		return this;
	}

	public Message setSendDate(Date sendDate) {
		this.sendDate = sendDate;
		return this;
	}

	public Message setRead(boolean read) {
		this.read = read;
		return this;
	}

	public Message setSender(String sender) {
		this.sender = sender;
		return this;
	}

	public Message setReceiver(String receiver) {
		this.receiver = receiver;
		return this;
	}

	public Message setUrl(String url) {
		this.url = url;
		return this;
	}

	public static Message newInstance(String subject, String content, String sender, String receiver, String url) {
		return new Message().setSendDate(new Date()).setSubject(subject).setContent(content).setSender(sender)
				.setReceiver(receiver).setUrl(url);
	}

}
