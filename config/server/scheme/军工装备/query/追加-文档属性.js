[ {
	"$lookup" : {
		"from" : "user",
		"localField" : "owner",
		"foreignField" : "userId",
		"as" : "user"
	}
}, {
	"$unwind" : {
		"path" : "$user",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "project_id",
		"foreignField" : "<field from the documents of the 'from' collection>",
		"as" : "<output array field>"
	}
}, {
	"$graphLookup" : {
		"from" : "folder",
		"startWith" : "$folder_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "path",
		"depthField" : "level"
	}
}, {
	"$addFields" : {
		"owner" : "$user.name",
		"path" : {
			"$reduce" : {
				"input" : "$path.desc",
				"initialValue" : "",
				"in" : {
					"$concat" : [ "$$value", "/", "$$this" ]
				}
			}
		},
		"vid" : {
			"$concat" : [ "$major_vid", ".", {
				"$toString" : "$svid"
			} ]
		},
		"createBy" : "$_caccount.username",
		"createOn" : "$_cdate"
	}
}, {
	"$project" : {
		"user" : false
	}
} ]