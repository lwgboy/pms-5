package com.bizvisionsoft.service.tools;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.service.model.RemoteFile;

public class Formatter {

	public static Logger logger = LoggerFactory.getLogger(Formatter.class);

	private static char[] array = "0123456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

	public static final String DATE_FORMAT_JS_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

	public static final String DATE_FORMAT_DATE = "yyyy-MM-dd";

	public static final String DATE_FORMAT_TIME = "HH:mm";

	public static final String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

	public static final String MONEY_NUMBER_FORMAT = "#,##0.0";

	/**
	 * ʮ����ת�����������
	 * 
	 * @param number
	 * @param N
	 * @return
	 */
	public static String dec_n(long number, int N) {
		Long rest = number;
		Stack<Character> stack = new Stack<Character>();
		StringBuilder result = new StringBuilder(0);
		while (rest != 0) {
			stack.add(array[new Long((rest % N)).intValue()]);
			rest = rest / N;
		}
		for (; !stack.isEmpty();) {
			result.append(stack.pop());
		}
		return result.length() == 0 ? "0" : result.toString();
	}

	/**
	 * JS �����ַ���ת������
	 * 
	 * @param str
	 * @return
	 */
	public static Date getDatefromJS(String str) {
		if (str == null)
			return null;
		String _str = str.replace("Z", " UTC");
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_JS_FULL);
		try {
			return format.parse(_str);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * �ַ���תint
	 * 
	 * @param text
	 * @param errMsg
	 *            ����ת��ʱ����ʾ
	 * @return
	 */
	public static int getInt(String text, String errMsg) {
		if (Check.isNotAssigned(text)) {
			return 0;
		}
		try {
			return Integer.parseInt(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(errMsg);
		}
	}

	public static int getInt(String text) {
		return getInt(text, "���ǺϷ���ֵ");
	}

	public static int getIntValue(Object value) {
		if (value == null)
			return 0;
		if (value instanceof String) {
			return getInt((String) value);
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		throw new RuntimeException(value + " ���ǺϷ���int����");
	}

	public static double getDoubleValue(Object value) {
		if (value == null)
			return 0;
		if (value instanceof String) {
			return getInt((String) value);
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		throw new RuntimeException(value + " ���ǺϷ���double����");
	}

	/**
	 * �ַ���תdouble
	 * 
	 * @param text
	 * @param errMsg
	 *            ����ת��ʱ����ʾ
	 * @return
	 */
	public static double getDouble(String text, String errMsg) {
		if (Check.isNotAssigned(text)) {
			return 0d;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(errMsg);
		}
	}

	public static double getDouble(String text) {
		return getDouble(text, "���ǺϷ���ֵ");
	}

	public static String getString(Object value) {
		return getString(value, null, null);
	}

	public static String getPercentageFormatString(Object value) {
		return getString(value, "#0.0%", null);
	}

	public static String getMoneyFormatString(Double budget) {
		if (budget == null || budget == 0d) {
			return "";
		}
		return Formatter.getString(budget, MONEY_NUMBER_FORMAT);
	}

	public static String getString(Object value, String format) {
		return getString(value, format, null);
	}

	public static String getString(Object value, String format, Locale locale) {
		return getString(value, format, "", locale);
	}

	public static String getString(Object value, String format, String defaultValue, Locale locale) {
		String text;
		if (value instanceof Date) {
			String sdf = Check.isNotAssigned(format) ? DATE_FORMAT_DATE : format;
			return Optional.ofNullable(locale).map(l -> new SimpleDateFormat(sdf, l)).orElse(new SimpleDateFormat(sdf)).format(value);
		} else if (value instanceof Integer || value instanceof Long || value instanceof Short) {
			text = Optional.ofNullable(format)//
					.map(f -> {
						DecimalFormat df = new DecimalFormat(f);
						df.setRoundingMode(RoundingMode.HALF_UP);
						return df.format(value);
					}).orElse(value.toString());
		} else if (value instanceof Float || value instanceof Double) {
			DecimalFormat df = new DecimalFormat(Optional.ofNullable(format).orElse("0.0"));
			df.setRoundingMode(RoundingMode.HALF_UP);
			return df.format(value);
		} else if (value instanceof Boolean) {
			text = (boolean) value ? "��" : "��";
		} else if (value instanceof String) {
			text = (String) value;
		} else if (value instanceof List<?>) {
			text = "";
			for (int i = 0; i < ((List<?>) value).size(); i++) {
				if (i != 0) {
					text += ", ";
				}
				text += getString(((List<?>) value).get(i), format, locale);
			}
		} else if (value instanceof RemoteFile) {
			text = ((RemoteFile) value).name;
		} else if (value instanceof Object) {
			text = Optional.ofNullable(AUtil.readLabel(value)).orElse("");
		} else {
			text = defaultValue;
		}

		return text;
	}

	public static <T, R> List<R> getList(List<T> source, Function<T, R> func) {
		ArrayList<R> result = new ArrayList<R>();
		source.forEach(item -> result.add(func.apply(item)));
		return result;
	}

	public static <T, R> List<R> getList(T[] source, Function<T, R> func) {
		return getList(Arrays.asList(source), func);
	}

	/**
	 * 
	 * @param <T>
	 * @param source
	 *            Ҫ�ָ������
	 * @param subSize
	 *            �ָ�Ŀ��С
	 * @return
	 *
	 */
	public static <T> List<List<T>> getSplitedList(List<T> source, int subSize) {
		List<List<T>> subAryList = new ArrayList<List<T>>();
		int count = subSize == 0 ? 0 : (source.size() % subSize == 0 ? source.size() / subSize : source.size() / subSize + 1);
		for (int i = 0; i < count; i++) {
			int index = i * subSize;
			List<T> list = new ArrayList<T>();
			int j = 0;
			while (j < subSize && index < source.size()) {
				list.add(source.get(index++));
				j++;
			}
			subAryList.add(list);
		}

		return subAryList;
	}

	// i, u, v��������ĸ, ����ǰ�����ĸ

	private static char[] alphatable = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',

			'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	// private static char[] alphatable = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
	// 'h', 'i',
	//
	// 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
	// 'x', 'y', 'z' };

	// ��ʼ��
	private static int[] alphatable_code = { 45217, 45253, 45761, 46318, 46826, 47010, 47297, 47614, 47614, 48119, 49062, 49324, 49896,
			50371, 50614, 50622, 50906, 51387, 51446, 52218, 52218, 52218, 52698, 52980, 53689, 54481, 55289 };

	/**
	 * ����һ���������ֵ��ַ�������һ������ƴ������ĸ���ַ���
	 * 
	 * @param String
	 *            SourceStr ����һ�����ֵ��ַ���
	 */
	public static String getAlphaString(String src) {
		if (src == null) {
			return "";
		}
		String result = ""; //$NON-NLS-1$
		int i;
		try {
			for (i = 0; i < src.length(); i++) {
				result += char_alpha(src.charAt(i));
			}
		} catch (Exception e) {
			result = ""; //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * ������,�����ַ�,�õ�������ĸ, Ӣ����ĸ���ض�Ӧ����ĸ �����Ǽ��庺�ַ��� '0'
	 * 
	 * @param char
	 *            ch ����ƴ������ĸ���ַ�
	 */
	private static char char_alpha(char ch) {

		if (ch >= 'a' && ch <= 'z')
			// return (char) (ch - 'a' + 'A');
			return ch;
		if (ch >= 'A' && ch <= 'Z')
			return ch;
		if (ch >= '0' && ch <= '9')
			return ch;

		int gb = getCodeValue(ch, "GB2312"); //$NON-NLS-1$
		if (gb < alphatable_code[0])
			return '0';

		int i;
		for (i = 0; i < 26; ++i) {
			if (alphaCodeMatch(i, gb))
				break;
		}

		if (i >= 26)
			return ' ';
		else
			return alphatable[i];
	}

	/**
	 * �ж��ַ��Ƿ���table�����е��ַ���ƥ��
	 * 
	 * @param i
	 *            table�����е�λ��
	 * @param gb
	 *            ���ı���
	 * @return
	 */
	private static boolean alphaCodeMatch(int i, int gb) {

		if (gb < alphatable_code[i])
			return false;

		int j = i + 1;

		// ��ĸZʹ����������ǩ
		while (j < 26 && (alphatable_code[j] == alphatable_code[i]))
			++j;

		if (j == 26)
			return gb <= alphatable_code[j];
		else
			return gb < alphatable_code[j];

	}

	/**
	 * ȡ�����ֵı���
	 * 
	 * @param char
	 *            ch ����ƴ������ĸ���ַ�
	 */
	private static int getCodeValue(char ch, String charsetName) {

		String str = new String();
		str += ch;
		try {
			byte[] bytes = str.getBytes(charsetName);
			if (bytes.length < 2)
				return 0;
			return (bytes[0] << 8 & 0xff00) + (bytes[1] & 0xff);
		} catch (Exception e) {
			return 0;
		}
	}

	public static String getFriendlyTimeDuration(long diff) {
		long day = diff / (24 * 60 * 60 * 1000);
		long hour = (diff / (60 * 60 * 1000) - day * 24);
		long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

		String result = "";
		if (day != 0)
			result += day + "�� ";
		if (hour != 0)
			result += hour + "Сʱ ";
		if (min != 0)
			result += min + "���� ";
		if (sec != 0)
			result += sec + "��";
		return result;
	}

	public static String toHtml(String text) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; text != null && i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'')
				out.append("&#039;");
			else if (c == '\"')
				out.append("&#034;");
			else if (c == '<')
				out.append("&lt;");
			else if (c == '>')
				out.append("&gt;");
			else if (c == '&')
				out.append("&amp;");
			else if (c == ' ')
				out.append("&nbsp;");
			else if (c == '\n')
				out.append("<br/>");
			else
				out.append(c);
		}
		return out.toString();
	}

	public static Date getStartOfDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getEndOfDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.DATE, 1);
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime();
	}

	public static Date getStartOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getEndOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime();
	}

	public static Date getStartOfYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getEndOfYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.YEAR, 1);
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime();
	}

	public static List<Double> toList(double... ds) {
		List<Double> result = new ArrayList<>();
		for (int i = 0; i < ds.length; i++) {
			result.add(ds[i]);
		}
		return result;
	}

	public static double[] toArray(List<Double> list) {
		double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = Optional.ofNullable(list.get(i)).orElse(0d);
		}
		return result;
	}

	/**
	 * ɾ��Html��ǩ
	 * 
	 * @param input
	 * @return
	 */
	public static String removeHtmlTag(String input) {
		if (input == null)
			return "";

		try {

			// ����script��������ʽ{��<script[^>]*?>[\\s\\S]*?<\\/script>
			String reg = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
			String result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(input).replaceAll(""); // ����script��ǩ

			// ����style��������ʽ{��<style[^>]*?>[\\s\\S]*?<\\/style>
			reg = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
			result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(""); // ����style��ǩ

			// ����HTML��ǩ��������ʽ
			reg = "<[^>]+>";
			result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(""); // ����html��ǩ

			// ����һЩ�����ַ���������ʽ �磺&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			reg = "\\&[a-zA-Z]{1,10};";
			result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(""); // ���������ǩ

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
