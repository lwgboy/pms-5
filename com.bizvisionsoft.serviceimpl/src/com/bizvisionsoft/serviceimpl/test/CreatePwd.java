package com.bizvisionsoft.serviceimpl.test;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.RSACoder;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class CreatePwd {
	public static void main(String[] args) {
		String url = "localhost";
		int port = 10001;
		MongoClient mongoClient = new MongoClient(url, port);
		DB psdoc = mongoClient.getDB("bvs_host");
		DBCursor dc = psdoc.getCollection("user").find();
		while(dc.hasNext()) {
			DBObject dbo = dc.next();
//			if(!dbo.get("userId").toString().equals("651854967@qq.com")) {
//				String pwd = encryPassword(((ObjectId)dbo.get("_id")).toHexString(),dbo.get("password").toString());
//				dbo.put("password",pwd);
//				psdoc.getCollection("user")
//				.update(new BasicDBObject("_id",dbo.get("_id")), dbo);
//			}
			if(null == dbo.get("domain")) {
				String pwd = encryPassword(((ObjectId)dbo.get("_id")).toHexString(),"1");
				dbo.put("password",pwd);
//				dbo.put("domain", "esl_1DEBN8GE9");
				dbo.put("domain", "leo_1DEQR41EQ");
				psdoc.getCollection("user")
				.update(new BasicDBObject("_id",dbo.get("_id")), dbo);
			}
		}
	}
	
	protected static String encryPassword(String salt, String psw) {
		try {
			byte[] data = RSACoder.encryptSHA((salt + psw).getBytes());
			String psw2 = Formatter.bytes2HexString(data);
			return psw2;
		} catch (Exception e) {
			return null;
		}
	}
}
