package com.bizvisionsoft.serviceimpl.valuegen;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.mongocodex.tools.IValueGenerateService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.model.ValueRule;
import com.bizvisionsoft.service.model.ValueRuleSegment;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.JSTools;
import com.bizvisionsoft.serviceimpl.SystemServiceImpl;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;

public class DocumentValueGenerator extends SystemServiceImpl implements IValueGenerateService {

	public Logger logger = LoggerFactory.getLogger(getClass());

	private List<ValueRuleSegment> segments;

	private String domain;

	public DocumentValueGenerator(ValueRule rule) {
		this.domain = rule.domain;
		segments = listValueRuleSegment(new BasicDBObject(), rule._id, rule.domain);
	}

	@Override
	public String getValue(Object input) {
		Map<ValueRuleSegment, String> stage = new HashMap<ValueRuleSegment, String>();
		return segments.stream().sorted((s1, s2) -> s1.executeSequance - s2.executeSequance)// 按执行顺序排序
				.map(s -> this.getSegmentValue(input, s, stage))// 计算值
				.sorted((s1, s2) -> s1.getKey().index - s2.getKey().index)// 按段号排序
				.reduce("", (s, e) -> e.getKey().disableOutput ? s : (s + e.getValue()), (s1, s2) -> s1);
	}

	private Entry<ValueRuleSegment, String> getSegmentValue(Object input, ValueRuleSegment seg, Map<ValueRuleSegment, String> stage) {
		Object value = null;
		// 类型1.常量字符串
		if ("常量".equals(seg.type)) {
			value = seg.value;
		} else if ("当前日期时间".equals(seg.type)) {// 类型2.日期
			value = new Date();
		} else if ("当前时间戳".equals(seg.type)) {// 类型3.时间戳
			value = System.currentTimeMillis();
		} else if ("字段".equals(seg.type)) {// 类型4.字段值
			value = getFieldValue(input, seg);
		} else if ("JavaScript".equals(seg.type)) {// 类型5.脚本
			value = getJSValue(input, seg);
		} else if ("查询".equals(seg.type)) {// 类型6.查询
			value = getQueryValue(input, seg, domain);
		} else if ("流水号".equals(seg.type)) {
			value = getSNValue(seg, stage, domain);
		}

		String text = format(seg, value);
		stage.put(seg, text);
		return new AbstractMap.SimpleEntry<ValueRuleSegment, String>(seg, text);
	}

	private Object getFieldValue(Object input, ValueRuleSegment seg) {
		if (input == null) {
			return null;
		}
		if (Check.isAssigned(seg.name)) {
			String[] _v = seg.name.split("/");
			String cName = null;
			String fName = null;
			if (_v.length == 1) {
				fName = _v[0];
			} else {
				cName = _v[0];
				fName = _v[1];
			}
			if (fName != null) {
				try {
					return AUtil.readValue(input, cName, fName, null);
				} catch (Exception e) {
					logger.error("读取字段值失败。", e);
				}
			}

		} else {
			logger.warn("字段值类型缺少name的定义，取值被忽略。");
		}
		return null;
	}

	private Object getJSValue(Object input, ValueRuleSegment seg) {
		if (Check.isAssigned(seg.script)) {
			String jsInput = new GsonBuilder().create().toJson(input);
			Document binding = new Document("input", input).append("jsonInput", "jsonInput");
			return JSTools.invoke(seg.script, seg.function, "output", binding, input, jsInput);
		} else {
			logger.warn("脚本类型缺少脚本的定义，取值被忽略。");
			return null;
		}
	}

	private Object getSNValue(ValueRuleSegment seg, Map<ValueRuleSegment, String> stage, String domain) {
		// 取流水号Id
		if (Check.isAssigned(seg.snId)) {
			StringBuffer sb = new StringBuffer();
			Arrays.asList(seg.snId.split("-")).stream().mapToInt(Integer::parseInt).forEach(i -> {
				if (i < 0) {
					sb.append(".undefine");
				} else {
					stage.keySet().stream().filter(t -> t.index == i).findFirst().ifPresent(vrs -> sb.append("." + stage.get(vrs)));
				}
			});
			String key = sb.toString();
			if (!key.isEmpty()) {
				return generateCode("Id_Gen", key, domain);
			}
		}
		return null;
	}

	private Object getQueryValue(Object input, ValueRuleSegment seg, String domain) {
		if (!Check.isAssigned(seg.collection)) {
			logger.warn("查询类型缺少集合名的定义，取值被忽略。");
			return null;
		}
		if (Check.isNotAssigned(seg.pipelineJson) && Check.isNotAssigned(seg.query)) {
			logger.warn("查询类型缺少查询的定义，取值被忽略。");
			return null;
		}

		JQ jq;
		if (Check.isAssigned(seg.pipelineJson)) {
			jq = new JQ();
		} else {
			// 根据JQ文件取数
			jq = Domain.getJQ(domain, seg.query);
		}

		if (seg.params != null) {
			Arrays.asList(seg.params.split(";")).forEach(d -> {
				String[] _v = d.split("=");
				String[] _n = _v[1].split("/");
				String pName = _v[0].trim();
				String cName = null;
				String fName = null;
				if (_n.length > 1) {
					cName = _n[0].trim();
					fName = _n[1].trim();
				} else if (_n.length == 1) {
					fName = _n[0].trim();
				} else {
					logger.warn("参数表达式语法错误。");
					return;
				}
				if (Check.isAssigned(fName, pName)) {
					try {
						Object pValue = AUtil.readValue(input, cName, fName, null);
						jq.set(pName, pValue);
					} catch (Exception e) {
						logger.warn(e.getMessage());
					}
				}
			});
		}

		List<Bson> pipe;
		if (Check.isAssigned(seg.pipelineJson)) {
			pipe = jq.array(seg.pipelineJson);
		} else {
			pipe = jq.array();
		}

		Document doc = c(seg.collection, domain).aggregate(pipe).first();
		if (doc != null) {
			return Check.isAssignedThen(seg.returnField, doc::get).orElse(doc);
		} else {
			logger.warn("查询类型没有返回结果。");
			return null;
		}
	}

	private String format(ValueRuleSegment seg, Object value) {
		// 格式化
		if (Check.isAssigned(seg.format) && value != null) {
			value = Formatter.getString(value, seg.format);
		}

		// 转换为文本
		String text = value == null ? "" : value.toString();

		// 补位
		if (seg.length != null) {
			int count = text.length() - seg.length;
			if (count > 0) {
				text = "右".equals(seg.supplymentDir) ? text.substring(0, seg.length) : text.substring(count);// 右截取和左截取
			} else if (count < 0) {
				if (Check.isAssigned(seg.supplymentPlaceholder)) {
					String placeholder = seg.supplymentPlaceholder.substring(0, 1);
					for (int i = 0; i < -count; i++) {
						text = "右".equals(seg.supplymentDir) ? (text + placeholder) : (placeholder + text);
					}
				}
			}
		}

		// 大小写
		if ("大写".equals(seg.caser)) {
			text = text.toUpperCase();
		} else if ("小写".equals(seg.caser)) {
			text = text.toLowerCase();
		}

		return text;
	}

	public static void generate(ValueRule vr, Document doc) {
		String value = new DocumentValueGenerator(vr).getValue(doc);
		doc.append(vr.fieldName, value);		
	}

}
