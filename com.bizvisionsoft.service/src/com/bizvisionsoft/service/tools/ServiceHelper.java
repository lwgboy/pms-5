package com.bizvisionsoft.service.tools;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

public class ServiceHelper {

	private static Logger logger = LoggerFactory.getLogger(ServiceHelper.class);

	/**
	 * 获取组织的经理
	 * 
	 * @param _id
	 * @return 返回组织的经理userId,如果没有，返回null
	 */
	public String getOrganizationManager(ObjectId _id) {
		if (_id == null) {
			logger.error("getOrganizationManager, 参数 _id为null");
			return null;
		}
		return Optional.ofNullable(ServicesLoader.get(OrganizationService.class).get(_id)).map(org -> org.getManagerId()).orElse(null);
	}

	public String getMultiActorId(Object input) {
		if (input instanceof String) {
			return "" + input;
		}

		if (input instanceof List<?>) {
			return ((List<?>) input).stream().reduce("", (s1, s2) -> {
				if (s1.isEmpty())
					return "" + s2;
				if (s2 == null || (s2 + "").isEmpty())
					return s1;
				return s1 + "," + s2;
			}, (s1, s2) -> s1);
		}

		if (input instanceof Object[]) {
			return Stream.of((Object[]) input).reduce("", (s1, s2) -> {
				if (s1.isEmpty())
					return "" + s2;
				if (s2 == null || (s2 + "").isEmpty())
					return s1;
				return s1 + "," + s2;
			}, (s1, s2) -> s1);
		}

		return null;
	}

}
