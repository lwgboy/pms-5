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
}, {
	"$group" : {
		"_id" : "$resTypeId"
	}
}, {
	"$lookup" : {
		"from" : "resourceType",
		"localField" : "_id",
		"foreignField" : "_id",
		"as" : "resType"
	}
}, {
	"$unwind" : {
		"path" : "$resType"
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$resType"
	}
} ]