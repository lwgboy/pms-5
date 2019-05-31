[ {
	"$match" : "<match>"
}, {
	"$lookup" : {
		"from" : "obs",
		"let" : {
			"p_scope_id" : "$project_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$$p_scope_id", "$scope_id" ]
					}, {
						"$in" : [ "$roleId", "<roleIdArray>" ]
					} ]
				}
			}
		} ],
		"as" : "obs"
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "project_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$unwind" : {
		"path" : "$project"
	}
}, {
	"$addFields" : {
		"receiver" : "$obs.managerId"
	}
} ]