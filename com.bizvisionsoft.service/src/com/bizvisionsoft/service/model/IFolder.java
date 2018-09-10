package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

public interface IFolder {

	ObjectId get_id();

	IFolder setOpened(boolean expanded);

	IFolder setName(String name);

}
