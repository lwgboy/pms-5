package com.bizvisionsoft.serviceimpl.mongotools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MongoDump {

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

		public MongoDump build() {
			MongoDump result = new MongoDump();
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

	public String execute() {
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
				System.out.println(line);
			process.waitFor();
			return outPath;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// private String toString(InputStream inputStream) throws IOException {
	// final int bufferSize = 1024;
	// final char[] buffer = new char[bufferSize];
	// final StringBuilder out = new StringBuilder();
	// Reader in = new InputStreamReader(inputStream, "UTF-8");
	// for (;;) {
	// int rsz = in.read(buffer, 0, buffer.length);
	// if (rsz < 0)
	// break;
	// out.append(buffer, 0, rsz);
	// }
	// return out.toString();
	// }
}
