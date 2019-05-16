package com.bizvisionsoft.pms.filecabinet;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.serviceconsumer.Services;

public class DocumentCommonDS {

	@DataSet("文档类型选择/" + DataSet.LIST)
	private List<String> lisCategory(@MethodParam(MethodParam.DOMAIN) String domain) {
		return Services.get(CommonService.class).listDictionary("文档类型", "name", domain);
	}

	@DataSet("Tag选择/" + DataSet.LIST)
	private List<String> listTag(@MethodParam(MethodParam.DOMAIN) String domain) {
		return Services.get(CommonService.class).listDictionary("Tag", "name", domain);
	}

}
