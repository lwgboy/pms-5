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
	public static final String typeName = "��������";

	@ReadValue
	@WriteValue
	private boolean enabled;

	@ReadValue
	@WriteValue
	private boolean global;
	
	@ReadValue
	@Persistence
	private double basicWorks;
	
	@WriteValue("basicWorks")
	public void setWorks(String _basicWorks) {
		double __basicWorks;
		try {
			__basicWorks = Double.parseDouble(_basicWorks);
		} catch (Exception e) {
			throw new RuntimeException("�������ʹ���");
		}
		if (__basicWorks <= 0) {
			throw new RuntimeException("ÿ�ձ�׼��ʱ������㡣");
		}
		basicWorks = __basicWorks;
	}
	
	@ReadValue
	@Persistence
	private double overTimeWorks;
	
	@WriteValue("overTimeWorks")
	public void setOverTimeWorks(String _overTimeWorks) {
		double __overTimeWorks;
		try {
			__overTimeWorks = Double.parseDouble(_overTimeWorks);
		} catch (Exception e) {
			throw new RuntimeException("�������ʹ���");
		}
		if (__overTimeWorks < 0) {
			throw new RuntimeException("ÿ�ռӰ๤ʱ����ڵ����㡣");
		}
		overTimeWorks = __overTimeWorks;
	}

	@Behavior({ "��������/���","��������/�������", "��������/ɾ��", "��������/�༭" })
	@Exclude
	private boolean behavior = true;

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@SetValue
	private List<WorkTime> workTime;

	@Structure("��������/list")
	public List<WorkTime> getWorkTime() {
		return Optional.ofNullable(workTime).orElse(new ArrayList<WorkTime>());
	}

	@Structure("��������/count")
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
