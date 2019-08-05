[ {
	"$lookup" : {
		"from" : "resourcePlan",
		"localField" : "_id",
		"foreignField" : "work_id",
		"as" : "resPlan"
	}
}, {
	"$lookup" : {
		"from" : "resourceActual",
		"localField" : "_id",
		"foreignField" : "work_id",
		"as" : "resActual"
	}
}, {
	"$project" : {
		"res" : {
			"$concatArrays" : [ "$resPlan", "$resActual" ]
		}
	}
}, {
	"$unwind" : {
		"path" : "$res"
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$res"
	}
} ]