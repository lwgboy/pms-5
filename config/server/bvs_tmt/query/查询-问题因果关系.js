[ {
	"$match" : {
		"status" : "已关闭"
	}
}, {
	"$lookup" : {
		"from" : "causeRelation",
		"localField" : "_id",
		"foreignField" : "problem_id",
		"as" : "cause"
	}
}, {
	"$unwind" : {
		"path" : "$cause"
	}
}, {
	"$project" : {
		"pid" : "$classifyProblem._id",
		"_id" : "$cause.classifyCause._id"
	}
} ]