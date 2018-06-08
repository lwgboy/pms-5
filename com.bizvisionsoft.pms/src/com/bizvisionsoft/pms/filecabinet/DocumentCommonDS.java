package com.bizvisionsoft.pms.filecabinet;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.serviceconsumer.Services;

public class DocumentCommonDS {

	@DataSet("文档类型选择/" + DataSet.LIST)
	private List<String> lisCategory() {
		return Services.get(CommonService.class).listDictionary("文档类型", "name");
	}

	@DataSet("Tag选择/" + DataSet.LIST)
	private List<String> listTag() {
		return Services.get(CommonService.class).listDictionary("Tag", "name");
	}
	
	

}
