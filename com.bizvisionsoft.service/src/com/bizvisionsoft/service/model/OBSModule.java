package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.SelectionValidation;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

/**
 * 项目模板，用于创建和编辑
 * 
 * @author
 *
 */
@Strict
@PersistenceCollection("obsModule")
public class OBSModule {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 标识属性
	/**
	 * _id
	 */
	@SetValue
	@GetValue
	private ObjectId _id;

	/**
	 * 编号
	 */
	@ReadValue
	@Label(Label.ID_LABEL)
	@WriteValue
	@Persistence
	private String id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 描述属性
	/**
	 * 名称
	 */
	@ReadValue
	@WriteValue
	@Persistence
	@Label(Label.NAME_LABEL)
	private String name;

	/**
	 * 描述
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	/**
	 * 适用范围
	 */
	@Persistence
	private List<ObjectId> eps_id;

	/**
	 * 角色团队
	 */
	@SetValue
	@ReadValue
	private List<String> obsTeam;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@SelectionValidation("epsInfos")
	public boolean selectionEPSInfos(@MethodParam(MethodParam.OBJECT) Object eps) {
		// TODO 无法控制多选框
		return (eps instanceof EPS) && ((EPS) eps).countSubEPS() == 0
				&& (eps_id == null || !eps_id.contains(((EPS) eps).get_id()));
	}

	@WriteValue("epsInfos")
	public void setEPSInfos(List<EPS> epss) {
		eps_id = Check.isAssignedThen(epss, e -> Formatter.getList(e, EPS::get_id)).orElse(null);
	}

	@ReadValue("epsInfos")
	public List<EPS> getEPSInfos() {
		EPSService epsService = ServicesLoader.get(EPSService.class);
		return Optional.ofNullable(eps_id).orElse(new ArrayList<>()).stream().map(epsService::get)
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
