package com.bizvisionsoft.serviceimpl;

import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.model.ServerInfo;
import com.bizvisionsoft.serviceimpl.mongotools.MongoDump;
import com.mongodb.MongoClient;

public class SystemServiceImpl extends BasicServiceImpl implements SystemService {

	@Override
	public ServerInfo getServerInfo(String req) {
		return new ServerInfo(req).setHostMessage("Hello " + req);

	}

	@Override
	public String mongodbDump() {
		final MongoClient client = Service.getMongo();
		String host = client.getAddress().getHost();
		int port = client.getAddress().getPort();
		String dbName = Service.db().getName();
		String dumpPath = Service.dumpFolder.getPath();
		String path = Service.mongoDbBinPath;
		String result = new MongoDump.Builder().runtime(Runtime.getRuntime()).path(path).host(host).port(port)
				.dbName(dbName).archive(dumpPath+"\\").build().execute();
		return result;
	}

}
