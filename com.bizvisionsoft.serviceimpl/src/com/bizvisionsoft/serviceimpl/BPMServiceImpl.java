package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.kie.api.internal.utils.BPM;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.common.query.JQ;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class BPMServiceImpl extends BasicServiceImpl implements BPMService {

	private static Logger logger = LoggerFactory.getLogger(BPMServiceImpl.class);

	@Override
	public List<Document> listResources() {
		List<Document> result = BPM.getKieBase().getProcesses().stream().map(p -> {
			// Document meta = new Document();
			// meta.putAll( );
			// Map<String, Object> meta = p.getMetaData();
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
					// .append("meta", meta)//
					.append("resource", resource);//
		}).collect(Collectors.toList());
		return result;
	}

	@Override
	public List<ProcessDefinition> listFunctionRoles(BasicDBObject condition, String userId) {
		List<Bson> pipeline = new ArrayList<>();
		new JQ("查询授权用户的流程定义").set("userId", userId).appendTo(pipeline);
		appendConditionToPipeline(pipeline, condition);
		List<?> ids = c("processDefinition").aggregate(pipeline).map((Document d) -> d.getObjectId("_id")).into(new ArrayList<>());
		ArrayList<ProcessDefinition> result = c(ProcessDefinition.class).find(new Document("_id", new Document("$in", ids)))
				.into(new ArrayList<>());
		return result;
	}

	@Override
	public long countFunctionRoles(BasicDBObject filter, String userId) {
		List<Bson> pipeline = new ArrayList<>();
		new JQ("查询授权用户的流程定义").appendTo(pipeline);
		Optional.ofNullable(filter).map(Aggregates::match).ifPresent(pipeline::add);
		return c("processDefinition").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public Long startProcess(Document parameters, String processId) {
		// 获取流程传入参数
		Document input = (Document) parameters.get("input");
		// 获取流程附加数据
		Document meta = (Document) parameters.get("meta");
		Document creationInfo = (Document) parameters.get("creationInfo");

//		CorrelationKeyInfo ck = new CorrelationKeyInfo();
//		ck.setName("meta");
//		ck.addProperty(new CorrelationPropertyInfo("name",meta.getString("name")));
//		ck.addProperty(new CorrelationPropertyInfo("type",meta.getString("type")));
//		ck.addProperty(new CorrelationPropertyInfo("userId",creationInfo.getString("userId")));
//		ck.addProperty(new CorrelationPropertyInfo("consignerId",creationInfo.getString("consignerId")));
//		// 启动流程
//		CorrelationAwareProcessRuntime kies = (CorrelationAwareProcessRuntime) BPM.getDefaultRuntimeEngine().getKieSession();
//		ProcessInstance pi = kies.startProcess(processId,ck,  input);
		
		KieSession kies = BPM.getDefaultRuntimeEngine().getKieSession();
		ProcessInstance pi = kies.startProcess(processId,  input);

		long id = pi.getId();
		// 更新流程附加信息
		c("bpm_ProcessInstanceInfo").updateOne(new Document("_id", id),
				new Document("$set", new Document("meta", meta).append("creationInfo", creationInfo)));
		logger.info("启动流程：{}", pi);
		return id;
	}

	@Override
	public List<Document> listTaskCard(BasicDBObject condition, String userId, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countTaskCard(BasicDBObject filter, String userId) {
		// TODO Auto-generated method stub
		return 0;
	}

}
