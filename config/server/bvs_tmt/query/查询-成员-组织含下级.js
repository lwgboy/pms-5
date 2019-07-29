[ {
	"$match" : {
		"_id" : {
			"$in" : "<ids>"
		}
	}
}, {
	"$graphLookup" : {
		"from" : "organization",
		"startWith" : "$_id",
		"connectFromField" : "_id",
		"connectToField" : "parent_id",
		"as" : "_iterSub"
	}
}, {
	"$addFields" : {
		"_iterSub" : {
			"$concatArrays" : [ [ "$$ROOT" ], "$_iterSub" ]
		}
	}
}, {
	"$unwind" : {
		"path" : "$_iterSub",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$_iterSub"
	}
}, {
	"$project" : {
		"_iterSub" : false
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "_id",
		"foreignField" : "org_id",
		"as" : "users"
	}
}, {
	"$unwind" : {
		"path" : "$users"
	}
}, {
	"$addFields" : {
		"users.orgFullName" : "$fullName"
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$users"
	}
}, {
	"$match" : {
		"activated" : true
	}
}, {
	"$project" : {
		"password" : false
	}
} ]