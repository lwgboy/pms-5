package com.bizvisionsoft.service.dps;

import java.util.List;
import java.util.Map;

public interface EmailSender {
	
	public static String EMAIL_TYPE_HTML = "html";

	public static String EMAIL_TYPE_IMAGE_HTML = "imghtml";

	public static String EMAIL_TYPE_MULTIPART = "multipart";

	public static String EMAIL_TYPE_TEXT = "text";

	void send(String appName, String emailType, String to, String subject, String emailBody, String from)
			throws Exception;

	void send(String appName, String emailType, boolean useServerAddress, List<String> to, List<String> cc,
			String subject, String emailBody, String from, List<Map<String, Object>> attachment) throws Exception;

	void send(String appName, String to, String subject, String emailBody, String from) throws Exception;

}
