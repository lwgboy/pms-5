package com.bizvisionsoft.jz;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.pgxxgc.www.IF_Service.IF_Service;
import com.pgxxgc.www.IF_Service.IF_ServiceServiceLocator;

public class Activator {
	public static void main(String[] args) {
		try {
			String msgid = null;

			/**
			 * TC
			 */
			// 项目
			// msgid = "010100010000002";

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
			Element rootElement = getXMLElement(msgid, doc);
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

	private static Element getXMLElement(String msgid, Document doc) {
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

			Element projectNoElement = doc.createElement("ZWERKS");
			projectNoElement.setTextContent("1000");
			datarowElement.appendChild(projectNoElement);

			projectNoElement = doc.createElement("ZWEMPF");
			projectNoElement.setTextContent("231");
			datarowElement.appendChild(projectNoElement);

		} else if ("010100010000008".equals(msgid)) {
			// ERP 生产
			Element datarowElement = doc.createElement("datarow");
			rowsElement.appendChild(datarowElement);

			Element projectNoElement = doc.createElement("ZWERKS");
			projectNoElement.setTextContent("1000");
			datarowElement.appendChild(projectNoElement);

			projectNoElement = doc.createElement("ZWEMPF");
			projectNoElement.setTextContent("231");
			datarowElement.appendChild(projectNoElement);
		}

		return rootElement;

	}
}
