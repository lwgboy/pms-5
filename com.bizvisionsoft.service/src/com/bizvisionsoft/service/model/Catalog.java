package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * ͨ�õ����νṹ
 * 
 * @author gh
 *
 */
public class Catalog {

	public ObjectId _id;

	public String label;

	public String icon;

	public Document meta;

	public List<Catalog> subCatalog;
}
