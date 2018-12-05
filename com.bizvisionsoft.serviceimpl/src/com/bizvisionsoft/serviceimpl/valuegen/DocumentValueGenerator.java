package com.bizvisionsoft.serviceimpl.valuegen;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.mongocodex.tools.IValueGenerateService;
import com.bizvisionsoft.service.ValueRule;
import com.bizvisionsoft.service.ValueRuleSegment;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class DocumentValueGenerator extends BasicServiceImpl implements IValueGenerateService {

	public Logger logger = LoggerFactory.getLogger(getClass());

	private List<ValueRuleSegment> segments;

	public DocumentValueGenerator(ValueRule rule) {
		segments = rule.segments;
	}

	@Override
	public String getValue(Object input) {
		Map<ValueRuleSegment, String> stage = new HashMap<ValueRuleSegment, String>();
		return segments.stream().sorted((s1, s2) -> s1.executeSequance - s2.executeSequance)// 按执行顺序排序
				.map(s -> this.getSegmentValue(input, s, stage))// 计算值
				.sorted((s1, s2) -> s1.getKey().index - s2.getKey().index)// 按段号排序
				.reduce("", (s, e) -> s + e.getValue(), (s1, s2) -> s1);
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
			if (input != null) {
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
							value = AUtil.readValue(input, cName, fName, null);
						} catch (Exception e) {
							logger.error("读取字段值失败。", e);
						}
					}

				} else {
					logger.warn("字段值类型缺少name的定义，取值被忽略。");
				}
			}
		} else if ("JavaScript".equals(seg.type)) {// 类型5.脚本
			String script = seg.script;
			if (Check.isAssigned(script)) {
				ScriptEngine eng = new ScriptEngineManager().getEngineByName("nashorn");
				try {
					value = eng.eval(script);
					String func = seg.function;
					if (func != null && !func.isEmpty()) {
						value = ((Invocable) eng).invokeFunction(func, input);
					}
				} catch (ScriptException | NoSuchMethodException e) {
					logger.error("脚本执行失败。", e);
				}
			} else {
				logger.warn("脚本类型缺少脚本的定义，取值被忽略。");
			}
		} else if ("查询".equals(seg.type)) {// 类型6.查询
			if (Check.isAssigned(seg.query, seg.collection)) {
				JQ jq = new JQ(seg.query);
				if (seg.params != null) {
					seg.params.forEach(d -> {
						String[] _v = d.split("=");
						String[] _n = _v[1].split("/");
						String pName = _v[0].trim();
						String cName = _n[0].trim();
						String fName = _n[1].trim();
						if (Check.isAssigned(cName, fName, pName)) {
							try {
								Object pValue = AUtil.readValue(input, cName, fName, null);
								jq.set(pName, pValue);
							} catch (Exception e) {
							}
						}
					});
				}
				Document doc = c(seg.collection).aggregate(jq.array()).first();
				if (doc != null) {
					value = Check.isAssignedThen(seg.returnField, doc::get).orElse(doc);
				} else {
					logger.warn("查询类型没有返回结果。");
				}
			} else {
				logger.warn("查询类型缺少查询和集合名的定义，取值被忽略。");
			}
		} else if ("流水号".equals(seg.type)) {
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
					value = generateCode("globalCodeGen", key);
				}
			}
		}

		String text = format(seg, value);
		stage.put(seg, text);
		return new AbstractMap.SimpleEntry<ValueRuleSegment, String>(seg, text);
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

}
