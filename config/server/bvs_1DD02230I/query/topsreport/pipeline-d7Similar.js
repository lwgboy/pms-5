[ {
	"$match" : {
		"problem_id" :"<id>"
	}
}, {
	"$unwind" : {
		"path" : "$id",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"ids" : {
			"$concat" : [ "$id.id", " ", "$id.keyword" ]
		}
	}
}, {
	"$group" : {
		"_id" : "$_id",
		"problem_id" : {
			"$first" : "$problem_id"
		},
		"similar" : {
			"$first" : "$similar"
		},
		"desc" : {
			"$first" : "$desc"
		},
		"degree" : {
			"$first" : "$degree"
		},
		"prob" : {
			"$first" : "$prob"
		},
		"id" : {
			"$addToSet" : "$ids"
		}
	}
} ]