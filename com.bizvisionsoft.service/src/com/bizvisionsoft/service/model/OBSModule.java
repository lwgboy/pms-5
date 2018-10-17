package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

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
import com.bizvisionsoft.service.tools.Formatter;

/**
 * ��Ŀģ�壬���ڴ����ͱ༭
 * 
 * @author
 *
 */
@Strict
@PersistenceCollection("obsModule")
public class OBSModule {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��ʶ����
	/**
	 * _id
	 */
	@SetValue
	@GetValue
	private ObjectId _id;

	/**
	 * ���
	 */
	@ReadValue
	@Label(Label.ID_LABEL)
	@WriteValue
	@Persistence
	private String id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��������
	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	@Label(Label.NAME_LABEL)
	private String name;

	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	/**
	 * ���÷�Χ
	 */
	@Persistence
	private List<ObjectId> eps_id;

	/**
	 * ��ɫ�Ŷ�
	 */
	@SetValue
	private List<String> obsTeam;

	@ReadValue("obsTeam")
	private String getOBSTeam() {
		StringBuffer sb = new StringBuffer();
		if (obsTeam != null && obsTeam.size() > 0) {
			// TODO ����Warpper��ʾ
			// sb.append("<div style='cursor:pointer;display:inline-flex;width:
			// 100%;justify-content: space-between;'>");

			for (String teamString : obsTeam) {
				sb.append(teamString + " ");
			}
			// sb.append("</div>");
		}
		return sb.toString();
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@SelectionValidation("epsInfos")
	public boolean selectionEPSInfos(@MethodParam(MethodParam.OBJECT) Object eps) {
		// TODO �޷����ƶ�ѡ��
		return (eps instanceof EPS) && ((EPS) eps).countSubEPS() == 0
				&& (eps_id == null || !eps_id.contains(((EPS) eps).get_id()));
	}

	@WriteValue("epsInfos")
	public void setEPSInfos(List<EPS> epss) {
		if (epss == null || epss.isEmpty()) {
			eps_id = null;
		} else {
			eps_id = Formatter.getList(epss, EPS::get_id);
		}
	}

	@ReadValue("epsInfos")
	public List<EPS> getEPSInfos() {
		List<EPS> result = new ArrayList<EPS>();
		if (eps_id != null) {
			eps_id.forEach(item -> result.add(ServicesLoader.get(EPSService.class).get(item)));
		}
		return result;
	}
}
