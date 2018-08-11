package com.bizvisionsoft.serviceimpl;

import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.model.ServerInfo;

public class SystemServiceImpl extends BasicServiceImpl implements SystemService {

	@Override
	public ServerInfo getServerInfo(String req) {
		return new ServerInfo(req).setHostMessage("Hello "+req);
	}

}
