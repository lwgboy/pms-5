package com.bizvisionsoft.serviceimpl.valuegen;

import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.mongocodex.tools.IValueGenerateService;
import com.bizvisionsoft.mongocodex.tools.IValueGenerateServiceFactory;
import com.bizvisionsoft.service.model.ValueRule;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;

public class DocumentValueGeneratorFactory extends BasicServiceImpl implements IValueGenerateServiceFactory {

	@Override
	public IValueGenerateService getService(String className, String fieldName, String domain) {
		return Optional
				.ofNullable(c(ValueRule.class, domain)
						.find(new Document("className", className).append("fieldName", fieldName).append("enable", true)).first())
				.map(d->new DocumentValueGenerator(d)).orElse(null);
	}

}
