package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;
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
	@WriteValue("是否已读")
	private boolean read;

	@ReadValue
	@WriteValue
	private String sender;

	@ReadValue("发送者")
	private String senderInfo;

	private RemoteFile senderHeadPic;

	@ReadValue("头像")
	public String getSenderHeadImageURL() {
		if (senderHeadPic != null) {
			return senderHeadPic.getURL(ServicesLoader.url);
		}
		return null;
	}
	
	public RemoteFile getSenderHeadPic() {
		return senderHeadPic;
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
	public static Message distributeWorkMsg(String title,String pjName, Document work, boolean isCharger, String sender,
			String receiver) {
		Date pstart = work.getDate("planStart");
		Date pfinish = work.getDate("planFinish");
		String fname = work.getString("fullName");
		return Message.newInstance(title, "项目：" + pjName + " ，工作：" + fname + " ，计划开始：" + format(pstart) + " ，计划完成："
				+ format(pfinish) + (isCharger ? " ，该工作由您负责。" : "，您需在计划开始前指派该工作负责人。"), sender, receiver, null);
	}

	public static Message distributeStageMsg(String pjName, Document work, String sender, String receiver) {
		Date pstart = work.getDate("planStart");
		Date pfinish = work.getDate("planFinish");
		String fname = work.getString("fullName");
		return Message.newInstance("阶段计划下达通知", "项目：" + pjName + " ，阶段：" + fname + " ，计划开始：" + format(pstart) + " ，计划完成："
				+ format(pfinish) + " ，您需要在阶段计划开始以前完成本阶段启动。", sender, receiver, null);
	}

	public static Message workEventMsg(String pjName, Document work, String eventName, Date eventDate, String sender,
			String receiver) {
		String workType;
		if (work.getBoolean("milestone", false)) {
			workType = "里程碑";
		} else if (work.getBoolean("stage", false)) {
			workType = "阶段";
		} else {
			workType = "工作";
		}
		String subject = workType + eventName + "通知";
		String date = format(eventDate);
		String fname = work.getString("fullName");
		return Message.newInstance(subject, "项目：" + pjName + " ，" + workType + "：" + fname + " ，已于" + date + eventName,
				sender, receiver, null);
	}

	public static Message precedenceEventMsg(String pjName, Document srcWork, Document tgtWork, String type,
			boolean start, String receiver, String sender) {
		String subject = start ? "紧前工作开始通知" : "紧前工作完成通知";
		String tgtName = tgtWork.getString("fullName");
		String srcName = srcWork.getString("fullName");
		Date srcDate = start ? srcWork.getDate("actualStart") : srcWork.getDate("actualFinish");
		Date tgtDate = start ? tgtWork.getDate("planStart") : tgtWork.getDate("planFinish");
		String srcAction = start ? "开始" : "完成";
		String tgtAction;
		if (type.equals("FF")) {
			tgtAction = "完成";
		} else if ("FS".equals(type)) {
			tgtAction = "开始";
		} else if ("SS".equals(type)) {
			tgtAction = "开始";
		} else {
			tgtAction = "完成";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("项目:" + pjName + " ，工作:" + srcName + " 已于 " + format(srcDate) + srcAction + "。");
		sb.append("<br/>");
		sb.append("您负责的后序工作：" + tgtName + " ，计划" + tgtAction + "：" + format(tgtDate));
		return newInstance(subject, sb.toString(), sender, receiver, null);
	}

	public static String format(Date srcDate) {
		return new SimpleDateFormat("yyyy年MM月dd日").format(srcDate);
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getContent() {
		return content;
	}

	public boolean isRead() {
		return read;
	}
	
	public String getSenderInfo() {
		return senderInfo;
	}
	
	public Date getSendDate() {
		return sendDate;
	}
	
	public ObjectId get_id() {
		return _id;
	}
}
