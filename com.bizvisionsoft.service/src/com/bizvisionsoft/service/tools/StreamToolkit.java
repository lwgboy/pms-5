package com.bizvisionsoft.service.tools;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.mongodb.BasicDBObject;

public class StreamToolkit {

	public static <T> Stream<T> appendCondition(Stream<T> stream, BasicDBObject condition) {
		stream = appendFilter(stream, (BasicDBObject) condition.get("filter"));
		stream = appendSort(stream, (BasicDBObject) condition.get("sort"));
		stream = appendSkip(stream, (Number) condition.get("skip"));
		stream = appendLimit(stream, (Number) condition.get("limit"));
		return stream;
	}

	public static <T> Stream<T> appendFilter(Stream<T> stream, BasicDBObject filter) {
		if (filter == null || filter.isEmpty())
			return stream;
		return stream.filter(o -> {
			if (o instanceof Map<?, ?>) {
				return filter((Map<?, ?>) o, filter);
			} else {
				BasicDBObject d = BsonTools.encodeBasicDBObject(o);
				return filter(d, filter);
			}
		});
	}

	private static boolean filter(Map<?, ?> data, BasicDBObject filter) {
		Iterator<Entry<String, Object>> iter = filter.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Object> itm = iter.next();
			String key = itm.getKey();
			Object value1 = data.get(key);
			Object value2 = itm.getValue();
			if (!match(value1, value2)) {
				return false;
			}
		}
		return true;
	}

	private static boolean match(Object value1, Object value2) {
		if (value2 instanceof String) {
			return value2.equals(value1);
		} else if (value2 instanceof Pattern) {
			if (value1 != null) {
				return ((Pattern) value2).matcher(value1.toString()).find();
			}
		}
		return false;
	}

	private static <T> Stream<T> appendLimit(Stream<T> stream, Number limit) {
		if (limit == null)
			return stream;
		return stream.limit(limit.longValue());
	}

	private static <T> Stream<T> appendSkip(Stream<T> stream, Number skip) {
		if (skip == null)
			return stream;
		return stream.skip(skip.longValue());
	}

	private static <T> Stream<T> appendSort(Stream<T> stream, BasicDBObject sort) {
		if (sort != null && !sort.isEmpty()) {
			Set<String> set = sort.keySet();
			return stream.sorted((o1, o2) -> {
				Map<?,?> d1 = Check.instanceOf(o1, Map.class).orElse(BsonTools.encodeBasicDBObject(o1));
				Map<?,?> d2 = Check.instanceOf(o2, Map.class).orElse(BsonTools.encodeBasicDBObject(o2));
				Iterator<String> iter = set.iterator();
				while (iter.hasNext()) {
					String k = iter.next();
					Object v = sort.get(k);
					int result = compare(set, k, v, d1, d2);
					if (result != 0)
						return result;
				}
				return 0;
			});
		} else {
			return stream;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compare(Set<String> set, String k, Object v, Map<?,?> d1, Map<?,?> d2) {
		Object v1 = d1.get(k);
		Object v2 = d2.get(k);
		if (v1 != null && v2 == null)
			return 1;
		if (v1 == null && v2 != null)
			return -1;
		if (v1 == null && v2 == null)
			return 0;

		int index;
		if (Boolean.FALSE.equals(v)) {
			index = -1;
		} else if (Boolean.TRUE.equals(v)) {
			index = 1;
		} else if (v instanceof Number) {
			if (((Number) v).doubleValue() > 0) {
				index = 1;
			} else if (((Number) v).doubleValue() < 0) {
				index = -1;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
		if (v1 instanceof Comparable<?> && v2 instanceof Comparable<?>) {
			String n1 = v1.getClass().getName();
			String n2 = v2.getClass().getName();
			if (n1.equals(n2)) {
				return index * ((Comparable) v1).compareTo(((Comparable) v2));
			}
		}
		return index * v1.toString().compareTo(v2.toString());
	}

}
