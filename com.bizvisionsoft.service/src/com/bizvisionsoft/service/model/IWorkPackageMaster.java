package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public interface IWorkPackageMaster {

	List<TrackView> getWorkPackageSetting();

	ObjectId get_id();

	Date getPlanFinish();

	default boolean isTemplate() {
		return false;
	}

}
