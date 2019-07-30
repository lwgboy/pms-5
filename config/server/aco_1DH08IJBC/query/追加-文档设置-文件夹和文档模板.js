[ {
	"$lookup" : {
		"from" : "folderInTemplate",
		"let" : {
			"folderInTemplate_id" : "$folderInTemplate_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$eq" : [ "$_id", "$$folderInTemplate_id" ]
				}
			}
		}, {
			"$graphLookup" : {
				"from" : "folderInTemplate",
				"startWith" : "$parent_id",
				"connectFromField" : "parent_id",
				"connectToField" : "_id",
				"as" : "parent"
			}
		}, {
			"$addFields" : {
				"path" : "$parent.name"
			}
		}, {
			"$project" : {
				"parent" : false
			}
		} ],
		"as" : "folderInTemplate"
	}
}, {
	"$unwind" : {
		"path" : "$folderInTemplate",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "docuTemplate",
		"localField" : "docuTemplate_id",
		"foreignField" : "_id",
		"as" : "docuTemplate"
	}
}, {
	"$unwind" : {
		"path" : "$docuTemplate",
		"preserveNullAndEmptyArrays" : true
	}
} ]