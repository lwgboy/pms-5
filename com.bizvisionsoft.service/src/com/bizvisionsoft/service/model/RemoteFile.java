package com.bizvisionsoft.service.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
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
		return "/bvs/fs?id=" + _id.toHexString() + "&namespace=" + namepace + "&name=" + name + "&sid=" + sid + "&domain=" + domain;
	}

}
