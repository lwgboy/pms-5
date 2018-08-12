[ {
	"$addFields" : {
		"stageCode" : {
			"$arrayElemAt" : [ {
				"$split" : [ "$wbsCode", "." ]
			}, 0.0 ]
		}
	}
}, {
	"$lookup" : {
		"from" : "work",
		"let" : {
			"f_project_id" : "$project_id",
			"f_wbsCode" : "$stageCode"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$$f_project_id", "$project_id" ]
					}, {
						"$eq" : [ "$$f_wbsCode", "$wbsCode" ]
					} ]
				}
			}
		}, {
			"$project" : {
				"name" : true,
				"_id" : false
			}
		} ],
		"as" : "stageInfo"
	}
}, {
	"$addFields" : {
		"stageName" : {
			"$arrayElemAt" : [ "$stageInfo.name", 0.0 ]
		}
	}
}, {
	"$project" : {
		"stageCode" : false,
		"stageInfo" : false
	}
} ]