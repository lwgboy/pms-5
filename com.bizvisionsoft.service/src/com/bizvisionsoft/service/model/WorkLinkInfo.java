package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkSpaceService;

@PersistenceCollection("worklinksspace")
@Strict
public class WorkLinkInfo {

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@Persistence
	private ObjectId _id;

	@ReadValue("项目甘特图（编辑）/id")
	public String getId() {
		return _id.toHexString();
	}

	@WriteValue("项目甘特图（编辑）/id")
	public WorkLinkInfo setId(String id) {
		this._id = new ObjectId(id);
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@Persistence
	private ObjectId project_id;

	@ReadValue("项目甘特图（编辑）/project")
	public String getProject() {
		return project_id == null ? null : project_id.toHexString();
	}

	@WriteValue("项目甘特图（编辑）/project")
	public WorkLinkInfo setProject(String project_id) {
		this.project_id = project_id == null ? null : new ObjectId(project_id);
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	private WorkInfo source;

	@ReadValue("项目甘特图（编辑）/source")
	public String getSource() {
		return source == null ? null : source.get_id().toHexString();
	}

	@ReadValue("工作搭接关系编辑器（1对1）/sourceTask")
	public String getSourceTaskLabel() {
		return source.toString();
	}

	@GetValue("source")
	public ObjectId getSourceId() {
		return source.get_id();
	}

	@SetValue("source")
	public void setSourceId(ObjectId source_id) {
		source = ServicesLoader.get(WorkSpaceService.class).getWorkInfo(source_id);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	private WorkInfo target;

	@ReadValue("项目甘特图（编辑）/target")
	public String getTarget() {
		return target == null ? null : target.get_id().toHexString();
	}

	@ReadValue("工作搭接关系编辑器（1对1）/targetTask")
	public String getTargetTaskLabel() {
		return target.toString();
	}

	@GetValue("target")
	public ObjectId getTargetId() {
		return target.get_id();
	}

	@SetValue("target")
	public void setTargetId(ObjectId target_id) {
		target = ServicesLoader.get(WorkSpaceService.class).getWorkInfo(target_id);
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
	// o
	@ReadValue("项目甘特图（编辑）/lag")
	@WriteValue("项目甘特图（编辑）/lag")
	@Persistence
	private int lag;

	@Persistence
	private ObjectId space_id;

	@WriteValue("工作搭接关系编辑器（1对1）/lag")
	public WorkLinkInfo setLagFromEditor(String lag) {
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

	public WorkLinkInfo set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public static WorkLinkInfo newInstance(ObjectId project_id) {
		return new WorkLinkInfo().set_id(new ObjectId()).setProject_id(project_id);
	}

	public WorkLinkInfo setSource(WorkInfo source) {
		this.source = source;
		return this;
	}

	public WorkLinkInfo setTarget(WorkInfo target) {
		this.target = target;
		return this;
	}

	public WorkLinkInfo setType(String type) {
		this.type = type;
		return this;
	}
	
	public String getType() {
		return type;
	}

	public WorkLinkInfo setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public WorkLinkInfo setSpaceId(ObjectId space_id) {
		this.space_id = space_id;
		return this;
	}

	public WorkLinkInfo setLag(int lag) {
		this.lag = lag;
		return this;
	}
	
	public int getLag() {
		return lag;
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
		WorkLinkInfo other = (WorkLinkInfo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}


}
