package com.bizvisionsoft.serviceimpl.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ServiceException extends WebApplicationException {
	
	public ServiceException() {
		this(Status.NOT_ACCEPTABLE, "·þÎñÒì³£");
	}

	public ServiceException(String msg) {
		this(Status.NOT_ACCEPTABLE, msg);
	}

	public ServiceException(Status status, String msg) {
		super(Response.status(status).entity(msg).type(MediaType.TEXT_PLAIN).build());
	}
}
