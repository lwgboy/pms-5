package com.bizvisionsoft.service.provider;

import java.lang.reflect.Type;

import org.bson.Document;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DocumentAdapter implements JsonDeserializer<Document>, JsonSerializer<Document> {

	@Override
	public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonParser().parse(src.toJson()).getAsJsonObject();
	}

	@Override
	public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		if (json.isJsonObject()) {
			return Document.parse(json.toString());
		}
		return null;
	}

}
