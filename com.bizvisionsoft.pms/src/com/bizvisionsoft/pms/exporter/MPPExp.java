package com.bizvisionsoft.pms.exporter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;

import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.EngUtil;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;
import net.sf.mpxj.mpx.MPXWriter;

public class MPPExp {

	private ProjectFile projectFile;

	private List<Work> works;

	private List<WorkLink> links;

	private Map<ObjectId, Task> taskMap = new HashMap<>();

	private String projectName;

	public MPPExp() {
		projectFile = new ProjectFile();
	}
	
	public MPPExp setTasks(List<Work> works) {
		this.works = works;
		return this;
	}

	public MPPExp setLinks(List<WorkLink> links) {
		this.links = links;
		return this;
	}
	
	public MPPExp setProjectName(String projectName) {
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

		File folder = EngUtil.createTempDirectory();
		String filePath = folder.getPath()+"/"+fileName;
		
		projectFile.getProjectProperties().setName(projectName);
		
		// 创建工作和关联
		works.forEach(w -> createTasks(w));
		links.forEach(l -> createLinks(l));

		MPXWriter writer = new MPXWriter();
		// 设置中文
		writer.setLocale(Locale.CHINESE);
		writer.write(projectFile, filePath);
		
		String url = UserSession.bruiToolkit().createLocalFileDownloadURL(filePath);
		RWT.getClient().getService(UrlLauncher.class).openURL(url);
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
		task.setStart(w.getPlanStart());
		task.setFinish(w.getPlanFinish());
		task.setActualStart(w.getActualStart());
		task.setActualFinish(w.getActualFinish());

		ObjectId parent_id = w.getParent_id();
		if (parent_id != null) {
			Task parentTask = taskMap.get(parent_id);
			parentTask.addChildTask(task);
		}
	}



}
