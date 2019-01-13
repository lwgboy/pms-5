package com.bizvisionsoft.pms.problem.assembly;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.ClassifyCause;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class CauseContentProvider implements ITreeContentProvider {

	String type;

	Problem problem;

	private ProblemService service;

	CauseContentProvider(Problem problem, String type) {
		this.problem = problem;
		this.type = type;
		service = Services.get(ProblemService.class);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object value) {
		return ((List<?>) value).toArray();
	}

	@Override
	public Object[] getChildren(Object elem) {
		BasicDBObject filter = new BasicDBObject();
		if (elem instanceof ClassifyCause) {
			filter.append("type", type).append("subject", ((ClassifyCause) elem).name).append("parent_id", null).append("problem_id", problem.get_id());
		} else if (elem instanceof CauseConsequence) {
			filter.append("type", type).append("subject", ((CauseConsequence) elem).getSubject())
					.append("parent_id", ((CauseConsequence) elem).get_id()).append("problem_id", problem.get_id());
		} else {
			return new Object[0];
		}
		return service.listCauseConsequences(filter).toArray();
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object elem) {
		BasicDBObject filter = new BasicDBObject();
		if (elem instanceof ClassifyCause) {
			filter.append("type", type).append("subject", ((ClassifyCause) elem).name).append("problem_id", problem.get_id());
		} else if (elem instanceof CauseConsequence) {
			filter.append("type", type).append("subject", ((CauseConsequence) elem).getSubject())
					.append("parent_id", ((CauseConsequence) elem).get_id()).append("problem_id", problem.get_id());
		} else {
			return false;
		}
		return service.countCauseConsequences(filter) > 0;
	}

}