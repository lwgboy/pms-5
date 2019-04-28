package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("taskDefinition")
public class TaskDefinition {
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "任务定义";

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String taskName;

	@ReadValue
	@WriteValue
	@Label(Label.NAME_LABEL)
	private String name;

	@ReadValue
	@WriteValue
	private String nodeId;

	@ReadValue
	@WriteValue
	private ObjectId processDefinitionId;

	@ReadValue
	@WriteValue
	private String type;

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
	
	@ReadValue("工作流定义列表/properties")
	private String readpropertiesByGrid() {
		StringBuffer sb = new StringBuffer();
		if (properties != null) {
			properties.entrySet().forEach(e -> {
				sb.append(e.getKey()+"="+e.getValue()+";");
			});
		}
		return sb.toString();
		
	}

	@ReadValue("任务定义编辑器/properties")
	private List<Document> readpropertiesByEditor() {
		List<Document> result = new ArrayList<>();
		if (properties != null) {
			properties.entrySet().forEach(e -> {
				result.add(new Document("name", e.getKey()).append("value", e.getValue()));
			});
		}
		return result;
	}

	public TaskDefinition setName(String name) {
		this.name = name;
		return this;
	}

	public TaskDefinition setTaskName(String taskName) {
		this.taskName = taskName;
		return this;
	}

	public TaskDefinition setData(Document data) {
		return setName(data.getString("name"))//
				.setTaskName(data.getString("taskName"))//
				.setEditor(data.getString("editor"))//
				.setNodeId(data.getString("nodeId"))//
				.setFunction(data.getString("function"))//
				.setProperties((Document) data.get("properties"))//
				.setScript(data.getString("script"))//
				.setNodeId(data.getString("nodeId"))//
				.setProcessDefinitionId(data.getObjectId("processDefinitionId"))//
				.setType(data.getString("type"))//
				.set_Id(data.getObjectId("_id"))//
		;
	}

	private TaskDefinition setEditor(String editor) {
		this.editor = editor;
		return this;
	}

	private TaskDefinition setFunction(String function) {
		this.function = function;
		return this;
	}

	public TaskDefinition setProcessDefinitionId(ObjectId processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
		return this;
	}

	private TaskDefinition setProperties(Document properties) {
		this.properties = properties;
		return this;
	}

	private TaskDefinition setScript(String script) {
		this.script = script;
		return this;
	}

	private TaskDefinition setType(String type) {
		this.type = type;
		return this;
	}

	private TaskDefinition set_Id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public TaskDefinition setNodeId(String nodeId) {
		this.nodeId = nodeId;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}
	
	@Override
	@Label
	public String toString() {
		return "" + name + "/" + taskName;
	}
	
}
