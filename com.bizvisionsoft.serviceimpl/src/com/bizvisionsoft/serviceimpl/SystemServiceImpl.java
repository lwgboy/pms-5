package com.bizvisionsoft.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.model.Backup;
import com.bizvisionsoft.service.model.ServerInfo;
import com.bizvisionsoft.service.tools.FileTools;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.mongotools.MongoDBBackup;
import com.mongodb.ServerAddress;

public class SystemServiceImpl extends BasicServiceImpl implements SystemService {

	@Override
	public ServerInfo getServerInfo(String req) {
		return new ServerInfo(req).setHostMessage("Hello " + req);

	}

	@Override
	public String mongodbDump(String note) {
		ServerAddress addr = Service.getDatabaseHost();
		String host = addr.getHost();
		int port = addr.getPort();
		String dbName = Service.db().getName();
		String dumpPath = Service.dumpFolder.getPath();
		String path = Service.mongoDbBinPath;
		String result = new MongoDBBackup.Builder().runtime(Runtime.getRuntime()).path(path).host(host).port(port)
				.dbName(dbName).archive(dumpPath + "\\").build().dump();
		try {
			FileTools.writeFile(note, result + "/notes.txt", "utf-8");
		} catch (IOException e) {
			throw new ServiceException(e.getMessage());
		}

		return result;
	}

	@Override
	public List<Backup> getBackups() {
		List<Backup> result = new ArrayList<>();
		File folder = Service.dumpFolder;
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				try {
					String name = files[i].getName();
					Backup backup = new Backup().setDate(new Date(Long.parseLong(name)));
					backup.setId(name);
					File[] note = files[i].listFiles(f -> f.getName().equals("notes.txt"));
					if (note != null && note.length > 0) {
						String text = FileTools.readFile(note[0].getPath(), "utf-8");
						backup.setNotes(text);
					}
					result.add(backup);
				} catch (Exception e) {
				}
			}
		}
		return result;
	}

	@Override
	public void updateBackupNote(String id, String text) {
		File[] files = Service.dumpFolder.listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			try {
				FileTools.writeFile(text, files[0].getPath() + "/notes.txt", "utf-8");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean deleteBackup(String id) {
		File[] files = Service.dumpFolder.listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			return FileTools.deleteFolder(files[0].getPath());
		}
		return false;
	}

	@Override
	public boolean restoreFromBackup(String id) {
		ServerAddress addr = Service.getDatabaseHost();
		String host = addr.getHost();
		int port = addr.getPort();
		String dbName = Service.db().getName();
		String path = Service.mongoDbBinPath;
		String archive;
		File[] files = Service.dumpFolder.listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			archive = files[0].getPath() + "\\" + dbName;
		} else {
			return false;
		}

		new MongoDBBackup.Builder().runtime(Runtime.getRuntime()).path(path).host(host).port(port).dbName(dbName)
				.archive(archive).build().restore();
		return true;
	}

	@Override
	public String getClientSetting(String userId, String clientId, String name) {
		Document doc = c("clientSetting")
				.find(new Document("userId", userId).append("clientId", clientId).append("name", name)).first();
		if (doc != null) {
			return doc.getString("value");
		} else {
			return "";
		}
	}

	@Override
	public void updateClientSetting(Document setting) {
		Document query = new Document();
		query.putAll(setting);
		query.remove("value");

		long cnt = c("clientSetting").countDocuments(query);
		if (cnt == 0) {
			c("clientSetting").insertOne(setting);
		} else {
			c("clientSetting").updateOne(query, new Document("$set", setting));
		}
	}

	@Override
	public void deleteClientSetting(String clientId, String name) {
		c("clientSetting").deleteMany(new Document("clientId", clientId).append("name", name));
	}

	@Override
	public void deleteClientSetting(String clientId) {
		c("clientSetting").deleteMany(new Document("clientId", clientId));
	}

	@Override
	public void deleteClientSetting(String userId, String clientId, String name) {
		c("clientSetting").deleteMany(new Document("userId", userId).append("clientId", clientId).append("name", name));
	}

}
