[ {
	"$lookup" : {
		"from" : "project",
		"localField" : "_id",
		"foreignField" : "cbs_id",
		"as" : "project"
	}
}, {
	"$unwind" : "$project"
}, {
	"$match" : {
		"project.status" : {
			"$nin" : [ "已创建", "已关闭" ]
		}
	}
}, {
	"$addFields" : {
		"scopeId" : "$project.id",
		"eps_id" : "$project.eps_id",
		"impUnit_id" : "$project.impUnit_id",
		"pmId" : "$project.pmId",
	}
}, {
	"$project" : {
		"project" : false
	}
} ]