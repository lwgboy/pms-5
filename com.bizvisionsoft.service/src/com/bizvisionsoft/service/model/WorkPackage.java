package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;

@PersistenceCollection("workPackage")
public class WorkPackage {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	/**
	 * 工作包的名称，与TrackView名称一致
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * 工作包的类型，与TrackView一致
	 */
	@ReadValue
	@WriteValue
	private String catagory;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "工作包";

	@Override
	@Label
	public String toString() {
		return name == null ? "基本工作包" : name;
	}

	@ReadValue
	@WriteValue
	@Persistence
	private String chargerId;

	@SetValue
	@ReadValue
	private String chargerInfo;

	@WriteValue("charger")
	private void setCharger(User charger) {
		this.chargerId = Optional.ofNullable(charger).map(o -> o.getUserId()).orElse(null);
		this.chargerInfo = charger.toString();
	}

	@ReadValue("charger")
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	@SetValue
	private Work work;

	@Persistence
	private ObjectId work_id;

	@ReadValue("deadline")
	private Date getDeadline() {
		return work.getPlanFinished();
	}

	public static WorkPackage newInstance(Work work, TrackView tv) {
		WorkPackage wp = new WorkPackage();
		wp.work_id = work.get_id();
		wp.work = work;
		if (tv != null) {
			wp.catagory = tv.getCatagory();
			wp.name = tv.getName();
		}
		return wp;
	}

}
