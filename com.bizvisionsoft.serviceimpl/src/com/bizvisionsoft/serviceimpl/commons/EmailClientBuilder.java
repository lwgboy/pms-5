package com.bizvisionsoft.serviceimpl.commons;

public class EmailClientBuilder {

	String emailType;

	public static final String HTML = "html";

	public static final String IMGHTML = "imghtml";

	public static final String MULTIPART = "multipart";

	public static final String SIMPLE = "simple";

	public EmailClientBuilder(String emailType) {
		this.emailType = emailType;
	}

	public EmailClientBuilder setEmailType(String emailType) {
		this.emailType = emailType;
		return this;
	}

	String charset = "GB2312";

	public EmailClientBuilder setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	String smtpHost;

	public EmailClientBuilder setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
		return this;
	}

	int smtpPort;

	public EmailClientBuilder setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
		return this;
	}

	boolean smtpUseSSL;

	public EmailClientBuilder setSmtpUseSSL(boolean smtpUseSSL) {
		this.smtpUseSSL = smtpUseSSL;
		return this;
	}

	String senderAddress, senderPassword;

	public EmailClientBuilder setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
		return this;
	}

	public EmailClientBuilder setSenderPassword(String senderPassword) {
		this.senderPassword = senderPassword;
		return this;
	}

	public EmailClient build() {
		return new EmailClient(this);
	}

}
