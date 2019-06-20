package com.bizvisionsoft.serviceimpl;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoDBService {

	/**
	 * 处理错误
	 * 
	 * @param e
	 * @param message
	 * @return
	 */
	final protected ServiceException handleMongoException(Exception e, String message) {
		if (e instanceof MongoException && ((MongoException) e).getCode() == 11000) {// 11000为唯一索引提示
			if (Check.isAssigned(message))
				return new ServiceException("违反唯一性规则：" + message);
			else
				return new ServiceException("违反唯一性规则");
		} // 其他代码的提示可增加到下方
		return new ServiceException(e.getMessage());
	}

	/**
	 * 处理错误
	 * 
	 * @param e
	 * @param message
	 * @return
	 */
	final protected ServiceException handleMongoException(Exception e) {
		return handleMongoException(e, null);
	}

	final protected <T> MongoCollection<T> c(Class<T> clazz, String domain) {
		return Domain.getCollection(domain, clazz);
	}

	final protected <T> MongoCollection<T> c(String col, Class<T> clazz, String domain) {
		return Domain.getDatabase(domain).getCollection(col, clazz);
	}

	final protected MongoCollection<Document> c(String name, String domain) {
		return Domain.getCollection(domain, name);
	}

	final protected MongoCollection<Document> c(String name) {
		return Domain.getCollection(null, name);
	}

	final protected <T> DeleteResult deleteOne(MongoCollection<?> col, Bson filter) {
		return col.deleteOne(filter);
	}

	final protected long deleteMany(MongoCollection<?> col, Bson filter) {
		return col.deleteMany(filter).getDeletedCount();
	}

	final protected Document findAndDeleteOne(MongoCollection<Document> col, Document filter) {
		return col.findOneAndDelete(filter);
	}

	final protected Document findAndUpdate(MongoCollection<Document> col, Document filter, Document update) {
		return col.findOneAndUpdate(filter, new Document("$set", update),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
	}

	final protected long update(MongoCollection<?> col, BasicDBObject filter, BasicDBObject update, UpdateOptions option) {
		try {
			UpdateResult updateMany = col.updateMany(filter, update, option);
			long cnt = updateMany.getModifiedCount();
			return cnt;
		} catch (Exception e) {
			throw handleMongoException(e);
		}
	}

	final protected <T> T insertOne(MongoCollection<T> col, T obj) {
		try {
			col.insertOne(obj);
			return obj;
		} catch (Exception e) {
			throw handleMongoException(e, obj.toString());
		}
	}

	final protected <T> MongoIterable<T> find(MongoCollection<T> col, BasicDBObject filter) {
		if (filter == null)
			return col.find();
		return col.find(filter);
	}

	final protected long count(MongoCollection<?> col, Bson filter) {
		if (filter == null)
			return col.countDocuments();
		return col.countDocuments(filter);
	}

}
