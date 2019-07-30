package com.bizvisionsoft.service.provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.service.ServicesLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;

@Provider
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

	public Logger logger = LoggerFactory.getLogger(getClass());

	public BsonProvider() {
	}

	public Gson getGson() {
		GsonBuilder gb = new GsonBuilder()//
				.serializeNulls().registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())//
				.registerTypeAdapter(Date.class, new DateAdapter())//
				.registerTypeAdapter(BasicDBObject.class, new BasicDBObjectAdapter())//
				.registerTypeAdapter(Document.class, new DocumentAdapter());//
		// .registerTypeAdapter(RemoteFile.class, new RemoteFileAdapter())//²»ÐèÒª
		if (logger.isDebugEnabled()) {
			gb.setPrettyPrinting();
		}
		return gb.create();
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
			MultivaluedMap<String, java.lang.Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
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
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		try (InputStreamReader reader = new InputStreamReader(entityStream, "UTF-8")) {
			if (type.equals(UniversalResult.class)) {
				return readFromResult(reader);
			} else {
				if (type.equals(genericType)) {
					return getGson().fromJson(reader, type);
				} else {
					return getGson().fromJson(reader, genericType);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private T readFromResult(InputStreamReader reader) {
		UniversalResult ur = getGson().fromJson(reader, UniversalResult.class);
		String className = ur.getTargetClassName();
		String bundleName = ur.getTargetBundleName();
		Class<?> clazz = ServicesLoader.getClass(className, bundleName);
		if (clazz == null || clazz.getName().equals("org.bson.Document")) {
			if (ur.isList()) {
				List<Document> value = new ArrayList<>();
				BsonArray arr = BsonArray.parse(ur.getResult());
				arr.forEach(v -> {
					if (v.isDocument()) {
						value.add(Document.parse(v.asDocument().toJson()));
					}
				});
				ur.setValue(value);
			} else {
				ur.setValue(Document.parse(ur.getResult()));
			}
		} else {
			try {
				if (ur.isList()) {
					ParameterizedType rpt = new ParameterizedType() {
						@Override
						public Type[] getActualTypeArguments() {
							return new Type[] { clazz };
						}

						@Override
						public Type getRawType() {
							return ArrayList.class;
						}

						@Override
						public Type getOwnerType() {
							return null;
						}
					};
					ur.setValue(getGson().fromJson(ur.getResult(), rpt));
				} else {
					ur.setValue(getGson().fromJson(ur.getResult(), clazz));
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return (T) ur;
	}

}