package com.bizvisionsoft.pms.exporter;

import java.util.Map;

import org.bson.types.ObjectId;

import net.sf.mpxj.Task;

@FunctionalInterface
public interface TaskConvertor<T> {

	 void accept(T work,Task task, Map<ObjectId, Task> taskMap);
	
}
