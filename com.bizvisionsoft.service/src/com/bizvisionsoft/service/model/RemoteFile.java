package com.bizvisionsoft.service.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;

public class RemoteFile implements JsonExternalizable {

	@Persistence
	public ObjectId _id;

	@Persistence
	public String name;

	@Persistence
	public String namepace;

	@Persistence
	public String contentType;

	public String domain;

	public String getServerSideURL(String baseURL) {
		try {
			return baseURL + "/fs/" + domain + "/" + namepace + "/" + _id + "/" + URLEncoder.encode(name, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public String getClientSideURL(String sid) {
		return createClientSideURL(sid, _id.toHexString(), namepace, name, domain);
	}

	public static String createClientSideURL(String _id, String namespace, String name, String domain) {
		return createClientSideURL("rwt", _id, namespace, name, domain);
	}

	public static String createClientSideURL(String sid, String _id, String namespace, String name, String domain) {
		if (sid == null) {
			sid = "rwt";
		}
		return "/bvs/fs?id=" + _id + "&namespace=" + namespace + "&name=" + name + "&sid=" + sid + "&domain=" + domain;
	}

	public static String createClientSideURL(Document doc, String domain) {
		return createClientSideURL(doc, domain, null);
	}

	public static String createClientSideURL(Document doc, String domain, String sid) {
		RemoteFile rf = new RemoteFile();
		rf.decodeDocument(doc);
		rf.domain = domain;
		return rf.getClientSideURL(sid);
	}

}
