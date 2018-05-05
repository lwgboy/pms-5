package com.bizvisionsoft.serviceimpl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.FileService;
import com.bizvisionsoft.service.model.RemoteFile;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;

public class FileServiceImpl implements FileService {

	public Response get(String id, String fileName, String namespace) {
		ObjectId _id = new ObjectId(id);
		GridFSBucket bucket = GridFSBuckets.create(Service.db(), namespace);
		GridFSFile file = bucket.find(Filters.eq("_id", _id)).first();
		if (file == null) {
			Response.status(404);
		}

		Object contentType = file.getMetadata().get("contentType");
		if (contentType == null)
			contentType = "application/octet-stream";

		String downloadableFileName;
		try {
			downloadableFileName = new String(fileName.getBytes(), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			downloadableFileName = fileName;
		}
		return Response.ok().entity(bucket.openDownloadStream(_id))
				.header("Content-Disposition", "attachment; filename=" + downloadableFileName)
				.header("Content-Type", contentType).build();
	}

	@Override
	public RemoteFile upload(InputStream fileInputStream, String fileName, String namespace, String contentType,
			String uploadBy) {
		GridFSUploadOptions option = new GridFSUploadOptions();
		option.metadata(new Document().append("contentType", contentType).append("uploadBy", uploadBy));
		ObjectId id = GridFSBuckets.create(Service.db(), namespace).uploadFromStream(fileName, fileInputStream, option);
		id.toString();
		RemoteFile rf = new RemoteFile();
		rf._id = id;
		rf.name = fileName;
		rf.namepace = namespace;
		rf.contentType = contentType;
		return rf;
	}

	@Override
	public void delete(String id, String namespace) {
		ObjectId _id = new ObjectId(id);
		GridFSBucket bucket = GridFSBuckets.create(Service.db(), namespace);
		bucket.delete(_id);
	}

}
