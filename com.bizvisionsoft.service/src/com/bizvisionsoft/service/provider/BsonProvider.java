package com.bizvisionsoft.service.provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;

@Provider
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

	public BsonProvider() {
	}

	public Gson getGson() {
		return new GsonBuilder()//
				.serializeNulls()
				.registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())//
				.registerTypeAdapter(Date.class, new DateAdapter())//
				.registerTypeAdapter(BasicDBObject.class, new BasicDBObjectAdapter())//
				.create();
	}

	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public void writeTo(T object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, java.lang.Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		String json;
		if (type.equals(genericType)) {
			json = getGson().toJson(object, type);
		} else {
			json = getGson().toJson(object, genericType);
		}

		try (OutputStream stream = entityStream) {
			entityStream.write(json.getBytes("utf-8"));
			entityStream.flush();
		}
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		try (InputStreamReader reader = new InputStreamReader(entityStream, "UTF-8")) {
			if (type.equals(genericType)) {
				return getGson().fromJson(reader, type);
			} else {
				return getGson().fromJson(reader, genericType);
			}
		}
	}



}