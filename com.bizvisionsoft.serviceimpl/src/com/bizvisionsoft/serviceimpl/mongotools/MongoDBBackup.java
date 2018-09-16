package com.bizvisionsoft.serviceimpl.mongotools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBBackup {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String host;
	private String dbName;
	private int port;
	private String archive;
	private Runtime runtime;
	private String path;

	public static class Builder {

		private String host;
		private int port;
		private String dbName;
		private String archive;
		private Runtime runtime;
		private String path;

		public Builder archive(String archive) {
			this.archive = archive;
			return this;
		}

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder dbName(String dbName) {
			this.dbName = dbName;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder runtime(Runtime runtime) {
			this.runtime = runtime;
			return this;
		}

		public MongoDBBackup build() {
			MongoDBBackup result = new MongoDBBackup();
			result.host = host;
			result.port = port;
			result.dbName = dbName;
			result.path = path;
			result.runtime = runtime;
			result.archive = archive;
			Runtime rt = runtime;
			if (rt == null) {
				rt = Runtime.getRuntime();
			}
			result.runtime = rt;
			return result;
		}
	}

	// public String executeLinux() {
	// try {
	// String command = String.format("%s --archive=%s --uri=%s", commandPath,
	// archive, uri);
	// Process runtimeProcess = runtime.exec(new String[] { "/bin/sh", "-c", command
	// });
	// int exitValue = runtimeProcess.waitFor();
	// if (exitValue != 0) {
	// InputStream error = runtimeProcess.getErrorStream();
	// String errorMessage = toString(error);
	// throw new Exception(errorMessage);
	// }
	// InputStream message = runtimeProcess.getInputStream();
	// return toString(message);
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }

	public String dump() {
		try {
			String outPath = archive + System.currentTimeMillis();
			String command = path + "/mongodump.exe --host " + host + " --port " + port + " --db " + dbName + " --out "
					+ outPath + " --gzip";
			Process process = runtime.exec(command);
			InputStream stderr = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				logger.debug(line);
			process.waitFor();
			logger.info("���ݿⱸ�����");
			return outPath;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void restore() {
		try {

			String command = path + "/mongorestore.exe --drop --gzip --host " + host + " --port " + port + " --db "
					+ dbName + " --dir " + archive;
			Process process = runtime.exec(command);
			InputStream stderr = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				logger.debug(line);
			process.waitFor();
			logger.info("���ݿ�ָ����");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
