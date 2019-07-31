[ {
	"$match" : {
		"_id" : "<_id>"
	}
}, {
	"$lookup" : {
		"from" : "exportDocRule",
		"localField" : "_id",
		"foreignField" : "formDef_id",
		"as" : "exportDocRule"
	}
}, {
	"$unwind" : {
		"path" : "$exportDocRule",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$exportDocRule"
	}
} ]