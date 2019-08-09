package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("folder")
public class VaultFolderDescriptor implements Comparable<VaultFolderDescriptor> {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	@SetValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	@SetValue
	private String desc;

	@ReadValue
	@WriteValue
	@SetValue
	private Integer level;

	@Override
	public int compareTo(VaultFolderDescriptor o) {
		return -1 * level.compareTo(o.level);
	}

	public String getName() {
		return desc;
	}

}
