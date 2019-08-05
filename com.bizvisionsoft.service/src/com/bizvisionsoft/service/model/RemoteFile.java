package com.bizvisionsoft.service.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;
import com.bizvisionsoft.service.ServicesLoader;

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

	public String getServerSideURL() {
		try {
			return ServicesLoader.url + "/fs/" + domain + "/" + namepace + "/" + _id + "/" + URLEncoder.encode(name, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public InputStream getInputStreamFromClient(String request) throws MalformedURLException, IOException {
		String url = getClientSideURL("rwt");
		url = request + url.substring(1);
		URLConnection connection = new URL(url).openConnection();
		connection.setDoOutput(true);
		return connection.getInputStream();
	}
	
	public InputStream getInputStreamFromServer() throws MalformedURLException, IOException {
		String url = getServerSideURL();
		URLConnection connection = new URL(url).openConnection();
		connection.setDoOutput(true);
		return connection.getInputStream();
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
