package com.bizvisionsoft.service.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadOptions;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.tools.Check;
import com.mongodb.BasicDBObject;

@PersistenceCollection("obs")
public class OBSItem implements JsonExternalizable {

	@Exclude
	public static final String ID_PM = "PM";

	@Exclude
	public static final String NAME_PM = "��Ŀ����";

	@Exclude
	public static final String ID_CHARGER = "WM";

	@Exclude
	public static final String NAME_CHARGER = "������";

	@Override
	@Label
	@ReadValue("��Ŀ�Ŷ�/label")
	public String toString() {
		String txt = "";
		if (name != null && !name.isEmpty())
			txt += name;

		if (roleName != null && !roleName.isEmpty())
			txt += " " + roleName;

		if (roleId != null && !roleId.isEmpty())
			txt += " [" + roleId + "]";

		if (managerInfo != null && !managerInfo.isEmpty())
			txt += " (" + managerInfo + ")";

		return txt;
	}

	@Behavior({ "��ӽ�ɫ", "�����Ŷ�", "�༭", "ָ��������" })
	public boolean behaviorAddItem() {
		return true;
	}

	@Behavior({ "ɾ��" })
	public boolean behaviorEditOrDeleteItem() {
		return !scopeRoot;
	}

	@Behavior({ "��Ա" })
	public boolean behaviorHasMember() {
		return !isRole;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private final String typeName = "��Ŀ�Ŷ�";

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@ReadValue
	@WriteValue
	@Persistence
	private ObjectId _id;

	/**
	 * �ϼ���֯
	 */
	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	@ReadValue
	@WriteValue
	private ObjectId scope_id;

	@ReadValue
	private Integer seq;

	@ReadValue
	@WriteValue
	private String dir;

	/**
	 * ���ɱ����˳���
	 * 
	 * @param scope_id
	 * @param parent_id
	 * @return
	 */
	public OBSItem generateSeq() {
		seq = ServicesLoader.get(OBSService.class).nextOBSSeq(new BasicDBObject("scope_id", scope_id).append("parent_id", parent_id));
		return this;
	}

	private ObjectId org_id;

	@WriteValue("organization ")
	public void setOrganization(Organization org) {
		this.org_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("organization ")
	public Organization getOrganization() {
		return Optional.ofNullable(org_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id)).orElse(null);
	}

	@Persistence
	private String managerId;

	@ReadValue("managerInfo")
	@WriteValue("managerInfo")
	@SetValue
	private String managerInfo;

	@SetValue
	private RemoteFile managerHeadPic;

	@WriteValue("manager")
	public void setManager(User manager) {
		if (manager == null) {
			managerId = null;
			managerInfo = "";
			managerHeadPic = null;
		} else {
			managerId = manager.getUserId();
			managerInfo = manager.toString();
			List<RemoteFile> _pics = manager.getHeadPics();
			managerHeadPic = _pics != null && _pics.size() > 0 ? _pics.get(0) : null;
		}
	}

	@ReadValue("manager")
	private User getManager() {
		return Optional.ofNullable(managerId).map(id -> {
			try {
				return ServicesLoader.get(UserService.class).get(id);
			} catch (Exception e) {
				return null;
			}
		}).orElse(null);
	}

	public String getManagerId() {
		return managerId;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String description;

	@Exclude
	private String selectedRole;

	@ReadValue
	@WriteValue
	@SetValue
	private String roleId;

	@ReadValue
	@WriteValue
	@SetValue
	private String roleName;

	private boolean scopeRoot;

	private boolean isRole;

	@Structure("��Ŀ�Ŷ�/list")
	public List<Object> listSubOBSItem() {
		List<Object> result = new ArrayList<Object>();
		result.addAll(ServicesLoader.get(OBSService.class).getMember(new Query().bson(), _id));
		result.addAll(ServicesLoader.get(OBSService.class).getSubOBSItem(_id));
		User manager = getManager();
		if (manager != null && !result.contains(manager))
			result.add(manager);
		return result;
	}

	@Structure("��Ŀ�Ŷ�/count")
	public long countSubOBSItem() {
		long l = ServicesLoader.get(OBSService.class).countMember((BasicDBObject) new Query().bson().get("filter"), _id);
		l += ServicesLoader.get(OBSService.class).countSubOBSItem(_id);
		User manager = getManager();
		if (manager != null)
			l += 1;
		return l;
	}

	@WriteValue("role")
	private void writeRole(Dictionary dictionary) {
		this.selectedRole = dictionary.getId() + "#" + dictionary.getName();
		if (this.selectedRole != null) {
			roleId = selectedRole.split("#")[0];
			roleName = selectedRole.split("#")[1];
		}
	}

	@ReadValue("role")
	private Dictionary readRole() {
		return Optional.ofNullable(roleId).map(id -> ServicesLoader.get(CommonService.class).getProjectRole(id)).orElse(null);
	}

	@ReadOptions("selectedRole")
	public Map<String, String> getSystemOBSRole() {
		return ServicesLoader.get(CommonService.class).getDictionary("��ɫ����");
	}

	@WriteValue("selectedRole")
	private void writeSelectedRole(String selectedRole) {
		this.selectedRole = selectedRole;
		if (this.selectedRole != null) {
			roleId = selectedRole.split("#")[0];
			roleName = selectedRole.split("#")[1];
		}
	}

	@ReadValue("selectedRole")
	private String readSelectedRole() {
		if (roleId != null && roleName != null)
			return roleId + "#" + roleName;
		return "";
	}

	@GetValue("roleId")
	public String getId() {
		if (roleId != null && !roleId.isEmpty())
			return roleId;
		if (selectedRole != null && !selectedRole.isEmpty())
			return selectedRole.split("#")[0];
		return null;
	}

	@GetValue("roleName")
	public String getRoleName() {
		if (roleName != null && !roleName.isEmpty())
			return roleName;
		if (selectedRole != null && !selectedRole.isEmpty())
			return selectedRole.split("#")[1];
		return null;
	}

	@ReadValue({ "��֯�ṹͼ/id", "��֯�ṹͼ���鿴��/id" })
	private String getDiagramId() {
		return _id.toHexString();
	}

	@ReadValue({ "��֯�ṹͼ/title", "��֯�ṹͼ���鿴��/title" })
	private String getDiagramTitle() {
		if (isRole()) {
			return Check.isNotAssigned(roleName) ? "[δ����]" : roleName;
		} else {
			return Check.isNotAssigned(name) ? "[δ����]" : name;
		}

	}

	@ReadValue({ "��֯�ṹͼ/text", "��֯�ṹͼ���鿴��/text" })
	private String getDiagramText() {
		if (isRole()) {
			if (Check.isNotAssigned(managerInfo)) {
				return "[����]";
			} else {
				return managerInfo;// .substring(0, managerInfo.indexOf("["));
			}
		} else {
			if (Check.isNotAssigned(roleName)) {
				if (Check.isNotAssigned(managerInfo)) {
					return "�Ŷ�";
				} else {
					return managerInfo;// .substring(0, managerInfo.indexOf("["));
				}
			} else {
				if (Check.isNotAssigned(managerInfo)) {
					return roleName + " [����]";
				} else {
					return roleName + " " + managerInfo;// .substring(0, managerInfo.indexOf("["));
				}
			}
		}

	}

	@ReadValue({ "��֯�ṹͼ/parent", "��֯�ṹͼ���鿴��/parent" })
	private String getDiagramParent() {
		return parent_id == null ? "" : parent_id.toHexString();
	}

	@ReadValue({ "��֯�ṹͼ/img", "��֯�ṹͼ���鿴��/img" })
	private String getDiagramImage() {
		if (managerHeadPic != null) {
			return managerHeadPic.getClientSideURL("rwt");
		} else if (roleId != null) {
			try {
				return "/bvs/svg?text=" + URLEncoder.encode(roleId, "utf-8") + "&color=ffffff";
			} catch (UnsupportedEncodingException e) {
			}
		}
		return "";
	}

	public OBSItem setRoleId(String roleId) {
		this.roleId = roleId;
		return this;
	}

	public String getRoleId() {
		return roleId;
	}

	public OBSItem setRoleName(String roleName) {
		this.roleName = roleName;
		return this;
	}

	public OBSItem setManagerId(String managerId) {
		this.managerId = managerId;
		return this;
	}

	public OBSItem setName(String name) {
		this.name = name;
		return this;
	}

	public OBSItem setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public OBSItem set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public OBSItem setScope_id(ObjectId scope_id) {
		this.scope_id = scope_id;
		return this;
	}

	public OBSItem setScopeRoot(boolean scopeRoot) {
		this.scopeRoot = scopeRoot;
		return this;
	}

	public boolean isScopeRoot() {
		return scopeRoot;
	}

	public ObjectId getScope_id() {
		return scope_id;
	}

	public ObjectId getOrg_id() {
		return org_id;
	}

	public OBSItem setIsRole(boolean isRole) {
		this.isRole = isRole;
		return this;
	}

	public boolean isRole() {
		return isRole;
	}
}
