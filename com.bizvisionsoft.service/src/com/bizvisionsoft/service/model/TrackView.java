package com.bizvisionsoft.service.model;

import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.google.gson.internal.LinkedTreeMap;

@PersistenceCollection("trackView")
public class TrackView {

	@Exclude
	public static String CATAGORY_DEVELOPMENT = "研发";
	
	@Exclude
	public static String CATAGORY_PURCHASE = "采购";
	
	@Exclude
	public static String CATAGORY_PRODUCTION = "生产";
	
	@Exclude
	public static String CATAGORY_QUALITY = "质量";

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String catagory;

	public String getCatagory() {
		return catagory;
	}

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue("text")
	public String getName() {
		return name;
	}

	@ReadValue
	@WriteValue
	private String viewAssembly;

	public String getViewAssembly() {
		return viewAssembly;
	}

	@ReadValue
	@WriteValue
	private String packageAssembly;

	public String getPackageAssembly() {
		return packageAssembly;
	}

	@ReadValue
	@WriteValue
	private String editAssembly;

	public String getEditAssembly() {
		return editAssembly;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "视图和工作包";

	@Override
	@Label
	public String toString() {
		return catagory + "/" + name;
	}

	public ObjectId get_id() {
		return _id;
	}

	@WriteValue
	@ReadValue
	private Map<String, Object> parameter;

	public TrackView setParameter(String name, Object value) {
		if (parameter == null) {
			parameter = new LinkedTreeMap<>();
		}
		parameter.put(name, value);
		return this;
	}

	public Object getParameter(String name) {
		return Optional.ofNullable(parameter).map(p -> p.get(name)).orElse(null);
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
		TrackView other = (TrackView) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

}
