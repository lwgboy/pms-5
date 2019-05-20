[ {
	"$match" : {
		"$or" : [ {
			"member" : "<userId>"
		}, {
			"managerId" : "<userId>"
		} ]
	}
}, {
	"$lookup" : {
		"from" : "work",
		"localField" : "scope_id",
		"foreignField" : "_id",
		"as" : "work"
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "scope_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "work.project_id",
		"foreignField" : "_id",
		"as" : "project2"
	}
}, {
	"$unwind" : {
		"path" : "$project",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$unwind" : {
		"path" : "$project2",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"project_id" : {
			"$cond" : [ "$project", "$project._id", {
				"$cond" : [ "$project2", "$project2._id", null ]
			} ]
		}
	}
}, {
	"$group" : {
		"_id" : "$project_id"
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$replaceRoot" : {
		"newRoot" : {
			"$arrayElemAt" : [ "$project", 0 ]
		}
	}
}, {
	"$match" : {
		"status" : {
			"$ne" : "已关闭"
		}
	}
} ]