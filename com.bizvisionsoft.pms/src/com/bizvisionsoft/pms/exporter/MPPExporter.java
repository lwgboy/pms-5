package com.bizvisionsoft.pms.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.tools.FileTools;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectProperties;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;
import net.sf.mpxj.mpx.MPXWriter;

public class MPPExporter {

	private ProjectFile projectFile;

	private List<Work> works;

	private List<WorkLink> links;

	private Map<ObjectId, Task> taskMap = new HashMap<>();

	private String projectName;

	public MPPExporter() {
		projectFile = new ProjectFile();
	}
	
	public MPPExporter setTasks(List<Work> works) {
		this.works = works;
		return this;
	}

	public MPPExporter setLinks(List<WorkLink> links) {
		this.links = links;
		return this;
	}
	
	public MPPExporter setProjectName(String projectName) {
		this.projectName = projectName;
		return this;
	}
	
	public void export() throws Exception {
	    Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
	    String fileName = this.projectName;
	    Matcher matcher = pattern.matcher(fileName);
	    fileName= matcher.replaceAll(""); 
	    export(fileName+".mpx");
	}

	public void export(String fileName) throws IOException {
		//TODO 检查必要的数据

		File folder = FileTools.createTempDirectory(RWT.getRequest().getSession().getId().toUpperCase());
		String filePath = folder.getPath()+"/"+fileName;
		
		ProjectProperties properties = projectFile.getProjectProperties();
		properties.setName(projectName);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND, 0);
		properties.setDefaultStartTime(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		cal.add(Calendar.SECOND, -1);
		
		properties.setMinutesPerDay(24*60);
		properties.setMinutesPerWeek(7*24*60);
		createCalendar();
		
		
		// 创建工作和关联
		works.forEach(w -> createTasks(w));
		links.forEach(l -> createLinks(l));

		MPXWriter writer = new MPXWriter();
		// 设置中文
		writer.setLocale(Locale.CHINESE);
		writer.write(projectFile, filePath);
		
		UserSession.bruiToolkit().downloadLocalFile(filePath);
	}

	private ProjectCalendar createCalendar() {
		//TODO日历问题
		ProjectCalendar c = projectFile.add7x24Calendar();
		c.setName(projectName);
		c.setMinutesPerDay(24*60);
		c.setMinutesPerWeek(7*24*60);
		projectFile.setDefaultCalendar(c);
		return c;
	}

	private void createLinks(WorkLink w) {
		ObjectId sourceId = w.getSourceId();
		ObjectId targetId = w.getTargetId();
		String type = w.getType();
		int lag = w.getLag();

		Task src = taskMap.get(sourceId);
		Task tgt = taskMap.get(targetId);

		RelationType rt;
		if ("FF".equals(type)) {
			rt = RelationType.FINISH_FINISH;
		} else if ("SS".equals(type)) {
			rt = RelationType.START_START;
		} else if ("SF".equals(type)) {
			rt = RelationType.START_FINISH;
		} else {
			rt = RelationType.FINISH_START;
		}

		tgt.addPredecessor(src, rt, Duration.getInstance(lag, TimeUnit.DAYS));
	}

	private void createTasks(Work w) {
		Task task = projectFile.addTask();
		taskMap.put(w.get_id(), task);

		task.setName(w.getText());
		task.setNotes(w.getFullName());
		Date planStart = w.getPlanStart();
		task.setStart(planStart);
		Date planFinish = w.getPlanFinish();
		task.setFinish(planFinish);
		task.setDuration(Duration.getInstance(w.getPlanDuration(),TimeUnit.DAYS));
		ObjectId parent_id = w.getParent_id();
		if (parent_id != null) {
			Task parentTask = taskMap.get(parent_id);
			parentTask.addChildTask(task);
		}
	}



}
