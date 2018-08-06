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

	@ReadValue("����")
	private String subject;

	@ReadValue("����")
	private String content;

	@ReadValue("��������")
	private Date sendDate;

	@ReadValue("�Ƿ��Ѷ�")
	private boolean read;

	@ReadValue
	@WriteValue
	private String sender;

	@ReadValue("������")
	private String senderInfo;

	private RemoteFile senderHeadPic;

	@ReadValue("ͷ��")
	private String getSenderHeadImageURL() {
		if (senderHeadPic != null) {
			return senderHeadPic.getURL(ServicesLoader.url);
		}
		return null;
	}

	@ReadValue
	@WriteValue
	private String receiver;

	@ReadValue("������")
	private String receiverInfo;

	@ReadValue("����")
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
	 * �´﹤���ƻ���֪ͨģ��
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
		return Message.newInstance("�����ƻ��´�֪ͨ",
				"��Ŀ��" + pjName + "��������" + fname + "���ƻ���ʼ��" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pstart)
						+ "���ƻ���ɣ�" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pfinish)
						+ (isCharger ? "���ù�����������" : "�������ڼƻ���ʼǰָ�ɸù��������ˡ�"),
				sender, receiver, null);
	}

	public static Message distributeStageMsg(String pjName, Document work, String sender, String receiver) {
		Date pstart = work.getDate("planStart");
		Date pfinish = work.getDate("planFinish");
		String fname = work.getString("fullName");
		return Message.newInstance("�׶μƻ��´�֪ͨ",
				"��Ŀ��" + pjName + "���׶Σ�" + fname + "���ƻ���ʼ��" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pstart)
						+ "���ƻ���ɣ�" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(pfinish)
						+ "������Ҫ�ڽ׶μƻ���ʼ��ǰ��ɱ��׶�������",
				sender, receiver, null);
	}

}
