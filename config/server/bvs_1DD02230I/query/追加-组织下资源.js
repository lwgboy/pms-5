[ {
	"$lookup" : {
		"from" : "user",
		"let" : {
			"org_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$org_id", "$$org_id" ]
					}, "$resourceType_id" ]
				}
			}
		} ],
		"as" : "hrRes"
	}
}, {
	"$lookup" : {
		"from" : "equipment",
		"let" : {
			"org_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$org_id", "$$org_id" ]
					}, "$resourceType_id" ]
				}
			}
		} ],
		"as" : "eqRes"
	}
}, {
	"$addFields" : {
		"res" : {
			"$concatArrays" : [ "$hrRes", "$eqRes" ]
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