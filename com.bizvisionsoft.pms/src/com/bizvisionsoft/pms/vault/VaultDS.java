package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;
import com.mongodb.BasicDBObject;

public class VaultDS {

	@DataSet("Ŀ¼����/list")
	public List<? extends IFolder> listFolder(@MethodParam(MethodParam.CONDITION) BasicDBObject condition, // ��ѯ����
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) List<ObjectId> input) {// �����Ŀ¼
		ArrayList<VaultFolder> path = createDemoData();

		return path;
	}

	private ArrayList<VaultFolder> createDemoData() {
		ArrayList<VaultFolder> path = new ArrayList<>();
		VaultFolder folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0001");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0���ļ���0000002");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000003");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00004");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000005");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00006");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000007");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0008");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00009");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000010");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000011");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000012");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000013");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0014");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000015");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000016");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0017");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000018");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00019");
		path.add(folder);
		return path;
	}

	@DataSet("Ŀ¼����/count")
	public long countFolder(@MethodParam(MethodParam.FILTER) BasicDBObject filter, // ������
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) List<ObjectId> input) {// �����Ŀ¼) {
		return createDemoData().size();
	}

}
