package com.bizvisionsoft.service.tools;

import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.UserMeta;

public class MetaInfoWarpper {

	public static String userInfo(UserMeta meta, String text) {
		if (meta == null) {
			return text;
		}
		if (!Check.isNotAssigned(meta.headPics)) {
			String headPicHtml = "<img src=&#x27;" + meta.headPics.get(0).getURL(ServicesLoader.url)
					+ "&#x27; style=&#x27;border-radius:28px;width:48px;height:48px;&#x27;/>";
			String message = "<div style=&#x27;display:flex &#x27;>" + headPicHtml
					+ "<div style=&#x27;margin-left:8px&#x27;>" + meta.name + " [" + meta.userId + "]<br>" + //
					(meta.orgInfo == null ? "" : (meta.orgInfo + "<br>")) + //
					(meta.position == null ? "" : (meta.position + "<br>")) + //
					(meta.email == null ? "" : ("邮箱：" + meta.email + "<br>")) + //
					(meta.tel == null ? "" : ("电话：" + meta.tel + "<br>")) + //
					"</div></div>";
			return warpper(text, message);
		}else {
			String message = 
					meta.name + " [" + meta.userId + "]<br>" + //
					(meta.orgInfo == null ? "" : (meta.orgInfo + "<br>")) + //
					(meta.position == null ? "" : (meta.position + "<br>")) + //
					(meta.email == null ? "" : ("邮箱：" + meta.email + "<br>")) + //
					(meta.tel == null ? "" : ("电话：" + meta.tel + "<br>")) ;
			return warpper(text, message);
		}
	}

	public static String warpper(String text, String message) {
		return warpper(text, message,3000);
	}
	
	public static String warpper(String text, String message,int millsecond) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div onclick='layer.tips(\"" + //
				message + "\", this, {tips: [1, \"#3595CC\"],time:"+millsecond+",area:\"300px\"})'>");//
		sb.append(text);
		sb.append("</div>");
		return sb.toString();
	}

}
