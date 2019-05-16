package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.RevenueService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.model.RevenueForecastItem;
import com.bizvisionsoft.service.model.RevenueRealizeItem;

public class RevenueServiceImpl extends BasicServiceImpl implements RevenueService {

	@Override
	public String getRevenueForecastType(ObjectId scope_id, String domain) {
		Document rfi = c("revenueForecastItem", domain).find(new Document("scope_id", scope_id)).first();
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
	public int getForwardRevenueForecastIndex(ObjectId scope_id, String domain) {
		Document doc = c("revenueForecastItem", domain).find(new Document("scope_id", scope_id)).sort(new Document("index", -1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index")).orElse(0);
	}

	@Override
	public void updateRevenueForecastItem(RevenueForecastItem rfi, String domain) {
		double amount = Optional.ofNullable(rfi.getAmount()).orElse(0d);

		Document condition = new Document("type", rfi.getType()).append("scope_id", rfi.getScope_id()).append("index", rfi.getIndex())
				.append("subject", rfi.getSubject());
		Document doc = c("revenueForecastItem", domain).find(condition).first();
		if (doc == null) {
			if (amount != 0) {
				c(RevenueForecastItem.class, domain).insertOne(rfi);
			}
		} else {
			double _amount = Optional.ofNullable((Double) doc.get("amount")).orElse(0d);
			if (amount != _amount)
				c("revenueForecastItem", domain).updateOne(condition, new Document("$set", new Document("amount", amount)));
		}
	}

	@Override
	public void updateRevenueRealizeItem(RevenueRealizeItem rfi, String domain) {
		double amount = Optional.ofNullable(rfi.getAmount()).orElse(0d);
		Document condition = new Document("scope_id", rfi.getScope_id()).append("id", rfi.getId()).append("subject", rfi.getSubject());
		Document doc = c("revenueRealizeItem", domain).find(condition).first();
		if (doc == null) {
			if (amount != 0) {
				c(RevenueRealizeItem.class, domain).insertOne(rfi);
			}
		} else {
			double _amount = Optional.ofNullable((Double) doc.get("amount")).orElse(0d);
			if (amount != _amount)
				c("revenueRealizeItem", domain).updateOne(condition, new Document("$set", new Document("amount", amount)));
		}

	}

	@Override
	public double getRevenueForecastAmount(ObjectId scope_id, String subject, String type, int index, String domain) {
		Document doc = c("revenueForecastItem", domain)
				.find(new Document("scope_id", scope_id).append("subject", subject).append("type", type).append("index", index)).first();
		return Optional.ofNullable(doc).map(d -> d.getDouble("amount")).map(d -> d.doubleValue()).orElse(0d);
	}

	@Override
	public double getRevenueRealizeAmount(ObjectId scope_id, String subject, String id, String domain) {
		Document doc = c("revenueRealizeItem", domain).find(new Document("scope_id", scope_id).append("subject", subject).append("id", id))
				.first();
		return Optional.ofNullable(doc).map(d -> d.getDouble("amount")).map(d -> d.doubleValue()).orElse(0d);
	}

	@Override
	public List<RevenueForecastItem> listRevenueForecast(ObjectId scope_id, String domain) {
		return c(RevenueForecastItem.class, domain).find(new Document("scope_id", scope_id)).into(new ArrayList<>());
	}

	@Override
	public void clearRevenueForecast(ObjectId scope_id, String domain) {
		c(RevenueForecastItem.class, domain).deleteMany(new Document("scope_id", scope_id));
	}

	@Override
	public List<Document> groupRevenueRealizeAmountByPeriod(ObjectId scope_id, String domain) {
		return c("revenueRealizeItem", domain).aggregate(Domain.getJQ(domain, "查询-SCOPE-收益数据").set("scope_id", scope_id).array()).into(new ArrayList<>());
	}

	@Override
	public List<String> getRevenueRealizePeriod(ObjectId scope_id, String domain) {
		ArrayList<String> result = c("revenueRealizeItem", domain).distinct("id", new Document("scope_id", scope_id), String.class)
				.into(new ArrayList<>());
		Collections.sort(result);
		return result;
	}

	@Override
	public void deleteRevenueRealize(ObjectId scope_id, String id, String domain) {
		c("revenueRealizeItem", domain).deleteMany(new Document("scope_id", scope_id).append("id", id));
	}

	@Override
	public void deleteRevenueForecast(ObjectId scope_id, int index, String domain) {
		c("revenueForecastItem", domain).deleteMany(new Document("scope_id", scope_id).append("index", index));
	}

}
