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

	@DataSet("目录导航/list")
	public List<? extends IFolder> listFolder(@MethodParam(MethodParam.CONDITION) BasicDBObject condition, // 查询条件
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) List<ObjectId> input) {// 输入根目录
		ArrayList<VaultFolder> path = createDemoData();

		return path;
	}

	private ArrayList<VaultFolder> createDemoData() {
		ArrayList<VaultFolder> path = new ArrayList<>();
		VaultFolder folder = new VaultFolder();
		folder.setDesc("TX0000中文计算0001");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0中文计算0000002");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000003");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00004");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000005");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00006");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000007");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0000中文计算0008");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00009");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000010");
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
		folder.setDesc("TX0000中文计算0014");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000015");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000016");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX0000中文计算0017");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000018");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00019");
		path.add(folder);
		return path;
	}

	@DataSet("目录导航/count")
	public long countFolder(@MethodParam(MethodParam.FILTER) BasicDBObject filter, // 过滤器
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) List<ObjectId> input) {// 输入根目录) {
		return createDemoData().size();
	}

}
