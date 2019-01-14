package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ProblemCardRenderer {

	public static final String[] priorityText = new String[] { "最高", "高", "中", "低", "最低" };

	public static final String[] similarDegreeText = new String[] { "相同", "近似", "类似", "不同" };

	public static final String[] cftRoleText = new String[] { "组长", "设计", "工艺", "生产", "质量" };

	private static final CardTheme indigo = new CardTheme(CardTheme.INDIGO);

	private static final CardTheme deepGrey = new CardTheme(CardTheme.DEEP_GREY);

	private static final CardTheme red = new CardTheme(CardTheme.RED);

	public static Document renderProblem(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		// 【公共块】//
		appendProblemCommonInfo(doc, sb);

		// 【不同状态的内容块】
		if ("解决中".equals(doc.get("status"))) {
			appendProblemScheduleBar(doc, sb);
		} else if ("已创建".equals(doc.get("status"))) {
		} else if ("已关闭".equals(doc.get("status"))) {
			appendProblemCostInfo(doc, sb);
		} else if ("已取消".equals(doc.get("status"))) {
		}

		// 【指标表】
		Document chart = createProblemInstuctors(doc);
		if (chart != null)
			sb.append("<div name='" + doc.get("_id") + "' style='width:100%;height:120px;'></div>");
		else
			sb.append("<div style='width:100%;height:24px;'></div>");// 占位

		// 【按钮块】
		appendProblemButtons(doc, sb);

		// 【卡片背景】
		RenderTools.appendCardBg(sb);

		Document result = new Document("_id", doc.get("_id")).append("html", sb.toString());
		if (chart != null)
			result.append("charts", Arrays.asList(chart));
		return result;
	}

	private static void appendProblemCostInfo(Document doc, StringBuffer sb) {
		Optional.ofNullable((Document) doc.get("cost")).map(c -> c.getDouble("summary")).map(c -> Formatter.getString(c, "￥#,###.00"))
				.ifPresent(s -> RenderTools.appendLabelAndTextLine(sb, "损失：", s));
	}

	private static void appendProblemCommonInfo(Document doc, StringBuffer sb) {
		RenderTools.appendHeader(sb, indigo, doc.getString("name"), 36);

		// 问题照片
		// Document photoDoc = Optional.ofNullable(doc.get("d2ProblemPhoto"))
		// .map(d -> ((List<?>) d).isEmpty() ? null : (Document) ((List<?>)
		// d).get(((List<?>) d).size() - 1)).orElse(null);
		// if (photoDoc != null) {
		// sb.append("<img src='" + RenderTools.getFirstFileURL(photoDoc, "problemImg")
		// + "'
		// style='cursor:pointer;width:100%;height:auto;'onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i="
		// + photoDoc.get("_id") + "&f=problemImg\",function(json){layer.photos({photos:
		// json});});'" + "/>");
		// }

		RenderTools.appendLabelAndTextLine(sb, "类别：",
				Optional.ofNullable((Document) doc.get("classifyProblem")).map(d -> d.getString("path")).orElse(""));

		Check.isAssigned(doc.getString("custInfo"), s -> RenderTools.appendLabelAndTextLine(sb, "客户：", s));
		Check.isAssigned(doc.getString("partName"), s -> RenderTools.appendLabelAndTextLine(sb, "零件：", s));

		String pnum = Check.isAssignedThen(doc.getString("partNum"), s -> "S/N:" + s).orElse("");
		String prev = Check.isAssignedThen(doc.getString("partVer"), s -> " Rev:" + s).orElse("");
		String lotNum = Check.isAssignedThen(doc.getString("lotNum"), s -> s).orElse("");
		if (Check.isAssigned(pnum, prev, lotNum))
			RenderTools.appendLabelAndTextLine(sb, "批次：", pnum + prev + " Lot:" + lotNum);

		String by = Optional.ofNullable(doc.getString("issueBy")).orElse("");
		String on = Optional.ofNullable(doc.getDate("issueDate")).map(Formatter::getString).orElse("");
		if (Check.isAssigned(by, on))
			RenderTools.appendLabelAndTextLine(sb, "发起：", on + " " + by);

		// RenderTools.appendLabelAndTextLine(sb, "来源：",
		// doc.getString("initiatedFrom"));
	}

	private static Document createProblemInstuctors(Document doc) {
		Object _id = doc.get("_id");
		/////////////////////////////////////////////////////////////////
		// 指标
		// 1. 计算PCI(问题关键指数)
		int severityInd = Optional.ofNullable((Document) doc.get("severityInd")).map(d -> d.getInteger("index")).orElse(0);
		// int detectionInd = Optional.ofNullable((Document)
		// doc.get("detectionInd")).map(d -> d.getInteger("index")).orElse(0);
		// int freqInd = Optional.ofNullable((Document) doc.get("freqInd")).map(d ->
		// d.getInteger("index")).orElse(0);
		// 2. incidenceInd影响程度
		int incidenceInd = Optional.ofNullable((Document) doc.get("incidenceInd")).map(d -> d.getInteger("index")).orElse(0);
		// 3. lostInd损失程度
		int lostInd = Optional.ofNullable((Document) doc.get("lostInd")).map(d -> d.getInteger("index")).orElse(0);
		// 4. urgencyInd紧急程度
		// int urgencyInd = Optional.ofNullable((Document) doc.get("urgencyInd")).map(d
		// -> d.getInteger("index")).orElse(0);
		if (severityInd + incidenceInd + lostInd > 0) {
			Document chart = new Document();
			chart.append("renderTo", "" + _id);
			chart.append("option", new JQ("图表-通用-小型三联排仪表").set("name1", "严重度").set("value1", severityInd).set("name2", "影响程度")
					.set("value2", incidenceInd).set("name3", "损失程度").set("value3", lostInd).doc());
			return chart;
		}
		return null;
	}

	private static void appendProblemButtons(Document doc, StringBuffer sb) {
		// 添加【按钮】
		if ("解决中".equals(doc.get("status"))) {
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "打开问题T.O.P.S.主页", "open8D");
		} else if ("已创建".equals(doc.get("status"))) {
			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑问题指标", "editProblem");
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "启动问题解决程序", "kickoff");
		} else if ("已关闭".equals(doc.get("status"))) {
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "打开问题T.O.P.S.主页", "open8D");
		} else if ("已取消".equals(doc.get("status"))) {
			// TODO 查看问题定义？
		}
	}

	private static void appendProblemScheduleBar(Document doc, StringBuffer sb) {
		/////////////////////////////////////////////////////////////////
		// 进度栏
		// 【状态字段】icaConfirmed, pcaApproved,pcaValidated,pcaConfirmed
		String[] msgs = new String[] { "已确认临时控制措施有效。", //
				"已批准永久纠正措施的方案开始执行。", //
				"通过长期监控永久纠正措施能够长期有效。", //
				"通过实施和验证，永久纠正措施能够解决问题，达到预期目标。" //
		};
		String[] titles = new String[] { "ICA确认", "PCA批准", "PCA验证", "PCA确认" };
		String[] fields = new String[] { "icaConfirmed", "pcaApproved", "pcaValidated", "pcaConfirmed" };

		String label = "";
		int max = 0;
		for (int i = 0; i < fields.length; i++) {
			if ((Document) doc.get(fields[i]) != null) {
				max = Math.max(max, i);
			}
			label += MetaInfoWarpper.warpper(titles[i], msgs[i]);
		}

		sb.append("<div class='layui-progress layui-progress' style='margin:8px 8px 0px 8px;'>");
		sb.append("<div class='layui-progress-bar' style='width:" + 100 * (max + 1) / (1 + fields.length) + "%'></div>");
		sb.append("</div>");
		sb.append("<div class='label_caption brui_ly_hline brui_line_padding' style='color:#" + indigo.lightText + ";padding-top:0px;'>");
		sb.append(label);
		sb.append("</div>");

		// 【紧急应对】eraStarted,eraStopped
		Document eraStarted = (Document) doc.get("eraStarted");
		Document eraStopped = (Document) doc.get("eraStopped");
		if (eraStarted != null) {// 紧急应对已启用
			String text, msg;
			if (eraStopped != null) {
				msg = eraStopped.getString("userName") + Formatter.getString(eraStopped.getDate("date")) + "<br>已终止紧急应对措施";
				text = "<span class='layui-badge'>" + "ERA 已终止" + "</span>";
			} else {
				msg = eraStarted.getString("userName") + Formatter.getString(eraStarted.getDate("date")) + "<br>已启动紧急应对措施";
				text = "<span class='layui-badge layui-bg-blue'>" + "ERA 已启动" + "</span>";
			}
			text = MetaInfoWarpper.warpper(text, msg);
			RenderTools.appendText(sb, text, RenderTools.STYLE_1LINE);
		}
	}

	public static Document renderD0ERA(Document doc, String lang) {
		StringBuffer sb = renderAction(doc, red, null);
		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD1CFTMember(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		// 头像
		String name = doc.getString("name");
		String role = cftRoleText[Integer.parseInt(doc.getString("role"))];
		String mobile = Optional.ofNullable(doc.getString("mobile")).map(e -> "<a href='tel:" + e + "'>" + e + "</a>").orElse("");
		String position = Optional.ofNullable(doc.getString("position")).orElse("");
		String email = Optional.ofNullable(doc.getString("email")).map(e -> "<a href='mailto:" + e + "'>" + e + "</a>").orElse("");
		String dept = Optional.ofNullable(doc.getString("dept")).orElse("");
		String img = RenderTools.getUserHeadPicURL(doc, name);

		RenderTools.appendHeader(sb, indigo, "<div class='label_subhead'><div>" + role + "</div><div class='label_body1'>" + name + " "
				+ dept + " " + position + "</div></div>" + img, 48);

		RenderTools.appendIconTextLine(sb, RenderTools.IMG_URL_TEL, 16, mobile);

		RenderTools.appendIconTextLine(sb, RenderTools.IMG_URL_EMAIL, 16, email);

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除团队成员", "delete");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD25W2H(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		RenderTools.appendHeader(sb, red, "5W2H", 36);

		RenderTools.appendLabelAndMultiLine(sb, "What / 当前状况", "deep_orange", Optional.ofNullable(doc.getString("what")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "When / 发现时间", "deep_orange", Optional.ofNullable(doc.getString("when")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Where / 地点和位置", "deep_orange", Optional.ofNullable(doc.getString("where")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Who / 有关人员", "deep_orange", Optional.ofNullable(doc.getString("who")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Why / 原因推测", "deep_orange", Optional.ofNullable(doc.getString("why")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "How / 怎样发现的问题", "deep_orange", Optional.ofNullable(doc.getString("how")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "How many / 频度，数量", "deep_orange", Optional.ofNullable(doc.getString("howmany")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendButton(sb, "layui-icon-edit", 12, 12, "编辑问题描述", "editpd");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD2PhotoCard(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		sb.append("<img src='" + RenderTools.getFirstFileURL(doc, "problemImg")
				+ "' style='cursor:pointer;width:100%;height:auto;border-radius:4px 4px 0px 0px;' onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i="
				+ doc.get("_id") + "&f=problemImg\", function(json){layer.photos({photos: json});});'" + "/>");

		RenderTools.appendText(sb, doc.getString("problemImgDesc"), RenderTools.STYLE_3LINE);

		RenderTools.appendText(sb,
				Formatter.getString(doc.getDate("receiveDate")) + "/" + doc.getString("receiver") + " " + doc.getString("location"),
				RenderTools.STYLE_1LINE);

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除图片资料", "deletephoto");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", 240);
	}

	public static Document renderD3ICA(Document doc, String lang) {
		StringBuffer sb = renderAction(doc, indigo, null);
		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	private static StringBuffer renderAction(Document doc, CardTheme theme, String head) {
		boolean finished = doc.getBoolean("finish", false);
		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = priorityText[Integer.parseInt(doc.getString("priority"))];
		Date planStart = doc.getDate("planStart");
		Date planFinish = doc.getDate("planFinish");
		Date actualStart = doc.getDate("actualStart");
		Date actualFinish = doc.getDate("actualFinish");
		Document chargerData = (Document) doc.get("charger_meta");
		Document verification = (Document) doc.get("verification");

		String status;
		if (finished) {
			status = "<span class='layui-badge  layui-bg-green'>" + "已完成" + "</span>";
			theme = deepGrey;
		} else if (verification != null) {
			String style;
			status = verification.getString("title");
			if ("已验证".equals(status)) {
				style = "layui-badge  layui-bg-green";
				// theme = new CardTheme(CardTheme.TEAL);
			} else {
				style = "layui-badge";
				// theme = new CardTheme(CardTheme.RED);
			}
			String title = verification.getString("title");
			String comment = verification.getString("comment");
			String _date = Formatter.getString(verification.getDate("date"));
			Document user_meta = (Document) verification.get("user_meta");
			String name = user_meta.getString("name");
			String verifyInfo = comment + "<br>" + name + " " + _date;

			status = "<span class='" + style + "' style='cursor:pointer;' onclick='layer.alert(\"" + verifyInfo
					+ "\", {\"skin\": \"layui-layer-lan\",title:\"" + title + "\"})'>" + status + "</span>";
		} else {
			status = "<span class='layui-badge  layui-bg-blue'>" + "已创建" + "</span>";
		}

		StringBuffer sb = new StringBuffer();
		if (head == null) {
			sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
					+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text'>" + action + "</div>"//
					+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + priority + "</div>"
					+ "<div class='label_caption'>优先级</div></div>"//
					+ "</div>");//
		} else {
			sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
					+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text'>" + head + "</div>"//
					+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + priority + "</div>"
					+ "<div class='label_caption'>优先级</div></div>"//
					+ "</div>");//
			RenderTools.appendText(sb, action, RenderTools.STYLE_NLINE);
		}

		RenderTools.appendLabelAndTextLine(sb, "预期结果：", objective, 0);

		RenderTools.appendSchedule(sb, planStart, planFinish, actualStart, actualFinish);

		RenderTools.appendUserAndText(sb, chargerData, status);

		if (!doc.getBoolean("finish", false)) {
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8 + 16 + 8, 12, "完成", "finish");

			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8 + 16 + 8, 12, "编辑", "edit");

			RenderTools.appendButton(sb, "layui-icon-survey", 12 + 16 + 8, 12, "验证", "verify");

			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除", "delete");
		} else {
			RenderTools.appendButton(sb, "layui-icon-more", 12, 12, "详细", "read");
		}

		RenderTools.appendCardBg(sb);
		return sb;
	}

	public static Document renderD4(Document doc, String title, String rootCauseDesc, Document charger_meta, Date date, String lang) {

		StringBuffer sb = new StringBuffer();

		RenderTools.appendHeader(sb, red, title, 36);

		RenderTools.appendText(sb, rootCauseDesc, RenderTools.STYLE_NLINE);

		RenderTools.appendUserAndText(sb, charger_meta, Formatter.getString(date));

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD4CauseConsequence(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = 8 * 3;

		String name = doc.getString("name");
		String desc = Optional.ofNullable(doc.getString("description")).orElse("");
		int w = doc.getInteger("weight", 1);
		String p = Formatter.getPercentageFormatString(doc.getDouble("probability") / 100);

		sb.append("<div class='label_caption' style='display:flex;flex-direction:column;justify-content:space-around;color:white;'>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;margin-bottom:1px;border-radius:4px 0px 0px 0px;'>");
		sb.append("<div>" + w + "</div><div>权重</div></div>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;;border-radius:0px 0px 0px 4px;'>");
		sb.append("<div>" + p + "</div><div>概率</div></div>");
		sb.append("</div>");

		sb.append("<div style='width:0;flex-grow:1;padding:0px 4px;display:flex;flex-direction:column;justify-content:space-around;'>");
		sb.append("<div class='brui_text_line label_caption'>" + name
				+ "</div><div class='brui_card_text label_caption' style='height:32px;'>" + desc + "</div>");

		Optional.ofNullable((Document) doc.get("classifyCause")).map(c -> c.getString("path")).ifPresent(path -> {
			sb.append("<div class='brui_text_line label_caption'>");
			Arrays.asList(path.split("/"))
					.forEach(em -> sb.append("<span class='layui-bg-green layui-badge' style='margin-right:4px;'>" + em + "</span>"));
			sb.append("</div>");
		});
		sb.append("</div>");

		rowHeight += 82;

		sb.insert(0,
				"<div class='brui_card_trans' style='display:flex;background:#f9f9f9;height:" + (rowHeight - 2 * 8) + "px;margin:8px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderD5PCA(List<?> list, String title, Document charger, Date planStart, Date planFinish, String lang) {
		StringBuffer sb = new StringBuffer();
		renderListItemsCard(sb, list, title, charger, planStart, planFinish, null, null, indigo);
		return new Document("html", sb.toString());
	}

	private static void renderListItemsCard(StringBuffer sb, List<?> list, String title, Document charger, Date planStart, Date planFinish,
			Date actualStart, Date actualFinish, CardTheme theme) {
		RenderTools.appendHeader(sb, theme, title, 36);

		RenderTools.appendSchedule(sb, planStart, planFinish, actualStart, actualFinish);

		RenderTools.appendList(sb, list, indigo.lightText, o -> ((Document) o).getString("name"));

		RenderTools.appendUserAndText(sb, charger, null);

		RenderTools.appendCardBg(sb);
	}

	public static Document renderD6IVPCA(Document doc, String lang) {

		String head;
		if ("make".equals(doc.getString("actionType"))) {
			head = "实施/确认问题产生的纠正措施";
		} else {
			head = "实施/确认问题流出的纠正措施";
		}
		StringBuffer sb = renderAction(doc, indigo, head);
		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD7Similar(Document t, String lang) {
		StringBuffer sb = new StringBuffer();

		String label = similarDegreeText[Integer.parseInt(t.getString("degree"))];

		String type = t.getString("similar");

		int prob = t.getDouble("prob").intValue();

		sb.append("<div class='brui_card_head' style='height:48px;background:#" + indigo.headBgColor + ";color:#" + indigo.headFgColor
				+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text' style='flex-grow:1;'>相似情形：" + type + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + label + "</div>"
				+ "<div class='label_caption'>相似度</div></div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + prob
				+ "<span style='font-size:9px;'>%</span></div>" + "<div class='label_caption'>可能性</div></div>"//
				+ "</div>");//

		RenderTools.appendText(sb, t.getString("desc"), RenderTools.STYLE_NLINE);

		List<?> ids = (List<?>) t.get("id");
		if (Check.isAssigned(ids)) {
			RenderTools.appendText(sb, "识别相似情形：", RenderTools.STYLE_1LINE);

			RenderTools.appendList(sb, ids, indigo.lightText, o -> {
				Document d = (Document) o;
				return Optional.ofNullable(d.getString("id")).orElse("") + " " + Optional.ofNullable(d.getString("keyword")).orElse("");
			});

		}

		RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑相似物", "editSimilar");

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除相似物", "deleteSimilar");

		RenderTools.appendCardBg(sb);

		return new Document("html", sb.toString()).append("_id", t.get("_id")).append("type", "similar");
	}

	public static Document renderD7SPA(Document t, String lang) {
		StringBuffer sb = new StringBuffer();
		RenderTools.appendHeader(sb, indigo, "系统性预防措施", 36);

		RenderTools.appendText(sb, t.getString("action"), RenderTools.STYLE_NLINE);

		Document charger = (Document) t.get("charger_meta");

		Date date = t.getDate("date");

		boolean finished = t.getBoolean("finish", false);
		String status = finished ? ("<span class='layui-badge  layui-bg-blue'>" + "已完成" + "</span>")
				: ("<span class='layui-badge'>" + "未完成" + "</span>");

		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date) + " " + status);

		if (!finished) {
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8, 12, "关闭PCA", "finishSPA");

			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑系统性预防措施", "editSPA");

			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除系统性预防措施", "deleteSPA");
		} else {
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "查看系统性预防措施", "readSPA");
		}

		RenderTools.appendCardBg(sb);

		return new Document("html", sb.toString()).append("_id", t.get("_id"));
	}

	public static Document renderD8Exp(Document t, String lang) {
		StringBuffer sb = new StringBuffer();

		String firstFileURL = RenderTools.getFirstFileURL(t, "video");
		if (firstFileURL != null) {
			sb.append("<video style='border-radius:4px 4px 0px 0px;' width='100%' height='auto' controls preload='auto'>");
			sb.append("<source src='" + firstFileURL + "' type='video/mp4'>");
			sb.append("</video>");
		} else {
			RenderTools.appendHeader(sb, indigo, "经验教训总结", 36);
		}

		RenderTools.appendText(sb, t.getString("name"), RenderTools.STYLE_1LINE);

		RenderTools.appendText(sb, t.getString("abstract"), RenderTools.STYLE_3LINE);

		Document charger = (Document) t.get("charger_meta");
		Date date = t.getDate("date");
		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date));

		RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑经验总结", "editExp");

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除经验总结", "deleteExp");

		RenderTools.appendCardBg(sb);
		return new Document("html", sb.toString()).append("_id", t.get("_id"));
	}

}
