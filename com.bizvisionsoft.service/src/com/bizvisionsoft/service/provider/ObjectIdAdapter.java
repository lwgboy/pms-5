package com.bizvisionsoft.service.provider;

import java.lang.reflect.Type;

import org.bson.types.ObjectId;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ObjectIdAdapter implements JsonDeserializer<ObjectId>, JsonSerializer<ObjectId> {

	@Override
	public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jo = new JsonObject();
		jo.addProperty("$oid", src.toString());
		return jo;
	}

	@Override
	public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		if (json.isJsonObject()) {
			JsonPrimitive oid = json.getAsJsonObject().getAsJsonPrimitive("$oid");
			if (oid.isString()) {
				try {
					ObjectId _id = new ObjectId(oid.getAsString());
					return _id;
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

}
