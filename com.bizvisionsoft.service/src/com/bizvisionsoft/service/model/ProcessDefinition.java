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

	public void setName(String name) {
		this.name = name;
	}

	public String getBpmnId() {
		return bpmnId;
	}

	public void setBpmnId(String bpmnId) {
		this.bpmnId = bpmnId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Document getProperties() {
		return properties;
	}

	public void setProperties(Document properties) {
		this.properties = properties;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public Boolean getAllUser() {
		return allUser;
	}

	public void setAllUser(Boolean allUser) {
		this.allUser = allUser;
	}

	public List<ObjectId> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<ObjectId> organizations) {
		this.organizations = organizations;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	

}
