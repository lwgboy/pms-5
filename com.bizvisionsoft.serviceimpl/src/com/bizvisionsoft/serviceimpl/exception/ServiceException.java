package com.bizvisionsoft.serviceimpl.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServiceException extends WebApplicationException {

	public ServiceException() {
		super(Response.status(401).entity("·þÎñÒì³£").type(MediaType.TEXT_PLAIN).build());
	}

	public ServiceException(String msg) {
		super(Response.status(401).entity(msg).type(MediaType.TEXT_PLAIN).build());
	}
}
