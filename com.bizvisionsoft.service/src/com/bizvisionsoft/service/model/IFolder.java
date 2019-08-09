package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.tools.Formatter;

public interface IFolder {

	public final static IFolder Null = new VaultFolder().set_id(Formatter.ZeroObjectId());
	
	public static IFolder newInstance(ObjectId _id) {
		return new VaultFolder().set_id(_id);
	}

	ObjectId get_id();

	IFolder setName(String name);

	String getName();

	IFolder getContainer();

	boolean isContainer();

}
