package com.bizvisionsoft.pms.filecabinet;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.serviceconsumer.Services;

public class DocumentCommonDS {

	@DataSet("�ĵ�����ѡ��/" + DataSet.LIST)
	private List<String> lisCategory() {
		return Services.get(CommonService.class).listDictionary("�ĵ�����", "name");
	}

	@DataSet("Tagѡ��/" + DataSet.LIST)
	private List<String> listTag() {
		return Services.get(CommonService.class).listDictionary("Tag", "name");
	}
	
	

}
