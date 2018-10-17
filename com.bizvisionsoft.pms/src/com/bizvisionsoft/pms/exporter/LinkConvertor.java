package com.bizvisionsoft.pms.exporter;

import java.util.Map;

import org.bson.types.ObjectId;

import net.sf.mpxj.Task;

@FunctionalInterface
public interface LinkConvertor<T> {

	 void accept(T link, Map<ObjectId, Task> taskMap);
	
}
