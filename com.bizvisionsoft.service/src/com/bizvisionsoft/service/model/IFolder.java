package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.tools.Formatter;

public interface IFolder {

	public final static IFolder Null = new VaultFolder().set_id(Formatter.ZeroObjectId());

	/**
	 * 创建一个只有_id的目录对象
	 * 
	 * @param _id
	 * @return
	 */
	public static IFolder newInstance(ObjectId _id) {
		IFolder fld = new IFolder() {

			@Override
			public ObjectId get_id() {
				return _id;
			}

			@Override
			public IFolder setName(String name) {
				return this;
			}

			@Override
			public String getName() {
				return "";
			}

			@Override
			public IFolder getContainer() {
				return null;
			}

			@Override
			public boolean isContainer() {
				return false;
			}
		};
		return fld;
	}

	ObjectId get_id();

	IFolder setName(String name);

	String getName();

	IFolder getContainer();

	boolean isContainer();

}
