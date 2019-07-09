[ {
	"$match" : {
		"parent_id" : null
	}
}, {
	"$lookup" : {
		"from" : "problem",
		"let" : {
			"class_id" : "$_id"
		},
		"pipeline" : [ {"$unwind":{
	    	"path":"$classifyProblems"
	    }},{
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$in" : [ "$$class_id", "$classifyProblems._ids" ]
					}, {
						"$in" : [ "$status", [ "解决中", "已关闭" ] ]
					} ]
				}
			}
		} ],
		"as" : "problem"
	}
}, {
	"$unwind" : {
		"path" : "$problem"
	}
}, {
	"$project" : {
		"name" : true,
		"problem_id" : "$problem._id",
		"date" : {
			"$dateToString" : {
				"date" : "$problem.issueDate",
				"format" : "%Y-%m"
			}
		}
	}
}, {
	"$group" : {
		"_id" : {
			"name" : "$name",
			"date" : "$date"
		},
		"count" : {
			"$sum" : 1.0
		}
	}
}, {
	"$group" : {
		"_id" : "$_id.name",
		"source" : {
			"$addToSet" : {
				"_id" : "$_id.date",
				"value" : "$count"
			}
		}
	}
} ]