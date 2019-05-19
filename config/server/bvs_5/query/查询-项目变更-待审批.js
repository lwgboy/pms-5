[ {
	"$unwind" : "$reviewer"
}, {
	"$match" : {
		"reviewer.user" : "<userId>",
		"reviewer.date" : null,
		"status" : "<status>"
	}
}, {
	"$group" : {
		"_id" : "$_id"
	}
}, {
	"$lookup" : {
		"from" : "projectChange",
		"localField" : "_id",
		"foreignField" : "_id",
		"as" : "projectChange"
	}
}, {
	"$unwind" : "$projectChange"
}, {
	"$replaceRoot" : {
		"newRoot" : "$projectChange"
	}
} ]