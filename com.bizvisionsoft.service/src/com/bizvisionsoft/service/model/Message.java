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

	@ReadValue("����")
	private String subject;

	@ReadValue("����")
	private String content;

	@ReadValue("��������")
	private Date sendDate;

	@ReadValue("�Ƿ��Ѷ�")
	@WriteValue("�Ƿ��Ѷ�")
	private boolean read;

	@ReadValue
	@WriteValue
	private String sender;

	@ReadValue("������")
	private String senderInfo;

	private RemoteFile senderHeadPic;

	@ReadValue("ͷ��")
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
	public static Message distributeWorkMsg(String title,String pjName, Document work, boolean isCharger, String sender,
			String receiver) {
		Date pstart = work.getDate("planStart");
		Date pfinish = work.getDate("planFinish");
		String fname = work.getString("fullName");
		return Message.newInstance(title, "��Ŀ��" + pjName + " ��������" + fname + " ���ƻ���ʼ��" + format(pstart) + " ���ƻ���ɣ�"
				+ format(pfinish) + (isCharger ? " ���ù�����������" : "�������ڼƻ���ʼǰָ�ɸù��������ˡ�"), sender, receiver, null);
	}

	public static Message distributeStageMsg(String pjName, Document work, String sender, String receiver) {
		Date pstart = work.getDate("planStart");
		Date pfinish = work.getDate("planFinish");
		String fname = work.getString("fullName");
		return Message.newInstance("�׶μƻ��´�֪ͨ", "��Ŀ��" + pjName + " ���׶Σ�" + fname + " ���ƻ���ʼ��" + format(pstart) + " ���ƻ���ɣ�"
				+ format(pfinish) + " ������Ҫ�ڽ׶μƻ���ʼ��ǰ��ɱ��׶�������", sender, receiver, null);
	}

	public static Message workEventMsg(String pjName, Document work, String eventName, Date eventDate, String sender,
			String receiver) {
		String workType;
		if (work.getBoolean("milestone", false)) {
			workType = "��̱�";
		} else if (work.getBoolean("stage", false)) {
			workType = "�׶�";
		} else {
			workType = "����";
		}
		String subject = workType + eventName + "֪ͨ";
		String date = format(eventDate);
		String fname = work.getString("fullName");
		return Message.newInstance(subject, "��Ŀ��" + pjName + " ��" + workType + "��" + fname + " ������" + date + eventName,
				sender, receiver, null);
	}

	public static Message precedenceEventMsg(String pjName, Document srcWork, Document tgtWork, String type,
			boolean start, String receiver, String sender) {
		String subject = start ? "��ǰ������ʼ֪ͨ" : "��ǰ�������֪ͨ";
		String tgtName = tgtWork.getString("fullName");
		String srcName = srcWork.getString("fullName");
		Date srcDate = start ? srcWork.getDate("actualStart") : srcWork.getDate("actualFinish");
		Date tgtDate = start ? tgtWork.getDate("planStart") : tgtWork.getDate("planFinish");
		String srcAction = start ? "��ʼ" : "���";
		String tgtAction;
		if (type.equals("FF")) {
			tgtAction = "���";
		} else if ("FS".equals(type)) {
			tgtAction = "��ʼ";
		} else if ("SS".equals(type)) {
			tgtAction = "��ʼ";
		} else {
			tgtAction = "���";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("��Ŀ:" + pjName + " ������:" + srcName + " ���� " + format(srcDate) + srcAction + "��");
		sb.append("<br/>");
		sb.append("������ĺ�������" + tgtName + " ���ƻ�" + tgtAction + "��" + format(tgtDate));
		return newInstance(subject, sb.toString(), sender, receiver, null);
	}

	public static String format(Date srcDate) {
		return new SimpleDateFormat("yyyy��MM��dd��").format(srcDate);
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
