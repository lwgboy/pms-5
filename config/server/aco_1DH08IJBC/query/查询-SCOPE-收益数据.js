[ {
	"$match" : {
		"scope_id" : "<scope_id>"
	}
}, {
	"$group" : {
		"_id" : "$subject",
		"periodValue" : {
			"$push" : {
				"k" : "$id",
				"v" : "$amount"
			}
		}
	}
}, {
	"$project" : {
		"values" : {
			"$arrayToObject" : "$periodValue"
		}
	}
} ]