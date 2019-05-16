package com.bizvisionsoft.pms.filecabinet;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.serviceconsumer.Services;

public class DocumentCommonDS {

	@DataSet("�ĵ�����ѡ��/" + DataSet.LIST)
	private List<String> lisCategory(@MethodParam(MethodParam.DOMAIN) String domain) {
		return Services.get(CommonService.class).listDictionary("�ĵ�����", "name", domain);
	}

	@DataSet("Tagѡ��/" + DataSet.LIST)
	private List<String> listTag(@MethodParam(MethodParam.DOMAIN) String domain) {
		return Services.get(CommonService.class).listDictionary("Tag", "name", domain);
	}

}
