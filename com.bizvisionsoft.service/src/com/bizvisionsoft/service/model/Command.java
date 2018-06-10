package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

public class Command {

	public Date date;

	public String userId;

	public String name;
	
	public ObjectId _id;

	public static Command newInstance(String name, String userId, Date date,ObjectId _id) {
		Command c = new Command();
		c.name = name;
		c.userId = userId;
		c.date = date;
		c._id = _id;
		return c;
	}

}
