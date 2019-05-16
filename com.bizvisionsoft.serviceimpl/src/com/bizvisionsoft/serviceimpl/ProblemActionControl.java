package com.bizvisionsoft.serviceimpl;

import java.util.Arrays;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProblemService;

public class ProblemActionControl extends BasicServiceImpl {

	// role "组长", "设计", "工艺", "生产", "质量", "顾客代表"

	public boolean hasPrivate(ObjectId problem_id, String action, String userId,String domain){
		if (ProblemService.ACTION_EDIT_TEAM.equals(action) //
				|| ProblemService.ACTION_PROBLEM_START.equals(action)//
				|| ProblemService.ACTION_PROBLEM_CANCEL.equals(action) //
				|| ProblemService.ACTION_PROBLEM_CLOSE.equals(action)){
			// 问题创建者 或组长
			return isCreator(problem_id, userId, domain) || hasRole(domain,problem_id, userId, "0");
		
		} else if (ProblemService.ACTION_ICA_CONFIRM.equals(action) //
				|| ProblemService.ACTION_PCA_CONFIRM.equals(action)){
			// 临时控制行动有效性确认需由团队组长或顾客代表执行
			return hasRole(domain,problem_id, userId, "0", "5");
		
		} else if (ProblemService.ACTION_PCA_APPROVE.equals(action) //
				|| ProblemService.ACTION_PCA_VALIDATE.equals(action)){
			return hasRole(domain,problem_id, userId, "0");
		}

		return false;
	}

	private boolean hasRole(String domain,ObjectId problem_id, String userId, String... role){
		return c("d1CFT",domain).countDocuments(new Document("problem_id", problem_id).append("member", userId).append("role",
				new Document("$in", Arrays.asList(role)))) > 0;
	}

	private boolean isCreator(ObjectId problem_id, String userId,String domain){
		return c("problem",domain).countDocuments(new Document("_id", problem_id).append("creationInfo.userId", userId)) > 0;
	}

}
