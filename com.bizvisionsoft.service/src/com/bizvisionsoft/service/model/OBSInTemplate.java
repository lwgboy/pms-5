package com.bizvisionsoft.service.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.tools.Check;
import com.mongodb.BasicDBObject;

@PersistenceCollection("obsInTemplate")
public class OBSInTemplate {

	@Override
	@Label
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

	@Behavior({ "ɾ��", "�༭" })
	public boolean behaviorEditOrDeleteItem() {
		return !scopeRoot;
	}

	@Behavior({ "��Ա" })
	public boolean behaviorHasMember() {
		return !isRole;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private final String typeName = "��Ŀģ���Ŷ�";

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

	/**
	 * ���ɱ����˳���
	 * 
	 * @param scope_id
	 * @param parent_id
	 * @return
	 */
	public OBSInTemplate generateSeq() {
		seq = ServicesLoader.get(ProjectTemplateService.class)
				.nextOBSSeq(new BasicDBObject("scope_id", scope_id).append("parent_id", parent_id));
		return this;
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

	@ReadValue({ "��Ŀģ����֯�ṹͼ/title" })
	private String getTitle() {
		if (Check.isNotAssigned(name)) {
			return roleId;
		} else {
			return name;
		}
	}

	@ReadValue({ "��Ŀģ����֯�ṹͼ/text", "roleName" })
	@WriteValue
	@SetValue
	private String roleName;

	@ReadValue({ "OBSģ����֯�ṹͼ/title" })
	private String getDiagramTitle() {
		if (isRole()) {
			return Check.isNotAssigned(roleName) ? "[δ����]" : roleName;
		} else {
			return Check.isNotAssigned(name) ? "[δ����]" : name;
		}

	}

	@ReadValue({ "OBSģ����֯�ṹͼ/text" })
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

	private boolean scopeRoot;

	private boolean isRole;

	@ReadOptions("selectedRole")
	public Map<String, String> getSystemOBSRole() {
		return ServicesLoader.get(CommonService.class).getDictionary("��ɫ����");
	}

	@WriteValue("selectedRole")
	public void writeSelectedRole(String selectedRole) {
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

	@ReadValue({ "��Ŀģ����֯�ṹͼ/id", "OBSģ����֯�ṹͼ/id" })
	private String getDiagramId() {
		return _id.toHexString();
	}

	@ReadValue({ "��Ŀģ����֯�ṹͼ/parent", "OBSģ����֯�ṹͼ/parent" })
	private String getDiagramParent() {
		return parent_id == null ? "" : parent_id.toHexString();
	}

	@ReadValue({ "��Ŀģ����֯�ṹͼ/img", "OBSģ����֯�ṹͼ/img" })
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

	@ReadValue
	@WriteValue
	private String dir;

	public OBSInTemplate setRoleId(String roleId) {
		this.roleId = roleId;
		return this;
	}

	public OBSInTemplate setRoleName(String roleName) {
		this.roleName = roleName;
		return this;
	}

	public OBSInTemplate setManagerId(String managerId) {
		this.managerId = managerId;
		return this;
	}

	public OBSInTemplate setName(String name) {
		this.name = name;
		return this;
	}

	public OBSInTemplate setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public OBSInTemplate set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public OBSInTemplate setScope_id(ObjectId scope_id) {
		this.scope_id = scope_id;
		return this;
	}

	public OBSInTemplate setScopeRoot(boolean scopeRoot) {
		this.scopeRoot = scopeRoot;
		return this;
	}

	public boolean isScopeRoot() {
		return scopeRoot;
	}

	public ObjectId getScope_id() {
		return scope_id;
	}

	public OBSInTemplate setIsRole(boolean isRole) {
		this.isRole = isRole;
		return this;
	}

	public boolean isRole() {
		return isRole;
	}

	/**
	 * ��ȡOBSInTemplateʵ��
	 * 
	 * @param scope_id
	 *            ������Χ
	 * @param parent_id
	 *            �ϼ��ڵ�
	 * @param name
	 *            �ڵ�����
	 * @param roleId
	 *            ��ɫid
	 * @param roleName
	 *            ��ɫ����
	 * @param scopeRoot
	 *            �Ƿ���ڵ�
	 * @return
	 */
	public static OBSInTemplate getInstanceTeam(ObjectId scope_id, ObjectId parent_id, String name, String roleId,
			String roleName, boolean scopeRoot) {
		return new OBSInTemplate()// ��������Ŀ��OBS���ڵ�
				.set_id(new ObjectId()).setScope_id(scope_id)// ����scope_id��������֯�ڵ�ķ�Χ
				.setParent_id(parent_id)// �����ϼ���id
				.setName(name)// ���ø���֯�ڵ��Ĭ������
				.setRoleId(roleId)// ���ø���֯�ڵ�Ľ�ɫid
				.setRoleName(roleName)// ���ø���֯�ڵ������
				.setScopeRoot(scopeRoot);// ��������ڵ��Ƿ�Χ�ڵĸ��ڵ�
	}

}
