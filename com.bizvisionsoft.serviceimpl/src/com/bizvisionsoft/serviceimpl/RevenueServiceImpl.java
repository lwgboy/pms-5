package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.RevenueService;
import com.bizvisionsoft.service.model.RevenueForecastItem;
import com.bizvisionsoft.service.model.RevenueRealizeItem;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class RevenueServiceImpl extends BasicServiceImpl implements RevenueService {

	@Override
	public String getRevenueForecastType(ObjectId scope_id) {
		Document rfi = c("revenueForecastItem").find(new Document("scope_id", scope_id)).first();
		if (rfi == null) {
			return "";
		} else if (rfi.get("quarter") != null) {
			return "季度";
		} else if (rfi.get("month") != null) {
			return "月";
		} else {
			return "年";
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
		double amount = Optional.ofNullable(rfi.getAmount()).orElse(0d);

		Document condition = new Document("type", rfi.getType()).append("scope_id", rfi.getScope_id())
				.append("index", rfi.getIndex()).append("subject", rfi.getSubject());
		Document doc = c("revenueForecastItem").find(condition).first();
		if (doc == null) {
			if (amount != 0) {
				c(RevenueForecastItem.class).insertOne(rfi);
			}
		} else {
			double _amount = Optional.ofNullable((Double) doc.get("amount")).orElse(0d);
			if (amount != _amount)
				c("revenueForecastItem").updateOne(condition, new Document("$set", new Document("amount", amount)));
		}
	}

	@Override
	public void updateRevenueRealizeItem(RevenueRealizeItem rfi) {
		double amount = Optional.ofNullable(rfi.getAmount()).orElse(0d);
		Document condition = new Document("scope_id", rfi.getScope_id()).append("id", rfi.getId()).append("subject",
				rfi.getSubject());
		Document doc = c("revenueRealizeItem").find(condition).first();
		if (doc == null) {
			if (amount != 0) {
				c(RevenueRealizeItem.class).insertOne(rfi);
			}
		} else {
			double _amount = Optional.ofNullable((Double) doc.get("amount")).orElse(0d);
			if (amount != _amount)
				c("revenueRealizeItem").updateOne(condition, new Document("$set", new Document("amount", amount)));
		}

	}

	@Override
	public double getRevenueForecastAmount(ObjectId scope_id, String subject, String type, int index) {
		Document doc = c("revenueForecastItem").find(new Document("scope_id", scope_id).append("subject", subject)
				.append("type", type).append("index", index)).first();
		return Optional.ofNullable(doc).map(d -> d.getDouble("amount")).map(d -> d.doubleValue()).orElse(0d);
	}

	@Override
	public double getRevenueRealizeAmount(ObjectId scope_id, String subject, String id) {
		Document doc = c("revenueRealizeItem")
				.find(new Document("scope_id", scope_id).append("subject", subject).append("id", id)).first();
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

	@Override
	public List<Document> groupRevenueRealizeAmountByPeriod(ObjectId scope_id) {
		return c("revenueRealizeItem").aggregate(new JQ("查询-SCOPE-收益数据").set("scope_id", scope_id).array())
				.into(new ArrayList<>());
	}

	@Override
	public List<String> getRevenueRealizePeriod(ObjectId scope_id) {
		ArrayList<String> result = c("revenueRealizeItem")
				.distinct("id", new Document("scope_id", scope_id), String.class).into(new ArrayList<>());
		Collections.sort(result);
		return result;
	}

}
