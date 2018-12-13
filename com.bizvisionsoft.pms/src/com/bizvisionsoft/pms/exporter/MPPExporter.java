package com.bizvisionsoft.pms.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.service.tools.FileTools;

import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectProperties;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpx.MPXWriter;

public class MPPExporter<W, L> {

	private ProjectFile projectFile;

	private List<W> works;

	private List<L> links;
	
	private Map<ObjectId, Task> taskMap = new HashMap<>();

	private String projectName;
	
	private TaskConvertor<W> taskConvertor;
	
	private LinkConvertor<L> linkConvertor;

	public MPPExporter() {
		projectFile = new ProjectFile();
	}
	
	public MPPExporter<W,L> setTasks(List<W> works) {
		this.works = works;
		return this;
	}

	public MPPExporter<W,L> setLinks(List<L> links) {
		this.links = links;
		return this;
	}
	
	public MPPExporter<W,L> setProjectName(String projectName) {
		this.projectName = projectName;
		return this;
	}
	
	public MPPExporter<W,L> setLinkConvertor(LinkConvertor<L> linkConvertor) {
		this.linkConvertor = linkConvertor;
		return this;
	}
	
	public MPPExporter<W,L> setTaskConvertor(TaskConvertor<W> taskConvertor) {
		this.taskConvertor = taskConvertor;
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
		works.forEach(this::convertTask);
		links.forEach(this::convertLink);

		MPXWriter writer = new MPXWriter();
		// 设置中文
		writer.setLocale(Locale.CHINESE);
		writer.write(projectFile, filePath);
		
		UserSession.bruiToolkit().downloadLocalFile(filePath);
	}

	private void convertLink(L l) {
		linkConvertor.accept(l, taskMap);
	}

	private void convertTask(W w) {
		taskConvertor.accept(w, projectFile, taskMap);
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

}
