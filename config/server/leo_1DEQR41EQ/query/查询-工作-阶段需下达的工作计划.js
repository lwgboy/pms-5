[ {
	"$match" : {
		"project_id" : "<project_id>"
	}
}, {
	"$graphLookup" : {
		"from" : "work",
		"startWith" : "$parent_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "_stage"
	}
}, {
	"$addFields" : {
		"_stage" : {
			"$filter" : {
				"input" : "$_stage",
				"as" : "patentStage",
				"cond" : {
					"$eq" : [ "$$patentStage.stage", true ]
				}
			}
		}
	}
}, {
	"$match" : {
		"distributed" : {
			"$ne" : true
		},
		"actualFinish" : null,
		"stage" : false
	}
}, {
	"$unwind" : {
		"path" : "$_stage",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$match" : {
		"$or" : [ {
			"_stage" : null
		}, {
			"_stage.status" : "进行中"
		}, {
			"_stage.status" : "收尾中"
		} ]
	}
}, {
	"$match" :"<match>"
} ]