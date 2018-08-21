package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.RevenueService;
import com.bizvisionsoft.service.model.RevenueForecastItem;

public class RevenueServiceImpl extends BasicServiceImpl implements RevenueService {

	@Override
	public String getRevenueForecastType(ObjectId scope_id) {
		Document rfi = c("revenueForecastItem").find(new Document("scope_id", scope_id)).first();
		if (rfi == null) {
			return "";
		} else if (rfi.get("quarter") != null) {
			return "¼¾¶È";
		} else if (rfi.get("month") != null) {
			return "ÔÂ";
		} else {
			return "Äê";
		}
	}

	@Override
	public int getForwardRevenueForecastIndex(ObjectId scope_id) {
		Document doc = c("revenueForecastItem").find(new Document("scope_id", scope_id)).sort(new Document("index", -1))
				.first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index")).orElse(0);
	}

	@Override
	public void updateRevenueForecastItem(RevenueForecastItem rfi) {
		Document condition = new Document("type", rfi.getType()).append("scope_id", rfi.getScope_id())
				.append("index", rfi.getIndex()).append("subject", rfi.getSubject());
		if (c("revenueForecastItem").countDocuments(condition) == 0) {
			c(RevenueForecastItem.class).insertOne(rfi);
		} else {
			c("revenueForecastItem").updateOne(condition,
					new Document("$set", new Document("amount", rfi.getAmount())));
		}
	}

	@Override
	public double getRevenueForecastAmount(ObjectId scope_id, String subject, String type, int index) {
		Document doc = c("revenueForecastItem").find(new Document("scope_id", scope_id).append("subject", subject)
				.append("type", type).append("index", index)).first();
		return Optional.ofNullable(doc).map(d -> d.getDouble("amount")).map(d -> d.doubleValue()).orElse(0d);
	}

	@Override
	public List<RevenueForecastItem> listRevenueForecast(ObjectId scope_id) {
		return c(RevenueForecastItem.class).find(new Document("scope_id", scope_id)).into(new ArrayList<>());
	}

	@Override
	public void clearRevenueForecast(ObjectId scope_id) {
		c(RevenueForecastItem.class).deleteMany(new Document("scope_id", scope_id));
	}
	
}
