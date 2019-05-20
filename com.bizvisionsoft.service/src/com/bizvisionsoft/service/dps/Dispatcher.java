package com.bizvisionsoft.service.dps;

import java.util.List;
import java.util.Map;

public interface Dispatcher {

	public Map<String, Object> run(List<Map<String, Object>> processors,String processorTypeId) throws Exception;

}
