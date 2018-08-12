[ {
	"$match" : "<match>"
}, {
	"$addFields" : {
		"date" : [ "$actualStart", "$actualFinish" ]
	}
}, {
	"$unwind" : "$date"
}, {
	"$match" : {
		"date" : {
			"$ne" : null
		}
	}
}, {
	"$sort" : {
		"date" : -1.0
	}
}, {
	"$limit" : "<limit>"
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "chargerId",
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
		"userInfo" :"$user.name"
	}
}, {
	"$project" : {
		"user" : false
	}
} ]