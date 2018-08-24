package com.bizvisionsoft.service.model;

import java.util.List;

import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;

public interface IRevenueScope extends IScope{
	
	public default List<AccountIncome> defaultListAccountIncome(){
		return ServicesLoader.get(CommonService.class).getAccoutIncomeRoot();
	}
	
	public default long defaultCountAccountIncome() {
		return ServicesLoader.get(CommonService.class).countAccoutIncomeRoot();
	}

	public String getRevenueForecastType();

	public List<AccountIncome> getRootAccountIncome();
	
}
