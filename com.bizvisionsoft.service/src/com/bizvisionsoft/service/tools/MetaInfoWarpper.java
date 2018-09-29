package com.bizvisionsoft.service.tools;

import com.bizvisionsoft.service.model.UserMeta;

public class MetaInfoWarpper {

	public static String userInfo(UserMeta meta, String text) {
		if (meta == null) {
			return text;
		}
		String message = 
				meta.name + " [" + meta.userId + "]<br>" + //
				(meta.orgInfo == null ? "" : (meta.orgInfo + "<br>")) + //
				(meta.position == null ? "" : (meta.position + "<br>")) + //
				(meta.email == null ? "" : ("” œ‰£∫" + meta.email + "<br>")) + //
				(meta.tel == null ? "" : ("µÁª∞£∫" + meta.tel + "<br>")) ; //
		
		return warpper(text,message);
	}
	
	public static String warpper(String text,String message) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div onclick='layer.tips(\"" + //
				message+
				"\", this, {tips: [1, \"#3595CC\"]})'>");//
		sb.append(text);
		sb.append("</div>");
		return sb.toString();
	}

}
