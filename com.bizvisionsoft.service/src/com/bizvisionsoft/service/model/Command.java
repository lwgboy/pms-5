package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Command{

	public Date date;

	public String userId;

	public String userName;

	public String consignerId;

	public String name;

	public ObjectId _id;

	public String consignerName;

	public static Command newInstance(String name, User user, User consigner, Date date, ObjectId _id) {
		Command c = new Command();
		c.name = name;
		c.userId = user.getUserId();
		c.userName = user.getName();
		c.consignerId = consigner.getUserId();
		c.consignerName = consigner.getName();
		c.date = date;
		c._id = _id;
		return c;
	}

	public static Command newInstance(String name, String userId, String userName, String consignerId,
			String consignerName, Date date, ObjectId _id) {
		Command c = new Command();
		c.name = name;
		c.userId = userId;
		c.userName = userName;
		c.consignerId = consignerId;
		c.consignerName = consignerName;
		c.date = date;
		c._id = _id;
		return c;
	}

	public Document info() {
		return new Document("date", date).append("userId", userId).append("userName", userName)
				.append("consignerId", consignerId).append("consignerName", consignerName);
	}

}
