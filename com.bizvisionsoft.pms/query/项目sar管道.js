[ {
	"$lookup" : {
		"from" : "work",
		"let" : {
			"project_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, "$stage", "$actualFinish" ]
				}
			}
		}, {
			"$project" : {
				"_sar" : {
					"$divide" : [ {
						"$subtract" : [ "$planFinish", "$planStart" ]
					}, {
						"$subtract" : [ "$actualFinish", "$actualStart" ]
					} ]
				}
			}
		} ],
		"as" : "_stage"
	}
}, {
	"$addFields" : {
		"_sar" : {
			"$avg" : "$_stage._sar"
		}
	}
}, {
	"$addFields" : {
		"sar" : {
			"$cond" : [ {
				"$gt" : [ "$_sar", 1.0 ]
			}, 1.0, "$_sar" ]
		}
	}
}, {
	"$project" : {
		"_stage" : false,
		"_sar" : false
	}
} ],