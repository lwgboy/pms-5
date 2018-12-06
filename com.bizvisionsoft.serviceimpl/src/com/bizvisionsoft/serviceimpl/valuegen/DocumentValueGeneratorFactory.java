package com.bizvisionsoft.serviceimpl.valuegen;

import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.mongocodex.tools.IValueGenerateService;
import com.bizvisionsoft.mongocodex.tools.IValueGenerateServiceFactory;
import com.bizvisionsoft.service.ValueRule;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;

public class DocumentValueGeneratorFactory extends BasicServiceImpl implements IValueGenerateServiceFactory {

	@Override
	public IValueGenerateService getService(String className, String fieldName) {
		return Optional
				.ofNullable(c(ValueRule.class)
						.find(new Document("className", className).append("fieldName", fieldName).append("enable", true)).first())
				.map(DocumentValueGenerator::new).orElse(null);
	}

}
