package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.tools.Util;

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

	/**
	 * 下达工作计划的通知模板
	 * 
	 * @param pjName
	 * @param work
	 * @param isCharger
	 * @param sender
	 * @param receiver
	 * @return
	 */
	public static Message distributeWorkMsg(String pjName, Document work, boolean isCharger, String sender,
			String receiver) {
		Date pstart = work.getDate("planStart");
		Date pfinish = work.getDate("planFinish");
		String fname = work.getString("fullName");
		return Message.newInstance("工作计划下达通知",
				"项目：" + pjName + "，工作：" + fname + "，计划开始：" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pstart)
						+ "，计划完成：" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pfinish)
						+ (isCharger ? "，该工作由您负责。" : "，您需在计划开始前指派该工作负责人。"),
				sender, receiver, null);
	}

	public static Message distributeStageMsg(String pjName, Document work, String sender, String receiver) {
		Date pstart = work.getDate("planStart");
		Date pfinish = work.getDate("planFinish");
		String fname = work.getString("fullName");
		return Message.newInstance("阶段计划下达通知",
				"项目：" + pjName + "，阶段：" + fname + "，计划开始：" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pstart)
						+ "，计划完成：" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pfinish)
						+ "，您需要在阶段计划开始以前完成本阶段启动。",
				sender, receiver, null);
	}

}
