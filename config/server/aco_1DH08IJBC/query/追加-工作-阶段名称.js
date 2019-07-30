[ {
	"$graphLookup" : {
		"from" : "work",
		"startWith" : "$parent_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "parent"
	}
}, {
	"$addFields" : {
		"parent" : {
			"$filter" : {
				"input" : "$parent",
				"as" : "patentStage",
				"cond" : {
					"$eq" : [ "$$patentStage.stage", true ]
				}
			}
		}
	}
}, {
	"$unwind" : {
		"path" : "$parent",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"stageName" : "$parent.name"
	}
}, {
	"$project" : {
		"parent" : false
	}
} ]