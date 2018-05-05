package com.bizvisionsoft.service.datatools;

import com.mongodb.BasicDBObject;

public class Query {
	
	private BasicDBObject result;

	public Query() {
		result = new BasicDBObject();
	}

	public Query skip(int skip) {
		result.append("skip", skip);
		return this;
	}
	
	public Query limit(int limit) {
		result.append("limit", limit);
		return this;
	}
	
	public Query filter(Object object) {
		result.append("filter", object);
		return this;
	}

	public BasicDBObject bson() {
		return result;
	}
}
