package com.bizvisionsoft.jz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bizvision.esb.IFService;
import com.bizvision.esb.IFServiceService;
import com.pgxxgc.www.IF_Service.IF_Service;
import com.pgxxgc.www.IF_Service.IF_ServiceServiceLocator;

public class Distribution {

	public static IFService getProcessManager() throws Exception {
		IFServiceService ifServiceService = new IFServiceService();
		return ifServiceService.getIFService();
		// IF_ServiceServiceLocator if_ServiceServiceLocator = new
		// IF_ServiceServiceLocator();
		// return if_ServiceServiceLocator.getIF_Service();
	}

	public static List<PLMObject> getPLMWorkspace(String projectNumber) throws Exception {
		String msgid = "010100010000002";
		String ifService = null;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);

		Element datarowElement = doc.createElement("datarow");

		Element projectNoElement = doc.createElement("projectNo");
		projectNoElement.setTextContent(projectNumber);
		datarowElement.appendChild(projectNoElement);

		ifService = cellESB(msgid, doc, Arrays.asList(datarowElement));

		doc = builder.parse(new ByteArrayInputStream(ifService.getBytes("UTF-8")));
		List<PLMObject> result = new ArrayList<PLMObject>();
		if (doc != null) {
			NodeList nl = doc.getElementsByTagName("datarow");
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.hasChildNodes()) {
					PLMObject plmWorkspace = new PLMObject();
					plmWorkspace.type = PLMObject.TYPE_WORKSPACE;
					result.add(plmWorkspace);
					NodeList childNodes = item.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						plmWorkspace.addValue(item2.getNodeName(), item2.getTextContent());
					}
				}
			}
		}

		return result;
	}

	public static List<PLMObject> getPLMRootFolder(List<String> workspaceIds) throws Exception {
		String msgid = "010100010000003";
		String ifService = null;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);
		List<Element> datarows = new ArrayList<Element>();
		for (String workspaceId : workspaceIds) {
			Element datarowElement = doc.createElement("datarow");
			datarows.add(datarowElement);

			Element element = doc.createElement("workspaceId");
			element.setTextContent(workspaceId);
			datarowElement.appendChild(element);
		}

		ifService = cellESB(msgid, doc, datarows);

		doc = builder.parse(new ByteArrayInputStream(ifService.getBytes("UTF-8")));
		List<PLMObject> result = new ArrayList<PLMObject>();
		if (doc != null) {
			NodeList nl = doc.getElementsByTagName("datarow");
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.hasChildNodes()) {
					PLMObject plmWorkspace = new PLMObject();
					plmWorkspace.type = PLMObject.TYPE_FOLDER;
					result.add(plmWorkspace);
					NodeList childNodes = item.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						plmWorkspace.addValue(item2.getNodeName(), item2.getTextContent());
					}
				}
			}
		}

		return result;
	}

	public static List<PLMObject> getPLMFolder(List<String> folderIds) throws Exception {
		String msgid = "010100010000004";
		String ifService = null;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);
		List<Element> datarows = new ArrayList<Element>();
		for (String folderId : folderIds) {
			Element datarowElement = doc.createElement("datarow");
			datarows.add(datarowElement);

			Element element = doc.createElement("folderId");
			element.setTextContent(folderId);
			datarowElement.appendChild(element);
		}

		ifService = cellESB(msgid, doc, datarows);

		doc = builder.parse(new ByteArrayInputStream(ifService.getBytes("UTF-8")));
		List<PLMObject> result = new ArrayList<PLMObject>();
		if (doc != null) {
			NodeList nl = doc.getElementsByTagName("datarow");
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.hasChildNodes()) {
					PLMObject plmWorkspace = new PLMObject();
					result.add(plmWorkspace);
					NodeList childNodes = item.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						plmWorkspace.addValue(item2.getNodeName(), item2.getTextContent());
						if ("type".equals(item2.getNodeName()) && "folder".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_FOLDER;
						} else if ("type".equals(item2.getNodeName()) && "document".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_DOCUMENT;
						} else if ("type".equals(item2.getNodeName()) && "part".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_PART;
						} else if ("type".equals(item2.getNodeName()) && "epm".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_EPM;
						}
					}
				}
			}
		}

		return result;
	}

	public static List<PLMObject> getPLMObjectInfo(List<String> objectIds) throws Exception {
		String msgid = "010100010000005";
		String ifService = null;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);
		List<Element> datarows = new ArrayList<Element>();
		for (String folderId : objectIds) {
			Element datarowElement = doc.createElement("datarow");
			datarows.add(datarowElement);

			Element element = doc.createElement("objectId");
			element.setTextContent(folderId);
			datarowElement.appendChild(element);
		}

		ifService = cellESB(msgid, doc, datarows);

		doc = builder.parse(new ByteArrayInputStream(ifService.getBytes("UTF-8")));
		List<PLMObject> result = new ArrayList<PLMObject>();
		if (doc != null) {
			NodeList nl = doc.getElementsByTagName("datarow");
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.hasChildNodes()) {
					PLMObject plmWorkspace = new PLMObject();
					result.add(plmWorkspace);
					NodeList childNodes = item.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						plmWorkspace.addValue(item2.getNodeName(), item2.getTextContent());
						if ("type".equals(item2.getNodeName()) && "folder".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_FOLDER;
						} else if ("type".equals(item2.getNodeName()) && "document".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_DOCUMENT;
						} else if ("type".equals(item2.getNodeName()) && "part".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_PART;
						} else if ("type".equals(item2.getNodeName()) && "epm".equals(item2.getTextContent())) {
							plmWorkspace.type = PLMObject.TYPE_EPM;
						}
					}
				}
			}
		}

		return result;
	}

	public static List<PLMObject> getPLMObjectProcess(List<String> objectIds) throws Exception {
		String msgid = "010100010000006";
		String ifService = null;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);
		List<Element> datarows = new ArrayList<Element>();
		for (String objectId : objectIds) {
			Element datarowElement = doc.createElement("datarow");
			datarows.add(datarowElement);

			Element element = doc.createElement("objectId");
			element.setTextContent(objectId);
			datarowElement.appendChild(element);
		}

		ifService = cellESB(msgid, doc, datarows);

		doc = builder.parse(new ByteArrayInputStream(ifService.getBytes("UTF-8")));
		List<PLMObject> result = new ArrayList<PLMObject>();
		if (doc != null) {
			NodeList nl = doc.getElementsByTagName("datarow");
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.hasChildNodes()) {
					PLMObject plmWorkspace = new PLMObject();
					result.add(plmWorkspace);
					NodeList childNodes = item.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						plmWorkspace.addValue(item2.getNodeName(), item2.getTextContent());
						plmWorkspace.type = PLMObject.TYPE_PROCESS;
					}
				}
			}
		}

		return result;
	}

	public static List<PLMObject> getERPPurchase(List<String> trackWorkOrders) throws Exception {
		String msgid = "010100010000007";
		String ifService = null;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);
		List<Element> datarows = new ArrayList<Element>();
		for (String trackWorkOrder : trackWorkOrders) {
			Element datarowElement = doc.createElement("datarow");
			datarows.add(datarowElement);

			Element element = doc.createElement("materialNo");
			element.setTextContent("1000");
			datarowElement.appendChild(element);

			element = doc.createElement("ZABLAD");
			element.setTextContent(trackWorkOrder);
			datarowElement.appendChild(element);
		}

		ifService = cellESB(msgid, doc, datarows);

		doc = builder.parse(new ByteArrayInputStream(ifService.getBytes("UTF-8")));
		List<PLMObject> result = new ArrayList<PLMObject>();
		if (doc != null) {
			NodeList node = doc.getElementsByTagName("IT_OUT");
			NodeList nl = node.item(0).getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.hasChildNodes()) {
					PLMObject plmWorkspace = new PLMObject();
					result.add(plmWorkspace);
					NodeList childNodes = item.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						plmWorkspace.addValue(item2.getNodeName(), item2.getTextContent());
						plmWorkspace.type = PLMObject.TYPE_PURCHASE;
					}
				}
			}
		}

		return result;
	}

	public static List<PLMObject> getERPProduction(Map<String, String> productions) throws Exception {
		String msgid = "010100010000008";
		String ifService = null;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);
		List<Element> datarows = new ArrayList<Element>();
		for (String production : productions.keySet()) {
			Element datarowElement = doc.createElement("datarow");
			datarows.add(datarowElement);

			Element element = doc.createElement("materialNo");
			element.setTextContent("1000");
			datarowElement.appendChild(element);

			element = doc.createElement("ZABLAD");
			element.setTextContent(production);
			datarowElement.appendChild(element);

			element = doc.createElement("ZMATNR");
			element.setTextContent(productions.get(production));
			datarowElement.appendChild(element);
		}

		ifService = cellESB(msgid, doc, datarows);

		doc = builder.parse(new ByteArrayInputStream(ifService.getBytes("UTF-8")));
		List<PLMObject> result = new ArrayList<PLMObject>();
		if (doc != null) {
			NodeList node = doc.getElementsByTagName("IT_OUT");
			NodeList nl = node.item(0).getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.hasChildNodes()) {
					PLMObject plmWorkspace = new PLMObject();
					result.add(plmWorkspace);
					NodeList childNodes = item.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						plmWorkspace.addValue(item2.getNodeName(), item2.getTextContent());
						plmWorkspace.type = PLMObject.TYPE_PRODUCTION;
					}
				}
			}
		}

		return result;
	}

	private static String cellESB(String msgid, Document doc, List<Element> dataRows) throws Exception {
//		IFService processManager = getProcessManager();
//		Element rootElement = doc.createElement("message");
//		rootElement.setAttribute("msgid", msgid);
//		doc.appendChild(rootElement);
//
//		Element datasetElement = doc.createElement("dataset");
//		rootElement.appendChild(datasetElement);
//
//		Element rowsElement = doc.createElement("rows");
//		datasetElement.appendChild(rowsElement);
//
//		for (Element dataRow : dataRows) {
//			rowsElement.appendChild(dataRow);
//		}
//
//		TransformerFactory transFactory = TransformerFactory.newInstance();
//		Transformer transFormer = transFactory.newTransformer();
//		transFormer.setOutputProperty("encoding", "gbk");
//
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//		DOMSource domSource = new DOMSource(doc);
//		// 设置输入源
//		StreamResult xmlResult = new StreamResult(out);
//		// 输出xml文件
//		transFormer.transform(domSource, xmlResult);
//		return processManager.ifService(msgid, out.toString());

		// return processManager.IFService(msgid, out.toString());
		 return getTestString(msgid);
	}

	private static String getTestString(String msgid) {
		if (msgid.endsWith("2"))
			return "<?xml version='1.0' encoding='utf-8'?>"
					+ "<message targetid='' sendtime='' sn='' msgtype='' resourcename='' resourceid='' msgid=''>"
					+ "<dataset><rows><datarow><workspaceId>bbbb</workspaceId><name>a</name></datarow></rows></dataset></message>";
		else if (msgid.endsWith("3"))
			return "<?xml version='1.0' encoding='utf-8'?>"
					+ "<message targetid='' sendtime='' sn='' msgtype='' resourcename='' resourceid='' msgid=''>"
					+ "<dataset><rows>"
					+ "<datarow><workspaceId>bbbb</workspaceId><id>1111</id><name>b1</name></datarow>"
					+ "<datarow><workspaceId>bbbb</workspaceId><id>2222</id><name>b2</name></datarow>"
					+ "</rows></dataset></message>";
		else if (msgid.endsWith("4"))
			return "<?xml version='1.0' encoding='utf-8'?>"
					+ "<message targetid='' sendtime='' sn='' msgtype='' resourcename='' resourceid='' msgid=''>"
					+ "<dataset><rows>"

					+ "<datarow><id>c1</id><name>c1</name><parentFolderId>1111</parentFolderId><type>folder</type><security></security>"
					+ "<stage></stage><majorVerNo></majorVerNo><status></status><createdBy></createdBy><createDate></createDate></datarow>"

					+ "<datarow><id>c2</id><name>c2</name><parentFolderId>1111</parentFolderId><type>document</type><security></security>"
					+ "<stage></stage><majorVerNo></majorVerNo><status></status><createdBy></createdBy><createDate></createDate></datarow>"

					+ "</rows></dataset></message>";
		else if (msgid.endsWith("5"))
			return "<?xml version='1.0' encoding='utf-8'?>"
					+ "<message targetid='' sendtime='' sn='' msgtype='' resourcename='' resourceid='' msgid=''>"
					+ "<dataset><rows>"

					+ "<datarow><id>c2</id><name>c2</name><parentFolderId>1111</parentFolderId><type>document</type><security></security>"
					+ "<stage></stage><majorVerNo></majorVerNo><status></status><createdBy></createdBy><createDate></createDate></datarow>"

					+ "</rows></dataset></message>";
		else if (msgid.endsWith("6"))
			return "<?xml version='1.0' encoding='utf-8'?>"
					+ "<message targetid='' sendtime='' sn='' msgtype='' resourcename='' resourceid='' msgid=''>"
					+ "<dataset><rows>"

					+ "<datarow><objectNo>c2</objectNo><processName>aaaa</processName><processId>aaa</processId><startDate></startDate>"
					+ "<charger>aaa</charger><endDate></endDate><name>校核</name><id></id><remark>批准</remark></datarow>"

					+ "</rows></dataset></message>";
		else if (msgid.endsWith("7"))
			return "<?xml version='1.0' encoding='utf-8'?><message targetid='' sendtime='' sn='' msgtype='' resourcename='' resourceid='' msgid=''><TABLES>"
					+ "<IT_IN><item><ZWERKS>1000</ZWERKS><ZWEMPF></ZWEMPF><ZABLAD></ZABLAD><ZMATNR></ZMATNR></item></IT_IN>"
					+ "<IT_OUT><item>"

					+ "<WERKS></WERKS><MATNR></MATNR><MAKTX></MAKTX><EBELN></EBELN><EBELP></EBELP><MEINS></MEINS>"
					+ "<WEMPF></WEMPF><AUFNR></AUFNR><ERDAT></ERDAT><DISPO></DISPO><ABLAD></ABLAD><BDMNG></BDMNG>"
					+ "<ZMENG></ZMENG><ZMENG2></ZMENG2><ZMENG3></ZMENG3>"

					+ "</item></IT_OUT></TABLES></message>";
		else if (msgid.endsWith("8"))
			return "<?xml version='1.0' encoding='utf-8'?><message targetid='' sendtime='' sn='' msgtype='' resourcename='' resourceid='' msgid=''><TABLES>"
					+ "<IT_IN><item><ZWERKS>1000</ZWERKS><ZWEMPF></ZWEMPF><ZABLAD></ZABLAD><ZMATNR></ZMATNR></item></IT_IN>"
					+ "<IT_OUT><item>"

					+ "<ZNUM></ZNUM><WERKS></WERKS><WEMPF></WEMPF><ABLAD></ABLAD><AUFNR></AUFNR>"
					+ "<KTEXT></KTEXT><MATNR></MATNR><MAKTX></MAKTX><BDMNG></BDMNG><ENMNG></ENMNG>"
					+ "<ZNUM1></ZNUM1><SORTF></SORTF><TXT_FEVOR></TXT_FEVOR><DSNAM></DSNAM><ZSTAT></ZSTAT>"

					+ "</item></IT_OUT></TABLES></message>";
		else
			return "";
	}

	public static void main(String[] args) {
		try {
			String msgid = null;

			/**
			 * TC
			 */
			// 项目
			msgid = "010100010000002";

			// 根目录
			// msgid = "010100010000003";

			// 目录下的对象
			// msgid = "010100010000004";

			// 对象属性
			// msgid = "010100010000005";

			// 对象流程
			// msgid = "010100010000006";

			/**
			 * ERP
			 */
			// 采购
			// msgid = "010100010000007";

			// 生产
			// msgid = "010100010000008";

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			doc.setXmlVersion("1.0");
			doc.setXmlStandalone(true);
			Element rootElement = mainXMLElement(msgid, doc);
			doc.appendChild(rootElement);

			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transFormer = transFactory.newTransformer();
			transFormer.setOutputProperty("encoding", "gbk");

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			DOMSource domSource = new DOMSource(doc);
			// 设置输入源
			StreamResult xmlResult = new StreamResult(out);
			// 输出xml文件
			transFormer.transform(domSource, xmlResult);

			// 测试文件输出的路径
			System.out.println(out.toString());

			IF_ServiceServiceLocator if_ServiceServiceLocator = new IF_ServiceServiceLocator();
			IF_Service if_Service = if_ServiceServiceLocator.getIF_Service();
			String ifService = null;
			ifService = if_Service.IFService(msgid, out.toString());
			System.out.println(ifService);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Element mainXMLElement(String msgid, Document doc) {
		Element rootElement = doc.createElement("message");
		rootElement.setAttribute("msgid", msgid);

		Element datasetElement = doc.createElement("dataset");
		rootElement.appendChild(datasetElement);

		Element rowsElement = doc.createElement("rows");
		datasetElement.appendChild(rowsElement);
		if ("010100010000002".equals(msgid)) {
			// 项目
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("projectNo");
			projectNoElement.setTextContent("9012");
			datarowElement.appendChild(projectNoElement);
		} else if ("010100010000003".equals(msgid)) {
			// TC 根目录
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("workspaceId");
			projectNoElement.setTextContent("U5dlY6VAqLpIyD");
			datarowElement.appendChild(projectNoElement);
		} else if ("010100010000004".equals(msgid)) {
			// TC 目录下的对象
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("folderId");
			projectNoElement.setTextContent("zLVlZcQcqLpIyD");
			datarowElement.appendChild(projectNoElement);
		} else if ("010100010000005".equals(msgid)) {
			// TC 对象属性
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("objectId");
			projectNoElement.setTextContent("9012-783-KFA-001|A");
			datarowElement.appendChild(projectNoElement);

			datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);
			projectNoElement = doc.createElement("objectId");
			projectNoElement.setTextContent("8930-783-KFA-001|A");
			datarowElement.appendChild(projectNoElement);

		} else if ("010100010000006".equals(msgid)) {
			// TC 对象流程
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("objectId");
			projectNoElement.setTextContent("JZ2.930.5026|A");
			datarowElement.appendChild(projectNoElement);

			datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			projectNoElement = doc.createElement("objectId");
			projectNoElement.setTextContent("JZ1.204.5016|A");
			datarowElement.appendChild(projectNoElement);
		} else if ("010100010000007".equals(msgid)) {
			// ERP 采购
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("materialNo");
			projectNoElement.setTextContent("1000");
			datarowElement.appendChild(projectNoElement);

			projectNoElement = doc.createElement("id");
			projectNoElement.setTextContent("231");
			datarowElement.appendChild(projectNoElement);

		} else if ("010100010000008".equals(msgid)) {
			// ERP 生产
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("materialNo");
			projectNoElement.setTextContent("1000");
			datarowElement.appendChild(projectNoElement);

			projectNoElement = doc.createElement("id");
			projectNoElement.setTextContent("231");
			datarowElement.appendChild(projectNoElement);
		}

		return rootElement;

	}
}
