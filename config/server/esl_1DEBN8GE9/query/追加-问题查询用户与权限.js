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
		"$and" : [ {
			"_id" : "<_id>"
		}, {
			"team" : "<userId>"
		} ]
	}
} ]