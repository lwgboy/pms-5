[ {
	"$lookup" : {
		"from" : "d1CFT",
		"localField" : "_id",
		"foreignField" : "problem_id",
		"as" : "team"
	}
}, {
	"$addFields" : {
		"team" : "$team.member"
	}
}, {
	"$match" : {
		"$or" : [ {
			"creationInfo.userId" : "<userId>"
		}, {
			"team" : "<userId>"
		} ]
	}
} ]