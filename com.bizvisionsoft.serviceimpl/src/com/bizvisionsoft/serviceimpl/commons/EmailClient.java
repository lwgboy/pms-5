package com.bizvisionsoft.serviceimpl.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

public class EmailClient {

	private Email email;

	private EmailClientBuilder option;

	EmailClient(EmailClientBuilder option) {
		createEmail(option);
	}

	private Email createEmail(EmailClientBuilder option) {
//        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
//        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
//        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
//        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
//        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
//        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
//        CommandMap.setDefaultCommandMap(mc);
		
		this.option = option;
		if ("html".equals(option.emailType)) {
			email = new HtmlEmail();
		} else if ("imghtml".equals(option.emailType)) {
			email = new ImageHtmlEmail();
		} else if ("multipart".equals(option.emailType)) {
			email = new MultiPartEmail();
		} else {
			email = new SimpleEmail();
		}
		email.setCharset(option.charset);
		email.setHostName(option.smtpHost);
		email.setSmtpPort(option.smtpPort);
		email.setSSLOnConnect(option.smtpUseSSL);
		if (option.senderAddress != null) {
			email.setAuthentication(option.senderAddress, option.senderPassword);
		}
		return email;
	}

	public String send() throws Exception {
		return email.send();
	}

	public EmailClient setMessage(String message) throws Exception {
		email.setMsg(message);
		if (email instanceof HtmlEmail) {
			((HtmlEmail) email).setHtmlMsg(message);
		}
		return this;
	}

	public EmailClient setSubject(String aSubject) {
		email.setSubject(aSubject);
		return this;
	}

	public EmailClient addTo(NamedAccount... accounts) throws Exception {
		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i].name != null) {
				email.addTo(accounts[i].address, accounts[i].name);
			} else {
				email.addTo(accounts[i].address);
			}
		}
		return this;
	}

	public EmailClient addCc(NamedAccount... accounts) throws Exception {
		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i].name != null) {
				email.addCc(accounts[i].address, accounts[i].name);
			} else {
				email.addCc(accounts[i].address);
			}
		}
		return this;
	}

	public EmailClient setFrom(NamedAccount account) throws Exception {
		if (account.name != null) {
			email.setFrom(account.address, account.name);
		} else {
			email.setFrom(account.address);
		}
		return this;
	}

	public EmailClient addAttachment(URL url, String fileName) throws Exception {
		if (email instanceof MultiPartEmail) {
			EmailAttachment attachment = new EmailAttachment();
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.setURL(url);
			attachment.setName(MimeUtility.encodeText(fileName, "gb2312", "b"));
			((MultiPartEmail) email).attach(attachment);
		} else {
			option.emailType = EmailClientBuilder.MULTIPART;
			email = createEmail(option);
			addAttachment(url, fileName);
		}
		return this;
	}

	public EmailClient addAttachment(String filePath) throws Exception {
		if (email instanceof MultiPartEmail) {
			EmailAttachment attachment = new EmailAttachment();
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			File file = new File(filePath);
			attachment.setPath(filePath);
			attachment.setName(MimeUtility.encodeText(file.getName(), "gb2312", "b"));
			((MultiPartEmail) email).attach(attachment);
		} else {
			option.emailType = EmailClientBuilder.MULTIPART;
			email = createEmail(option);
			addAttachment(filePath);
		}
		return this;
	}

	public EmailClient addAttachment(DataHandler dataHandler, String fileName) throws Exception {
		if (email instanceof MultiPartEmail) {
			EmailAttachment attachment = new EmailAttachment();
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			File file = File.createTempFile("email_", "_" + System.currentTimeMillis());
			writeToFile(dataHandler, file);
			attachment.setPath(file.getPath());
			attachment.setName(MimeUtility.encodeText(file.getName(), "gb2312", "b"));
			((MultiPartEmail) email).attach(attachment);
		} else {
			option.emailType = EmailClientBuilder.MULTIPART;
			email = createEmail(option);
			addAttachment(dataHandler, fileName);
		}
		return this;
	}

	private void writeToFile(DataHandler dataHandler, File file) throws IOException {
		InputStream is = dataHandler.getInputStream();
		OutputStream os = new FileOutputStream(file);
		byte[] bytes = new byte[1024];
		int c;
		while ((c = is.read(bytes)) != -1) {
			os.write(bytes, 0, c);
		}
		os.close();
		is.close();
	}

}
