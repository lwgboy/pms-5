package com.bizvisionsoft.serviceimpl;

import java.util.List;

import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.mongodb.BasicDBObject;

public class ProblemServiceImpl extends BasicServiceImpl implements ProblemService {

	@Override
	public Problem insert(Problem p) {
		return super.insert(p);
	}

	@Override
	public List<Problem> list(BasicDBObject condition, String status, String userid, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(BasicDBObject filter, String status, String userid, String lang) {
		// TODO Auto-generated method stub
		return 0;
	}

}
