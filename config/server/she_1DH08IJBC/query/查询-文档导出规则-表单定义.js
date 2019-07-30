[ {
	"$match" : {
		"_id" : "<_id>"
	}
}, {
	"$lookup" : {
		"from" : "exportDocRule",
		"localField" : "exportDocRule_ids",
		"foreignField" : "_id",
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