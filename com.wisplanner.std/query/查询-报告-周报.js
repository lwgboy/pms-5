[ {
	"$match" : {
		"status" : "确认",
		"type" : "周报",
		"project_id" : "<project_id>",
		"reportDate" : {
			"$gte" : "<from>",
			"$lte" : "<to>"
		}
	}
}, {
	"$sort" : {
		"reportDate" : -1.0
	}
}, {
	"$lookup" : {
		"from" : "workReportItem",
		"localField" : "_id",
		"foreignField" : "report_id",
		"as" : "item"
	}
}, {
	"$unwind" : {
		"path" : "$item",
		"preserveNullAndEmptyArrays" : false
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "project_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$lookup" : {
		"from" : "work",
		"localField" : "stage_id",
		"foreignField" : "_id",
		"as" : "stage"
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "reporter",
		"foreignField" : "userId",
		"as" : "user"
	}
}, {
	"$lookup" : {
		"from" : "work",
		"localField" : "item.work_id",
		"foreignField" : "_id",
		"as" : "work"
	}
}, {
	"$project" : {
		"_id" : "$item._id",
		"reportDate" : true,
		"type" : true,
		"reportBy" : {
			"$arrayElemAt" : [ "$user.name", 0.0 ]
		},
		"projectName" : {
			"$arrayElemAt" : [ "$project.name", 0.0 ]
		},
		"projectId" : {
			"$arrayElemAt" : [ "$project.id", 0.0 ]
		},
		"stageName" : {
			"$arrayElemAt" : [ "$stage.fullName", 0.0 ]
		},
		"workName" : {
			"$arrayElemAt" : [ "$work.fullName", 0.0 ]
		},
		"workPlanFinish" : {
			"$arrayElemAt" : [ "$work.planFinish", 0.0 ]
		},
		"statement" : "$item.statement",
		"problems" : "$item.problems",
		"pmRemark" : "$item.pmRemark",
		"tags" : "$item.tags",
		"estimatedFinish" : "$item.estimatedFinish"
	}
} ]