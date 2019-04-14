package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("processDefinition")
public class ProcessDefinition {
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String bpmnId;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private Boolean enabled;

	private Document constants;

	/**
	 * @param input
	 *            输入的是name,value
	 */
	@WriteValue("constants")
	private void writeConstantsByEditor(List<Document> input) {
		constants = new Document();
		if (input != null) {
			input.forEach(d -> constants.append(d.getString("name"), d.get("value")));
		}
	}

	@ReadValue("constants")
	private List<Document> readConstantsByEditor() {
		List<Document> result = new ArrayList<>();
		if (constants != null) {
			constants.entrySet().forEach(e -> {
				result.add(new Document("name", e.getKey()).append("value", e.getValue()));
			});
		}
		return result;
	}

	private List<String> parameters;

	@WriteValue("parameters")
	private void writeParameters(List<Document> input) {
		parameters = new ArrayList<>();
		if (input != null) {
			input.forEach(d -> parameters.add(d.getString("name")));
		}
	}

	@ReadValue("parameters")
	private List<Document> readParameters() {
		List<Document> result = new ArrayList<>();
		if (parameters != null) {
			parameters.forEach(e -> {
				result.add(new Document("name", e));
			});
		}
		return result;
	}

	@WriteValue
	private List<String> users;

	@ReadValue("users")
	private List<User> readUsersByEditor() {
		if (users != null) {
			Query q = new Query().filter(new BasicDBObject("userId", new BasicDBObject("$in", users)));
			return ServicesLoader.get(UserService.class).createDataSet(q.bson());
		} else {
			return new ArrayList<>();
		}
	}

	@WriteValue
	private List<ObjectId> organizations;

	@ReadValue("organizations")
	private List<Organization> readOrganizationsByEditor() {
		if (organizations != null) {
			Query q = new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", organizations)));
			return ServicesLoader.get(OrganizationService.class).createDataSet(q.bson());
		} else {
			return new ArrayList<>();
		}
	}

	@WriteValue
	private List<String> roles;

	@ReadValue("roles")
	private List<Dictionary> readRoles() {
		if (roles == null)
			return new ArrayList<>();
		return ServicesLoader.get(CommonService.class)
				.listFunctionRoles(new BasicDBObject("filter", new BasicDBObject("id", new BasicDBObject("$in", roles))));
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "流程定义";

	@Override
	public String toString() {
		return "" + name + "/" + bpmnId;
	}

}
