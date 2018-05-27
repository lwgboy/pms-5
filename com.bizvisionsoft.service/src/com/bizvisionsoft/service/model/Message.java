package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("message")
public class Message {

	private ObjectId _id;

	@ReadValue("����")
	private String subject;

	@ReadValue("����")
	private String content;

	@ReadValue("��������")
	private Date sendDate;

	@ReadValue("�Ƿ��Ѷ�")
	private boolean read;

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

	private String receiver;

	@ReadValue("������")
	private String receiverInfo;

	@ReadValue("����")
	private String url;

	public ObjectId get_id() {
		return _id;
	}

	public Message set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public String getSubject() {
		return subject;
	}

	public Message setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public String getContent() {
		return content;
	}

	public Message setContent(String content) {
		this.content = content;
		return this;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public Message setSendDate(Date sendDate) {
		this.sendDate = sendDate;
		return this;
	}

	public boolean isRead() {
		return read;
	}

	public Message setRead(boolean read) {
		this.read = read;
		return this;
	}

	public String getSender() {
		return sender;
	}

	public Message setSender(String sender) {
		this.sender = sender;
		return this;
	}

	public String getReceiver() {
		return receiver;
	}

	public Message setReceiver(String receiver) {
		this.receiver = receiver;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Message setUrl(String url) {
		this.url = url;
		return this;
	}

}
