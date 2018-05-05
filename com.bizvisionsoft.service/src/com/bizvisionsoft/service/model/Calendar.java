package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("calendar")
public class Calendar {

	////////////////////////////////////////////////////////////////////////////////////////////////////
	@Persistence
	@ReadValue
	@WriteValue
	private ObjectId _id;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	@Persistence
	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "工作日历";

	@ReadValue
	@WriteValue
	private boolean enabled;

	@ReadValue
	@WriteValue
	private boolean global;

	@Behavior({ "工作日历/添加", "工作日历/删除", "工作日历/编辑" })
	private boolean behavior = true;

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@SetValue
	private List<WorkTime> workTime;

	@Structure("工作日历/list")
	public List<WorkTime> getWorkTime() {
		return Optional.ofNullable(workTime).orElse(new ArrayList<WorkTime>());
	}

	@Structure("工作日历/count")
	public long countWorkTime() {
		return Optional.ofNullable(workTime).map(w -> w.size()).orElse(0);
	}

	public ObjectId get_id() {
		return _id;
	}

	public void addWorkTime(WorkTime wt) {
		if (workTime == null)
			workTime = new ArrayList<WorkTime>();
		workTime.add(wt);
	}

}
