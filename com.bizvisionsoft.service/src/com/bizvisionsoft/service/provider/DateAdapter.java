package com.bizvisionsoft.service.provider;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateAdapter implements JsonDeserializer<Date>,
		JsonSerializer<Date> {

	@Override
	public JsonElement serialize(Date src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject jo = new JsonObject();
		jo.addProperty("$date", src.getTime());
		return jo;
	}

	@Override
	public Date deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonObject()) {
			JsonPrimitive oid = json.getAsJsonObject().getAsJsonPrimitive(
					"$date");
			try {
				long millis = oid.getAsBigInteger().longValue();
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(millis);
				return c.getTime();
			} catch (Exception e) {
			}
		}
		return null;
	}
	
}
