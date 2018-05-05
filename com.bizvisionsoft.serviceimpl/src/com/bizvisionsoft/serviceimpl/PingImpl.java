package com.bizvisionsoft.serviceimpl;

import com.bizvisionsoft.service.Ping;
import com.bizvisionsoft.service.model.ServerInfo;

public class PingImpl implements Ping {

	@Override
	public ServerInfo getServerInfo(String req) {
		return new ServerInfo(req).setHostMessage("Hello "+req);
	}

}
