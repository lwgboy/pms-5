package com.bizvisionsoft.service.tools;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

public class ServiceHelper {
	
	private static Logger logger = LoggerFactory.getLogger(ServiceHelper.class);


	/**
	 * ��ȡ��֯�ľ���
	 * 
	 * @param _id
	 * @return ������֯�ľ���userId,���û�У�����null
	 */
	public String getOrganizationManager(ObjectId _id) {
		if (_id == null) {
			logger.error("getOrganizationManager, ���� _idΪnull");
			return null;
		}
		return Optional.ofNullable(ServicesLoader.get(OrganizationService.class).get(_id)).map(org -> org.getManagerId()).orElse(null);
	}

}
