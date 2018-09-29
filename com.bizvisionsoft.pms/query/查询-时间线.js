[ {
	"$match" : "<match>"
}, {
	"$addFields" : {
		"date" : {"$cond":["$actualFinish","$actualFinish","$actualStart"]}
	}
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
}]