package com.bizvisionsoft.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.kie.api.internal.utils.BPM;
import org.kie.api.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.BPMService;

public class BPMServiceImpl extends BasicServiceImpl implements BPMService {

	private static Logger logger = LoggerFactory.getLogger(BPMServiceImpl.class);

	@Override
	public List<Document> listResources() {
		List<Document> result = BPM.getKieBase().getProcesses().stream().map(p -> {
			Document meta = new Document();
			meta.putAll(p.getMetaData());
			Document resource = new Document();
			Resource res = p.getResource();
			resource.append("sourcePath", res.getSourcePath())//
					.append("targetPath", res.getTargetPath())//
					.append("resourceType", res.getResourceType().getName());
			return new Document("id", p.getId())//
					.append("name", p.getName())//
					.append("namespace", p.getNamespace())//
					.append("version", p.getVersion())//
					.append("knowledgeType", p.getKnowledgeType().name())//
					.append("type", p.getType())//
					.append("packageName", p.getPackageName())//
					.append("meta", meta).append("resource", resource);//
		}).collect(Collectors.toList());
		return result;
	}

}
