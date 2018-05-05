package com.bizvisionsoft.service.model;

import java.util.Date;

public class ServerInfo {

	private String requester;

	private Date requestTime;

	private String hostMessage;

	public ServerInfo(String requester) {
		this.requester = requester;
		this.requestTime = new Date();
	}

	public String getRequester() {
		return requester;
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public String getHostMessage() {
		return hostMessage;
	}

	public ServerInfo setHostMessage(String hostMessage) {
		this.hostMessage = hostMessage;
		return this;
	}

}
