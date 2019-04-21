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
	private String type;

	@ReadValue
	@WriteValue
	private Boolean enabled;

	private Document properties;

	@ReadValue
	@WriteValue
	private String editor;

	@ReadValue
	@WriteValue
	private String script;

	@ReadValue
	@WriteValue
	private String function;

	/**
	 * @param input
	 *            输入的是name,value
	 */
	@WriteValue("properties")
	private void writepropertiesByEditor(List<Document> input) {
		properties = new Document();
		if (input != null) {
			input.forEach(d -> properties.append(d.getString("name"), d.get("value")));
		}
	}

	@ReadValue("properties")
	private List<Document> readpropertiesByEditor() {
		List<Document> result = new ArrayList<>();
		if (properties != null) {
			properties.entrySet().forEach(e -> {
				result.add(new Document("name", e.getKey()).append("value", e.getValue()));
			});
		}
		return result;
	}

	@WriteValue
	private List<String> users;

	@ReadValue
	@WriteValue
	private Boolean allUser;

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

	public String getName() {
		return name;
	}

	public ProcessDefinition setName(String name) {
		this.name = name;
		return this;
	}

	public String getBpmnId() {
		return bpmnId;
	}

	public ProcessDefinition setBpmnId(String bpmnId) {
		this.bpmnId = bpmnId;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public ProcessDefinition setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getType() {
		return type;
	}

	public ProcessDefinition setType(String type) {
		this.type = type;
		return this;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public ProcessDefinition setEnabled(Boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public Document getProperties() {
		return properties;
	}

	public ProcessDefinition setProperties(Document properties) {
		this.properties = properties;
		return this;
	}

	public List<String> getUsers() {
		return users;
	}

	public ProcessDefinition setUsers(List<String> users) {
		this.users = users;
		return this;
	}

	public Boolean getAllUser() {
		return allUser;
	}

	public ProcessDefinition setAllUser(Boolean allUser) {
		this.allUser = allUser;
		return this;
	}

	public List<ObjectId> getOrganizations() {
		return organizations;
	}

	public ProcessDefinition setOrganizations(List<ObjectId> organizations) {
		this.organizations = organizations;
		return this;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getEditor() {
		return editor;
	}

	public String getFunction() {
		return function;
	}

	public String getScript() {
		return script;
	}

	public ProcessDefinition setEditor(String editor) {
		this.editor = editor;
		return this;

	}

	public ProcessDefinition setFunction(String function) {
		this.function = function;
		return this;
	}

	public ProcessDefinition setScript(String script) {
		this.script = script;
		return this;
	}

}
