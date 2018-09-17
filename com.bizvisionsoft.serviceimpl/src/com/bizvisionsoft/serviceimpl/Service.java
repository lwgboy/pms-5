package com.bizvisionsoft.serviceimpl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.osgi.internal.debug.Debug;
import org.eclipse.osgi.internal.framework.BundleContextImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.mongocodex.codec.CodexProvider;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Service implements BundleActivator {

	public Logger logger = LoggerFactory.getLogger(getClass());

	private static BundleContext context;

	private static MongoDatabase database;

	private static MongoClient mongo;

	private boolean loadJSQueryAtInit;

	private static Debug debug;

	public static String mongoDbBinPath;

	public static File dumpFolder;

	public static File queryFolder;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Service.context = bundleContext;

		String filePath = context.getProperty("com.bizvisionsoft.service.MongoDBConnector");
		loadDatabase(filePath);
		loadQuery(filePath);
		loadBackupFolder(filePath);
	}

	private void loadBackupFolder(String filePath) {
		try {
			mongoDbBinPath = context.getProperty("com.bizvisionsoft.service.MongoDBPath");
			String dumpPath = context.getProperty("com.bizvisionsoft.service.backupPath");
			if (dumpPath == null || dumpPath.isEmpty()) {
				dumpFolder = new File(new File(filePath).getParent() + "/dump");
			} else {
				dumpFolder = new File(dumpPath);
			}
			if (!dumpFolder.isDirectory()) {
				dumpFolder.mkdirs();
			}
			logger.info("数据库备份目录：" + dumpFolder);
		} catch (Exception e) {
			logger.warn("数据库备份目录错误", e);
		}
	}

	private void loadQuery(String filePath) {
		try {
			queryFolder = new File(new File(filePath).getParent() + "/query");
			loadJSQueryAtInit = "init".equalsIgnoreCase(context.getProperty("com.bizvisionsoft.service.LoadJSQuery"));
			if (loadJSQueryAtInit) {
				JQ.reloadJS();
			}

			JQ.forceReloadJSQuery = "force"
					.equalsIgnoreCase(context.getProperty("com.bizvisionsoft.service.LoadJSQuery"));
			logger.info("JSQuery加载完成，目录：" + queryFolder);
		} catch (Exception e) {
			logger.error("JSQuery加载错误：", e);
		}
	}

	private void loadDatabase(String filePath) {
		try {
			FileInputStream fis = new FileInputStream(filePath); // $NON-NLS-1$
			InputStream is = new BufferedInputStream(fis);
			Properties props = new Properties();
			props.load(is);
			mongo = createMongoClient(props);
			String dbname = props.getProperty("db.name"); //$NON-NLS-1$
			database = mongo.getDatabase(dbname).withCodecRegistry(getCodecRegistry());
			logger.info("连接数据库完成：" + dbname);
			fis.close();
			is.close();
		} catch (Exception e) {
			logger.error("连接数据库出现错误。", e);
		}
	}

	private static MongoClient createMongoClient(Properties props) throws UnknownHostException {
		String host = props.getProperty("db.host"); //$NON-NLS-1$
		String _port = props.getProperty("db.port");
		int port = _port == null ? 10001 : Integer.parseInt(_port); // $NON-NLS-1$
		ArrayList<ServerAddress> serverList = null;
		String replicaSet = props.getProperty("db.replicaSet"); //$NON-NLS-1$
		if (replicaSet != null && replicaSet.length() > 0) {
			serverList = new ArrayList<ServerAddress>();
			String[] arr = replicaSet.split(" ");
			for (int i = 0; i < arr.length; i++) {
				String[] ari = arr[i].split(":");
				ServerAddress address = new ServerAddress(ari[0], Integer.parseInt(ari[1]));
				serverList.add(address);
			}
		}

		Builder builder = MongoClientOptions.builder();
		// builder.autoConnectRetry("true".equalsIgnoreCase(props //$NON-NLS-1$
		// .getProperty("db.options.autoConnectRetry"))); //$NON-NLS-1$
		// CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
		// CodecRegistries.fromCodecs(new
		// UuidCodec(UuidRepresentation.STANDARD)),
		// MongoClient.getDefaultCodecRegistry());
		// builder.codecRegistry(codecRegistry);
		builder.connectionsPerHost(Integer.parseInt(props.getProperty("db.options.connectionsPerHost"))); //$NON-NLS-1$
		builder.maxWaitTime(Integer.parseInt(props.getProperty("db.options.maxWaitTime"))); //$NON-NLS-1$
		builder.socketTimeout(Integer.parseInt(props.getProperty("db.options.socketTimeout"))); //$NON-NLS-1$
		builder.connectTimeout(Integer.parseInt(props.getProperty("db.options.connectTimeout"))); //$NON-NLS-1$
		builder.threadsAllowedToBlockForConnectionMultiplier(
				Integer.parseInt(props.getProperty("db.options.threadsAllowedToBlockForConnectionMultiplier"))); //$NON-NLS-1$
		ServerAddress address = new ServerAddress(host, port);
		if (serverList != null) {
			return new MongoClient(serverList, builder.build());
		} else {
			return new MongoClient(address, builder.build());
		}

	}

	private CodecRegistry getCodecRegistry() {
		CodecRegistry modelCodeRegistry = CodecRegistries.fromProviders(new CodexProvider());
		CodecRegistry defaultCodecRegistry = MongoClient.getDefaultCodecRegistry();
		return CodecRegistries.fromRegistries(modelCodeRegistry, defaultCodecRegistry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		mongo.close();
		mongo = null;
		database = null;
	}

	public static MongoDatabase db() {
		return database;
	}

	public static MongoClient getMongo() {
		return mongo;
	}

	public static <T> MongoCollection<T> col(Class<T> clazz) {
		return database.getCollection(clazz.getAnnotation(PersistenceCollection.class).value(), clazz);
	}

	public static MongoCollection<Document> col(String name) {
		return database.getCollection(name);
	}

	public static Debug getDebug() {
		if (debug == null) {
			debug = ((BundleContextImpl) context).getContainer().getConfiguration().getDebug();
		}
		return debug;
	}

}
