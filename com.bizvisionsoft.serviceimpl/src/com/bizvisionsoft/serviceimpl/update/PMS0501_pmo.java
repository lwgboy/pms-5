package com.bizvisionsoft.serviceimpl.update;
import com.bizvisionsoft.service.common.Service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.serviceimpl.SystemServiceImpl;
import com.mongodb.client.MongoCollection;

/**
 * 更新内容：
 * <p>
 * 1. 重新创建数据库索引
 * <p>
 * 2. 增加组织模板，在项目创建后，可直接通过套用组织模板的方式创建项目团队。
 * <p>
 * 3. 修改组织中项目承担单位字段名称为：qualifiedContractor。
 * <p>
 * 4. 增加报告管理，在其中可以查看我管理的项目的日报、周报和月报。
 * <p>
 * 5.
 * 增加项目角色：项目管理组（角色编号为：PMO），功能角色：供应链经理、制造经理、财务经理，并通过PMO角色对所有项目、采购管理、生产管理、成本管理和报告管理中显示的内容进行细分。
 * <p>
 * 所有项目：具有项目总监和项目管理员权限的账户可以访问，具有项目总监权限的用户在其中可以查看所有项目信息。项目管理员权限的账户只能看到其作为项目PMO团队成员的项目。
 * <p>
 * 生产管理：具有制造管理和制造经理权限的账户可以访问，具有制造管理权限的用户在其中可以查看所有项目的生产工作。制造经理权限的账户只能看到其作为项目PMO团队成员的的生产工作。
 * <p>
 * 成本管理：具有成本管理和财务经理权限的账户可以访问，具有成本管理权限的用户在其中可以查看所有项目的成本数据。财务经理权限的账户只能看到其作为项目PMO团队成员的项目成本数据。
 * <p>
 * 报告管理：具有项目总监和项目管理员权限的账户可以访问，具有项目总监权限的用户在其中可以查看所有项目的报告。项目管理员权限的账户只能看到其作为项目PMO团队成员的项目的报告。
 * <p>
 * 注： 更新该功能时，系统将自动在已创建的项目中添加PMO团队。该功能更新完成后，请在服务器端存放js查询的目类中添加以下三个文件：
 * <p>
 * 1.查询-项目PMO成员.js；
 * <p>
 * 2.追加-CBS-CBS叶子节点ID.js；
 * <p>
 * 3.追加-CBSScope-CBS叶子节点ID.js。
 * 
 * @author gdiyang
 *
 */
public class PMS0501_pmo implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void run() {
		// 1.重新创建数据库索引
		new SystemServiceImpl().createIndex();

		// 2.添加词典（注意名称重复）
		insertDictionary("角色名称", "PMO", "项目管理组");
		insertDictionary("功能角色", "项目总监", "项目总监");
		insertDictionary("功能角色", "项目管理员", "项目管理员");
		insertDictionary("功能角色", "供应链管理", "供应链管理");
		insertDictionary("功能角色", "供应链经理", "供应链经理");
		insertDictionary("功能角色", "制造管理", "制造管理");
		insertDictionary("功能角色", "制造经理", "制造经理");
		insertDictionary("功能角色", "成本管理", "成本管理");
		insertDictionary("功能角色", "财务经理", "财务经理");

		// 3.修改组织里面的qualifiedContractor 原：projectBuilder
		c("organization").updateMany(new Document("projectBuilder", new Document("$ne", null)),
				new Document("$rename", new Document("projectBuilder", "qualifiedContractor")));
		logger.info("完成组织字段projectBuilder字段名修改，修改为qualifiedContractor。");

		// 4.历史项目添加PMO
		List<OBSItem> insertOBSItem = new ArrayList<OBSItem>();
		// 获取当前OBS团队中存在PMO的范围ID
		List<ObjectId> scope_ids = c("OBS").distinct("scope_id", new Document("roleId", "PMO"), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		// 获取需要创建PMO团队的项目，并构建需要插入到OBS中的PMO团队
		c("project").find(new Document("_id", new Document("$nin", scope_ids)))
				.projection(new Document("_id", true).append("obs_id", true)).forEach((Document doc) -> {
					insertOBSItem.add(new OBSItem().setIsRole(false).generateSeq().setRoleId("PMO").setName("项目管理组")
							.setScopeRoot(false).setParent_id(doc.getObjectId("obs_id"))
							.setScope_id(doc.getObjectId("_id")));
				});

		if (insertOBSItem.size() > 0)
			try {
				Service.col(OBSItem.class).insertMany(insertOBSItem);
				logger.info("完成项目添加PMO团队。");
			} catch (Exception e) {
				logger.info("为项目添加PMO团队时出现错误:" + e.getMessage());
			}
	}

	/**
	 * 在名称中创建角色
	 * 
	 * @param type
	 * @param id
	 * @param name
	 */
	private void insertDictionary(String type, String id, String name) {
		try {
			c("dictionary").insertOne(new Document("type", type).append("id", id).append("name", name));
			logger.info("完成 " + type + " " + id + " 的添加");
		} catch (Exception e) {
			logger.error("添加 " + type + " " + id + " 时出现错误:" + e.getMessage());
		}
	}

	private MongoCollection<Document> c(String name) {
		return Service.col(name);
	}

}
/**
 * 配置需更新内容：
 * <p>
 * 1.组件库-业务管理-增加组织模板的一组组件
 * <p>
 * 1.1组织模板组件: 组件名称:组织模板; 组件类型:表格组件;
 * 取数服务名称:com.bizvisionsoft.service.ProjectTemplateService
 * 启用顶部标题栏和工具栏;组件标题:组织模板管理
 * 表格列:id(列名称:id,文本:编号,左对齐,宽:160);name(列名称:name,文本:名称,左对齐,宽:300);description(列名称:description,文本:说明,左对齐,宽:320)
 * 行操作:删除(操作类型:删除选中对象),编辑(操作类型:编辑或打开选中对象.编辑器组件:组织模板编辑器),打开(操作类型:自定义操作,图标:/img/right.svg,样式:默认,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.projecttemplate.OpenProjectTemplateACT)
 * 工具栏操作:创建组织模板:(操作类型:创建新对象,编辑器组件:组织模板编辑器,插件标识:com.bizvisionsoft.service,类名:com.bizvisionsoft.service.model.OBSModule)
 * <p>
 * 1.2组织模板编辑器组件:组件名称:组织模板编辑器;标题:组织模板;使用窄模式
 * 字段:id(名称:id,文本:编号,不可为空,字段类型:单行文本),name(名称:name,文本:名称,不可为空,字段类型:单行文本),description(名称:description,文本:说明,字段类型:单行文本),epsInfos(名称:epsInfos,文本:适用范围,不可为空,字段类型:多个对象选择框,选择器组件:EPS管理)
 * <p>
 * 1.3组织模板选择器列表组件: 组件名称:组织模板选择器列表; 组件类型:表格组件;描述:用于项目选择组织模板;
 * 取数服务名称:com.bizvisionsoft.service.ProjectTemplateService
 * 表格列:id(列名称:id,文本:编号,左对齐,宽:160);name(列名称:name,文本:名称,左对齐,宽:300);description(列名称:description,文本:说明,左对齐,宽:320)
 * <p>
 * 1.4组织模板选择器组件:组件名称:组织模板选择器; 组件类型:弹出式选择器组件;描述:用于项目选择组织模板;表格组件:组织模板选择器列表;
 * <p>
 * 1.5OBS模板组织结构图组件: 组件名称:OBS模板组织结构图; 组件类型:树组件;
 * 取数服务名称:com.bizvisionsoft.service.ProjectTemplateService
 * 启用顶部标题栏和工具栏;组件标题:组织结构模板 节点操作:
 * 添加角色:类型:自定义操作,名称:添加角色,文本:添加角色,强制使用文本,图标:/img/add_16_w.svg,风格:一般,对象行为控制,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.projecttemplate.CreateOBSRoleItemACT;
 * 创建团队:类型:自定义操作,名称:创建团队,文本:创建团队,强制使用文本,图标:/img/team_w.svg,风格:一般,对象行为控制,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.projecttemplate.CreateOBSTeamItemACT;;
 * 指定担任者:类型:自定义操作,名称:指定担任者,文本:指定担任者,强制使用文本,图标:/img/appointment_w.svg,风格:一般,对象行为控制,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.projecttemplate.AppointmentOBSInTemplateACT;
 * 编辑:类型:自定义操作,名称:编辑,文本:编辑,强制使用文本,图标:/img/edit_w.svg,风格:一般,对象行为控制,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.projecttemplate.EditOBSItemACT;
 * 删除:类型:删除选中对象,名称:删除,文本:删除,强制使用文本,图标:/img/minus_w.svg,风格:警告;
 * 成员:类型:打开内容区,名称:成员,文本:成员,强制使用文本,图标:/img/people_list_w.svg,风格:信息,内容区组件:组织模板团队成员,原有内容区不关闭;
 * <p>
 * 1.6组织模板团队成员组件: 组件名称:组织模板团队成员; 组件类型:表格组件;描述:用于组织模板中编辑团队成员;
 * 取数服务名称:com.bizvisionsoft.service.OBSService 启用顶部标题栏和工具栏;组件标题:团队成员,显示传入对象名称,
 * 表格列,行操作同"团队成员(id:162dcc76808)"组件.
 * 工具栏操作:添加用户:类型:自定义操作,名称:添加用户,图标:/img/add_16_w.svg,风格:一般,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.projecttemplate.AddOBSInTemplateMember
 * <p>
 * 2.组件库-增加报告管理的一组组件
 * <p>
 * 2.1日报管理组件: 组件名称:日报管理; 组件类型:表格组件;
 * 取数服务名称:com.bizvisionsoft.service.WorkReportService 启用顶部标题栏和工具栏;组件标题:日报管理
 * 表格列:同"日报(id:163f1e35a8f)"组件
 * 行操作:打开日报(操作类型:自定义操作,图标:/img/right.svg,样式:默认,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 2.2周报管理组件: 组件名称:周报管理; 组件类型:表格组件;
 * 取数服务名称:com.bizvisionsoft.service.WorkReportService 启用顶部标题栏和工具栏;组件标题:周报管理
 * 表格列:同"周报(id:163fe7e3c53)"组件
 * 行操作:打开周报(操作类型:自定义操作,图标:/img/right.svg,样式:默认,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 2.3月报管理组件: 组件名称:月报管理; 组件类型:表格组件;
 * 取数服务名称:com.bizvisionsoft.service.WorkReportService 启用顶部标题栏和工具栏;组件标题:月报管理
 * 表格列:同"月报(id:163fe9eba7f)"组件
 * 行操作:打开月报(操作类型:自定义操作,图标:/img/right.svg,样式:默认,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 2.4日报(id:163f1e35a8f),周报(id:163fe7e3c53),月报(id:163fe9eba7f),待确认的报告(id:1646acf088f),项目月报(id:16401bc7721),项目周报(id:16401bbfa5c),项目日报(id:16401bb95a9)
 * 行操作"打开月报"修改:(操作类型:自定义操作,图标:/img/right.svg,样式:默认,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.workreport.OpenWorkReportACT)
 * <p>
 * 3.组件库-业务管理-组织管理：组织编辑器（id：162b75519b0）和根组织编辑器（id：162b7bb8236）中项目承担组织字段名改为qualifiedContractor
 * <p>
 * 4.开发环境侧边栏,操作栏目新增子栏目:名称为"更新项目管理组",插件标识:com.bizvisionsoft.onlinedesigner,类名:com.bizvisionsoft.onlinedesigner.systemupdate.SystemUpdateV0501_pmo
 * <p>
 * 5.主页侧边栏修改:
 * <p>
 * 5.1新增侧边栏"报告管理",放在"我的报告"栏目下方,角色设置为:项目管理员#项目总监.报告管理栏目下存在3个子栏目:1.日报管理:操作类型为打开内容区,内容区设置为"日报管理";2.周报管理:操作类型为打开内容区,内容区设置为"周报管理";3.月报管理:操作类型为打开内容区,内容区设置为"月报管理"
 * <p>
 * 5.2修改成本管理角色设置为:财务经理#财务管理#项目总监
 * <p>
 * 5.3修改采购管理角色设置为:供应链经理#供应链管理#项目总监
 * <p>
 * 5.4修改生产管理角色设置为:制造经理#制造管理#项目总监
 * <p>
 * 5.5需要添加：查询-项目PMO成员.js、追加-CBS-CBS叶子节点ID.js和追加-CBSScope-CBS叶子节点ID.js
 * <p>
 * 6.业务管理侧边栏增加组织模板,
 * <p>
 * 6.1放在"WBS模块"栏目下方.操作类型为打开内容区,内容区设置为"组织模板";
 * <p>
 * 6.2组件库-主页-项目主页-项目快速启动(创建状态)面板操作中增加action:套用组织模板,放在"使用项目模板"action下方.套用组织模板操作类型:自定义操作;图标为:/img/org_c.svg;插件标识com.bizvisionsoft.pms;类名:com.bizvisionsoft.pms.project.action.UseOBSModule;角色:PM#PPM
 * <p>
 * 6.3组件库-项目-项目团队-组织结构图节点操作中增加actuon：添加组织模板,放在"创建团队"action下方.添加组织模板操作类型:自定义操作;名称:添加组织模板;文本:添加模板;图标为:/img/module_w.svg;插件标识com.bizvisionsoft.pms;类名:com.bizvisionsoft.pms.obs.AddOBSModuleACT;
 * <p>
 * 6.4组件库-业务管理-项目模板管理-OBS模板-项目模板组织结构图.增加工具栏操作:另存为组织模板.操作类型:自定义操作;强制使用文本;按钮风格:一般;插件标识com.bizvisionsoft.pms;类名:com.bizvisionsoft.pms.projecttemplate.SaveAsOBSModuleACT
 * <p>
 * 6.5需要添加：查询-OBS-组织模板中重复的角色.js和追加-组织模板-角色.js
 * <p>
 * 7.打开变更操作修改:组件:项目变更(id:16441e4dd33)和待审批的项目变更(id:1644f5bf5cf)
 * 行操作"打开变更"修改:(操作类型:自定义操作,图标:/img/right.svg,样式:默认,插件标识:com.bizvisionsoft.pms,类名:com.bizvisionsoft.pms.projectchange.OpenProjectChangeACT)
 * <p>
 * 8.添加自定义导出
 * <p>
 * 8.1项目资金计划组件(id:162faeee5d8):操作中添加
 * 导出(操作类型:自定义操作,图标:/img/excel_w.svg,样式:一般,插件标识:com.bizvisionsoft.bruiengine,类名:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 8.2项目实际成本组件(id:165c2a4b23a):操作中添加
 * 导出(操作类型:自定义操作,图标:/img/excel_w.svg,样式:一般,插件标识:com.bizvisionsoft.bruiengine,类名:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 8.3资源分配组件(id:16370ff5184):工具栏中添加
 * 导出(操作类型:自定义操作,图标:/img/excel_w.svg,样式:一般,插件标识:com.bizvisionsoft.bruiengine,类名:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 8.4资源用量组件(id:16396e1b1ae):工具栏中添加
 * 导出(操作类型:自定义操作,图标:/img/excel_w.svg,样式:一般,插件标识:com.bizvisionsoft.bruiengine,类名:com.bizvisionsoft.bruiengine.action.ExportAll)
 * <p>
 * 9.增加undo和redo按钮：组件库-项目-进度计划-项目甘特图（编辑）（id：1633ee05a77）的工具栏操作中增加撤销和恢复按钮
 * <p>
 * 9.1撤销按钮：操作类型：自定义操作；操作名称：撤销；文本：撤销；强制使用文本；风格：一般；插件标识：com.bizvisionsoft.pms；类名：com.bizvisionsoft.pms.work.gantt.action.GanttEditUndo
 * <p>
 * 9.2恢复按钮：操作类型：自定义操作；操作名称：恢复；文本：恢复；强制使用文本；风格：一般；插件标识：com.bizvisionsoft.pms；类名：com.bizvisionsoft.pms.work.gantt.action.GanttEditRedo
 * <p>
 * 10.增加阶段编辑器(可复制"甘特图阶段工作编辑器(id:162ff4cd460)"进行修改)：
 * 组件库-项目-编辑器-工作编辑器中增加甘特图总成阶段编辑器，组件名称：甘特图总成阶段编辑器，组件标题：阶段，描述：用于阶段分解后编辑阶段，窄，加入到父上下文
 * 字段：text（字段类型：单行文本框，字段名称：text，字段文本：阶段名称，不可为空）；
 * start_date（字段类型：日期时间选择，字段名称：start_date，字段文本：计划开始，只读，日期类型：日期时间）；
 * end_date（字段类型：日期时间选择，字段名称：end_date，字段文本：计划完成，只读，日期类型：日期时间）；
 * charger（字段类型：对象选择框，字段名称：charger，字段文本：阶段负责，不可为空，选择器组件：项目团队（id：162d7e505eb））
 * <p>
 * 11.增加编辑工作时计划开始时间、计划完成和工期的交互：
 * 组件-项目-编辑器-工作编辑器中：甘特图工作编辑器(id：1628fc969a5)和甘特图阶段工作编辑器(id：162ff4cd460)中在start_date下方增加行，并将end_date移动到新增行的下级。并在end_date下方增加新字段：duration。
 * 修改end_date字段（写入后更新其他字段：duration）
 * 新增duration字段（字段类型：单行文本框、字段名称：duration、文本：工期、输入检验：整数、写入后更新其它字段：end_date）
 * <p>
 * 12.修改资源计划维护方式，其中需要修改以下JS： 查询-资源-计划和实际用量-负责人所在部门.js 查询-资源-计划和实际用量-项目.js
 * 查询-资源-计划用量-部门.js 查询-资源-计划用量-项目.js 查询-资源-计划用量.js 查询-资源-实际用量.js 查询-资源.js
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 *
 *
 *
 */
