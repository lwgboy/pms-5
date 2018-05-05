package com.bizvisionsoft.demo.rsclient;

import java.util.Arrays;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;

public class DemoForDataSource {
	
	@DataSet("字符串选择表格演示/list")
	public List<String> getDataSource(){
		return Arrays.asList(new String[]{"演示1","Demo2","演示3"});
	}

}
