package com.bizvisionsoft.service;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.bizvisionsoft.service.model.RemoteFile;

@Path("/fs")
public interface FileService {

	@GET
	@Path("/{namespace}/{id}/{name}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response get(@PathParam("id") String id, @PathParam("name") String fileName,
			@PathParam("namespace") String namespace);

	@POST
	@Path("/upload")
	@Produces("application/json;charset=UTF-8")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public RemoteFile upload(@FormDataParam("file") InputStream fileInputStream, @FormDataParam("name") String fileName,
			@FormDataParam("namespace") String namespace, @FormDataParam("contentType") String contentType,
			@FormDataParam("uploadBy") String uploadBy);

	@DELETE
	@Path("/{namespace}/{id}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public void delete(@PathParam("id") String id, @PathParam("namespace") String namespace);

}
