package com.bizvisionsoft.serviceimpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.FileService;
import com.bizvisionsoft.service.model.RemoteFile;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.Service;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;

public class FileServiceImpl extends BasicServiceImpl implements FileService {

	public Response get(String namespace, String id, String fileName,String domain) {
		ObjectId _id = new ObjectId(id);
		GridFSBucket bucket = GridFSBuckets.create(Domain.getDatabase(domain), namespace);
		GridFSFile file = bucket.find(Filters.eq("_id", _id)).first();
		if (file == null) {
			return Response.status(404).build();
		}

		String contentType = "" + file.getMetadata().get("contentType");
		if (Check.isNotAssigned("" + contentType)) {
			try {
				contentType = Files.probeContentType(Paths.get(fileName));
			} catch (IOException e) {
			}
		}
		if (Check.isNotAssigned("" + contentType))
			contentType = "application/octet-stream";

		String downloadableFileName;
		try {
			downloadableFileName = new String(fileName.getBytes(), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			downloadableFileName = fileName;
		}
		return Response.ok().entity(bucket.openDownloadStream(_id))
				.header("Content-Disposition", "attachment; filename=" + downloadableFileName).header("Content-Type", contentType).build();
	}

	@Override
	public RemoteFile upload(InputStream fileInputStream, String fileName, String namespace, String contentType, String uploadBy, String domain) {
		GridFSUploadOptions option = new GridFSUploadOptions();
		option.metadata(new Document().append("contentType", contentType).append("uploadBy", uploadBy));
		ObjectId id = GridFSBuckets.create(Domain.getDatabase(domain), namespace).uploadFromStream(fileName, fileInputStream, option);
		id.toString();
		RemoteFile rf = new RemoteFile();
		rf._id = id;
		rf.name = fileName;
		rf.namepace = namespace;
		rf.contentType = contentType;
		return rf;
	}

	@Override
	public void delete(String namespace, String id,String domain) {
		deleteFile(new Document("namepace", namespace).append("_id", new ObjectId(id)), domain);
	}

}
