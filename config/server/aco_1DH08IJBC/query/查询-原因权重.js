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
		"cid" : "$cause.classifyCause._id",
		"cname" : "$cause.classifyCause.path",
		"value" : {
			"$multiply" : [ "$cause.weight", "$cause.probability" ]
		}
	}
}, {
	"$group" : {
		"_id" : {
			"cid" : "$cid",
			"cname" : "$cname"
		},
		"value" : {
			"$sum" : "$value"
		}
	}
}, {
	"$project" : {
		"_id" : "$_id.cid",
		"name" : "$_id.cname",
		"value" : "$value"
	}
}, {
	"$sort" : {
		"_id" : 1.0
	}
} ]