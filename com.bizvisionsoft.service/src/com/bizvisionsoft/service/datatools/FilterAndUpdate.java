package com.bizvisionsoft.service.datatools;

import com.mongodb.BasicDBObject;

public class FilterAndUpdate {

	private BasicDBObject result;

	public FilterAndUpdate() {
		result = new BasicDBObject();
	}

	public FilterAndUpdate filter(Object filter) {
		result.append("filter", filter);
		return this;
	}

	public FilterAndUpdate update(Object update) {
		result.append("update", update);
		return this;
	}

	public FilterAndUpdate set(Object object) {
		result.append("update", new BasicDBObject("$set", object));
		return this;
	}

	public BasicDBObject bson() {
		return result;
	}

}
