package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class CBSEstimationSetting {
	
	@ReadValue
	public Integer episodeCount;
	
	@ReadValue
	public Integer episodeTime;

	@WriteValue("episodeCount")
	public void setEpisodeCount(String input) {
		try {
			this.episodeCount = Integer.parseInt(input);
		} catch (Exception e) {
			throw new RuntimeException("����Ҫ������������");
		}
	}
	
	@WriteValue("episodeTime")
	public void setEpisodeTime(String input) {
		try {
			this.episodeTime = Integer.parseInt(input);
		} catch (Exception e) {
			throw new RuntimeException("ʱ��Ҫ������������");
		}
	}
}
