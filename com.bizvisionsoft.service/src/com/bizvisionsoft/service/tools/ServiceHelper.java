package com.bizvisionsoft.service.tools;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

public class ServiceHelper {

	/**
	 * ��ȡ��֯�ľ���
	 * 
	 * @param _id
	 * @return ������֯�ľ���userId,���û�У�����null
	 */
	public String getOrganizationManager(ObjectId _id) {
		return Optional.ofNullable(ServicesLoader.get(OrganizationService.class).get(_id)).map(org -> org.getManagerId()).orElse(null);
	}

}
