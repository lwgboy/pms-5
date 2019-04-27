package com.bizvisionsoft.service.tools;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

public class ServiceHelper {

	/**
	 * 获取组织的经理
	 * 
	 * @param _id
	 * @return 返回组织的经理userId,如果没有，返回null
	 */
	public String getOrganizationManager(ObjectId _id) {
		return Optional.ofNullable(ServicesLoader.get(OrganizationService.class).get(_id)).map(org -> org.getManagerId()).orElse(null);
	}

}
