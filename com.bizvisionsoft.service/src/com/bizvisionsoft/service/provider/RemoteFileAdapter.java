package com.bizvisionsoft.service.provider;

import java.lang.reflect.Type;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.model.RemoteFile;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RemoteFileAdapter implements JsonDeserializer<RemoteFile>, JsonSerializer<RemoteFile> {

	@Override
	public JsonElement serialize(RemoteFile rf, Type type, JsonSerializationContext context) {
		JsonObject jo = new JsonObject();
		if (rf._id != null)
			jo.addProperty("$oid", rf._id.toString());
		jo.addProperty("contentType", rf.contentType);
		jo.addProperty("name", rf.name);
		jo.addProperty("namepace", rf.namepace);
		return jo;
	}

	@Override
	public RemoteFile deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		RemoteFile rf = new RemoteFile();
		JsonObject jo = json.getAsJsonObject();
		
		JsonPrimitive jp = jo.getAsJsonPrimitive("$oid");
		if (jp.isString())
			rf._id = new ObjectId(jp.getAsString());

		jp = jo.getAsJsonPrimitive("contentType");
		if (jp.isString())
			rf.contentType = jp.getAsString();

		jp = jo.getAsJsonPrimitive("name");
		if (jp.isString())
			rf.name = jp.getAsString();

		jp = jo.getAsJsonPrimitive("namepace");
		if (jp.isString())
			rf.namepace = jp.getAsString();

		return rf;
	}

}
