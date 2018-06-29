package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;

/**
 * 
 * 
 * <h3 id="link_properties">Properties of a link object</h3>
 * 
 * <ul>
 * <li><b><i>Mandatory properties</i></b></li>
 * <ul>
 * <li><b>id</b> - (<i> string|number </i>) the link id.</li>
 * <li><b>source</b> - (<i> number </i>) the id of a task that the dependency
 * will start from.</li>
 * <li><b>target</b> - (<i> number </i>) the id of a task that the dependency
 * will end with.</li>
 * <li><b>type</b> - (<i>string</i>) the dependency type. The available values
 * are stored in the <a href="api__gantt_links_config.html">links</a> object. By
 * default, they are:</li>
 * <ul>
 * <li><b>"0"</b> - 'finish_to_start'.</li>
 * <li><b>"1"</b> - 'start_to_start'.</li>
 * <li><b>"2"</b> - 'finish_to_finish'.</li>
 * <li><b>"3"</b> - 'start_to_finish'.</li>
 * </ul>
 * If you want to store the dependency types in some way other than the default
 * values('0','1','2'), you may change values of the related properties of the
 * <a href="api__gantt_links_config.html">links</a> object. For example:
 * 
 * <pre>
 * <code><pre class="js">gantt.<span class="me1">config</span>.<span class=
 * "me1">links</span>.<span class="me1">start_to_start</span> <span class=
 * "sy0">=</span> <span class="st0">"start2start"</span><span class=
 * "sy0">;</span></pre></code>
 * </pre>
 * 
 * Note, these values affect only the way the dependency type is stored, not the
 * behaviour of visualization.
 * </ul>
 * <li><b><i>Optional properties</i></b></li>
 * <ul>
 * <li><b>lag</b>-(<i>number</i>) optional, <a href=
 * "desktop__auto_scheduling.html/settinglagandleadtimesbetweentasks">task
 * lag</a>.</li>
 * <li><b>readonly</b>-(<i>boolean</i>) optional, can mark link as
 * <a href="desktop__readonly_mode.html">readonly</a>.</li>
 * <li><b>editable</b>-(<i>boolean</i>) optional, can mark link as
 * <a href="desktop__readonly_mode.html">editable</a>.</li>
 * </ul>
 * </ul>
 * 
 * <!-- Content Area End -->
 * 
 * <script type="text/javascript">var disqus_shortname = 'dhxdocumentation';var
 * disqus_developer = 1;(function() {var dsq = document.createElement('script');
 * dsq.type = 'text/javascript'; dsq.async = true;dsq.src = '//' +
 * disqus_shortname +
 * '.disqus.com/embed.js';(document.getElementsByTagName('head')[0] ||
 * document.getElementsByTagName('body')[0]).appendChild(dsq);})();</script>
 * </div>
 * 
 * @author hua
 *
 */
@PersistenceCollection("worklinks")
@Strict
public class WorkLink {

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@Persistence
	private ObjectId _id;

	@ReadValue({ "��Ŀ����ͼ/id", "��Ŀ����ͼ���༭��/id", "��Ŀ����ͼ���ޱ��鿴��/id", "��Ŀ����ͼ����Դ���䣩/id", "��Ŀ��չ����ͼ/id", "��Ŀ���߸���ͼ/id" })
	public String getId() {
		return _id.toHexString();
	}

	@WriteValue({ "��Ŀ����ͼ/id", "��Ŀ����ͼ���༭��/id", "��Ŀ����ͼ���ޱ��鿴��/id", "��Ŀ����ͼ����Դ���䣩/id", "��Ŀ��չ����ͼ/id", "��Ŀ���߸���ͼ/id" })
	public WorkLink setId(String id) {
		this._id = new ObjectId(id);
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@Persistence
	private ObjectId project_id;

	@ReadValue({ "��Ŀ����ͼ/project", "��Ŀ����ͼ���༭��/project", "��Ŀ����ͼ���ޱ��鿴��/project", "��Ŀ����ͼ����Դ���䣩/project", "��Ŀ��չ����ͼ/project",
			"��Ŀ���߸���ͼ/project" })
	public String getProject() {
		return project_id == null ? null : project_id.toHexString();
	}

	@WriteValue({ "��Ŀ����ͼ/project", "��Ŀ����ͼ���༭��/project", "��Ŀ����ͼ���ޱ��鿴��/project", "��Ŀ����ͼ����Դ���䣩/project",
			"��Ŀ��չ����ͼ/project", "��Ŀ���߸���ͼ/project" })
	public WorkLink setProject(String project_id) {
		this.project_id = project_id == null ? null : new ObjectId(project_id);
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	private Work source;

	@ReadValue({ "��Ŀ����ͼ/source", "��Ŀ����ͼ���༭��/source", "��Ŀ����ͼ���ޱ��鿴��/source", "��Ŀ����ͼ����Դ���䣩/source", "��Ŀ��չ����ͼ/source" })
	public String getSource() {
		return source == null ? null : source.get_id().toHexString();
	}

	@ReadValue("������ӹ�ϵ�༭����1��1��/sourceTask")
	public String getSourceTaskLabel() {
		return source.toString();
	}

	@GetValue("source")
	public ObjectId getSourceId() {
		return source.get_id();
	}

	@SetValue("source")
	public void setSourceId(ObjectId source_id) {
		source = ServicesLoader.get(WorkService.class).getWork(source_id);
	}

	@Exclude
	private Work sourceWork;

	@SetValue("sourceWork")
	public void setSourceWork(Work sourceWork) {
		this.sourceWork = sourceWork;
	}

	@ReadValue({ "��Ŀ���߸���ͼ/source" })
	public String getSourceWork() {
		return sourceWork == null ? null : sourceWork.get_id().toHexString();
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	private Work target;

	@ReadValue({ "��Ŀ����ͼ/target", "��Ŀ����ͼ���༭��/target", "��Ŀ����ͼ���ޱ��鿴��/target", "��Ŀ����ͼ����Դ���䣩/target", "��Ŀ��չ����ͼ/target" })
	public String getTarget() {
		return target == null ? null : target.get_id().toHexString();
	}

	@ReadValue("������ӹ�ϵ�༭����1��1��/targetTask")
	public String getTargetTaskLabel() {
		return target.toString();
	}

	@GetValue("target")
	public ObjectId getTargetId() {
		return target.get_id();
	}

	@SetValue("target")
	public void setTargetId(ObjectId target_id) {
		target = ServicesLoader.get(WorkService.class).getWork(target_id);
	}

	@Exclude
	private Work targetWork;

	@SetValue("targetWork")
	public void setTargetWork(Work targetWork) {
		this.targetWork = targetWork;
	}

	@ReadValue({ "��Ŀ���߸���ͼ/target" })
	public String getTargetWork() {
		return targetWork == null ? null : targetWork.get_id().toHexString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@ReadValue
	@WriteValue
	@Persistence
	private String type;
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@ReadValue({ "��Ŀ����ͼ/lag", "��Ŀ����ͼ���༭��/lag", "��Ŀ����ͼ���ޱ��鿴��/lag", "��Ŀ����ͼ����Դ���䣩/lag", "��Ŀ��չ����ͼ/lag", "��Ŀ���߸���ͼ/lag" })
	@WriteValue({ "��Ŀ����ͼ/lag", "��Ŀ����ͼ���༭��/lag", "��Ŀ����ͼ���ޱ��鿴��/lag", "��Ŀ����ͼ����Դ���䣩/lag", "��Ŀ��չ����ͼ/lag", "��Ŀ���߸���ͼ/lag" })
	@Persistence
	private int lag;

	@Persistence
	private ObjectId space_id;

	@WriteValue("������ӹ�ϵ�༭����1��1��/lag")
	public WorkLink setLagFromEditor(String lag) {
		this.lag = Integer.parseInt(lag);
		return this;
	}

	@ReadValue("������ӹ�ϵ�༭����1��1��/lag")
	public String getLagForEdior() {
		return "" + lag;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// �����ǿ���gantt�Ŀͻ��˵�����
	@ReadValue("editable")
	public Boolean getEditable() {
		return true;
	}

	public WorkLink set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public static WorkLink newInstance(ObjectId project_id) {
		return new WorkLink().set_id(new ObjectId()).setProject_id(project_id);
	}

	public WorkLink setSource(Work source) {
		this.source = source;
		return this;
	}

	public WorkLink setTarget(Work target) {
		this.target = target;
		return this;
	}

	public WorkLink setType(String type) {
		this.type = type;
		return this;
	}

	public WorkLink setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public void setSpaceId(ObjectId space_id) {
		this.space_id = space_id;
	}

}
