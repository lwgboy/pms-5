[ {
	"$lookup" : {
		"from" : "cbsSubject",
		"localField" : "id",
		"foreignField" : "subjectNumber",
		"as" : "cbsSubject"
	}
}, {
	"$addFields" : {
		"cbsSubject.itemName" : "$name",
		"cbsSubject.subAccounts" : {
			"$concatArrays" : [ {
				"$ifNull" : [ "$subAccounts", [] ]
			}, [ "$id" ] ]
		}
	}
}, {
	"$project" : {
		"cbsSubject" : true
	}
}, {
	"$unwind" : "$cbsSubject"
}, {
	"$replaceRoot" : {
		"newRoot" : "$cbsSubject"
	}
}, {
	"$lookup" : {
		"from" : "cbs",
		"localField" : "cbsItem_id",
		"foreignField" : "_id",
		"as" : "cbs"
	}
}, {
	"$unwind" : "$cbs"
}, {
	"$graphLookup" : {
		"from" : "cbs",
		"startWith" : "$cbs._id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "cbs"
	}
}, {
	"$lookup" : {
		"from" : "work",
		"localField" : "cbs.scope_id",
		"foreignField" : "_id",
		"as" : "work"
	}
}, {
	"$unwind" : {
		"path" : "$work",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "cbs.scope_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$unwind" : {
		"path" : "$project",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"project_id" : {
			"$ifNull" : [ "$work.project_id", "$project._id" ]
		}
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
	"$graphLookup" : {
		"from" : "eps",
		"startWith" : "$project.eps_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "eps"
	}
}, {
	"$project" : {
		"id" : true,
		"budget" : {
			"$ifNull" : [ "$budget", 0.0 ]
		},
		"cost" : {
			"$ifNull" : [ "$cost", 0.0 ]
		},
		"itemName" : true,
		"subAccounts" : true,
		"project_id" : true,
		"cbs_id" : "$cbs._id",
		"eps_id" : "$eps._id"
	}
} ]