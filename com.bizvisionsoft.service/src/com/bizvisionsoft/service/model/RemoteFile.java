package com.bizvisionsoft.service.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;

public class RemoteFile {

	@Persistence
	public ObjectId _id;

	@Persistence
	public String name;

	@Persistence
	public String namepace;

	@Persistence
	public String contentType;

	public String getURL(String baseURL) {
		try {
			return baseURL + "/fs/" + namepace + "/" + _id + "/" + URLEncoder.encode(name, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

}
