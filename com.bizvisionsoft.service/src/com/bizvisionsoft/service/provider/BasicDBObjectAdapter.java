package com.bizvisionsoft.service.provider;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mongodb.BasicDBObject;

public class BasicDBObjectAdapter implements JsonDeserializer<BasicDBObject>, JsonSerializer<BasicDBObject> {

	@Override
	public JsonElement serialize(BasicDBObject src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonParser().parse(src.toJson()).getAsJsonObject();
	}

	@Override
	public BasicDBObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		if (json.isJsonObject()) {
			return BasicDBObject.parse(json.toString());
		}
		return null;
	}

}
