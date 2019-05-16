[ {
	"$match" : {
		"scope_id" : "<scope_id>"
	}
}, {
	"$group" : {
		"_id" : null,
		"start" : {
			"$min" : "$id"
		},
		"end" : {
			"$max" : "$id"
		}
	}
} ]