package com.bizvisionsoft.service.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("workPackageProgress")
public class WorkPackageProgress {
	

	@Persistence
	public ObjectId _id;

	@Persistence
	public ObjectId package_id;

	public WorkPackageProgress setPackage_id(ObjectId package_id) {
		this.package_id = package_id;
		return this;
	}

	@Persistence
	private Date updateTime;

	public WorkPackageProgress setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ɹ�ʹ�õ��ֶΣ����ų�������ͼʹ��
	@Persistence
	@ReadValue
	private Double completeQty;

	@WriteValue("completeQty")
	private void setPlanQty(String _completeQty) {
		try {
			completeQty = Double.parseDouble(_completeQty);
		} catch (Exception e) {
			throw new RuntimeException("������Ϸ�������");
		}
	}

	@Persistence
	@ReadValue
	@WriteValue
	private Date time;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Label
	public String getText() {
		if (time != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			return df.format(time);
		}

		return "";
	}
}
