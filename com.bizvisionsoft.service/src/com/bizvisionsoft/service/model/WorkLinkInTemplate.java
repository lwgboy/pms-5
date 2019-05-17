package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("worklinkInTemplate")
@Strict
public class WorkLinkInTemplate {

	@Exclude
	public String domain;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@Persistence
	private ObjectId _id;

	@ReadValue("id")
	public String getId() {
		return _id.toHexString();
	}

	@WriteValue("id")
	public WorkLinkInTemplate setId(String id) {
		this._id = new ObjectId(id);
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@Persistence
	private ObjectId template_id;

	@ReadValue("project")
	public String getTemplateId() {
		return template_id == null ? null : template_id.toHexString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	private WorkInTemplate source;

	@ReadValue("source")
	public String getSource() {
		return source == null ? null : source.getId();
	}

	@ReadValue("sourceTask")
	public String getSourceTaskLabel() {
		return source.toString();
	}

	@GetValue("source")
	public ObjectId getSourceId() {
		return source.get_id();
	}

	@SetValue("source")
	public void setSource(ObjectId source_id) {
		source = ServicesLoader.get(ProjectTemplateService.class).getWorkInTemplate(source_id, domain);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	private WorkInTemplate target;

	@ReadValue("target")
	public String getTarget() {
		return target == null ? null : target.getId();
	}

	@ReadValue("targetTask")
	public String getTargetTaskLabel() {
		return target.toString();
	}

	@GetValue("target")
	public ObjectId getTargetId() {
		return target.get_id();
	}

	@SetValue("target")
	public void setTarget(ObjectId target_id) {
		target = ServicesLoader.get(ProjectTemplateService.class).getWorkInTemplate(target_id, domain);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@ReadValue
	@WriteValue
	@Persistence
	private String type;
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@ReadValue("项目模板甘特图/lag")
	@WriteValue("项目模板甘特图/lag")
	@Persistence
	private int lag;

	@WriteValue("工作搭接关系编辑器（1对1）/lag")
	public WorkLinkInTemplate setLagFromEditor(String lag) {
		this.lag = Integer.parseInt(lag);
		return this;
	}

	@ReadValue("工作搭接关系编辑器（1对1）/lag")
	public String getLagForEdior() {
		return "" + lag;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 以下是控制gantt的客户端的属性
	@ReadValue("editable")
	public Boolean getEditable() {
		return true;
	}

	public WorkLinkInTemplate set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public static WorkLinkInTemplate newInstance(ObjectId template_id, String domain) {
		return new WorkLinkInTemplate().setDomain(domain).set_id(new ObjectId()).setTemplate_id(template_id);
	}

	private WorkLinkInTemplate setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public WorkLinkInTemplate setSource(WorkInTemplate source) {
		this.source = source;
		return this;
	}

	public WorkLinkInTemplate setTarget(WorkInTemplate target) {
		this.target = target;
		return this;
	}

	public WorkLinkInTemplate setType(String type) {
		this.type = type;
		return this;
	}

	public WorkLinkInTemplate setTemplate_id(ObjectId template_id) {
		this.template_id = template_id;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkLinkInTemplate other = (WorkLinkInTemplate) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
}
