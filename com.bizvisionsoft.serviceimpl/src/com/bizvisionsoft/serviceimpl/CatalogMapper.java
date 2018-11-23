package com.bizvisionsoft.serviceimpl;

import org.bson.Document;

import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;

public class CatalogMapper {

	private static Catalog setType(Catalog c, Class<?> clas) {
		c.type = clas.getName();
		return c;
	}

	public static Catalog org(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("fullName");
		setType(c, Organization.class);
		c.icon = "img/org_c.svg";
		c.meta = doc;
		return c;
	}

	public static Catalog resourceType(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name");
		setType(c, ResourceType.class);
		c.icon = "img/resource_c.svg";
		c.meta = doc;
		return c;
	}

	public static Catalog user(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name");
		setType(c, User.class);
		c.icon = "img/user_c.svg";
		c.meta = doc;
		return c;
	}

	public static Catalog equipment(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name");
		setType(c, Equipment.class);
		c.icon = "img/equipment_c.svg";
		c.meta = doc;
		return c;
	}

	public static Catalog eps(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name")+" ["+doc.get("id")+"]";
		setType(c, EPS.class);
		c.icon = "img/eps_c.svg";
		c.meta = doc;
		return c;
	}
	
	public static Catalog project(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name");
		Check.isAssigned(doc.getString("id"),i->c.label+= " ["+i+"]");
		setType(c, Project.class);
		c.icon = "img/project_c.svg";
		c.meta = doc;
		return c;
	}


}
