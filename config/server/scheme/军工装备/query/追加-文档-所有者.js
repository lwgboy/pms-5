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
	"$addFields" : {
		"owner" : "$user.name",
	}
}, {
	"$project" : {
		"user" : false
	}
} ]