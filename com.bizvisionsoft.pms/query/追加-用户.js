[ {
	"$lookup" : {
		"from" : "user",
		"let" : {
			"userId" : "<$chargerId>"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$eq" : [ "$$userId", "$userId" ]
				}
			}
		}, {
			"$project" : {
				"_id" : false,
				"userId" : true,
				"name" : true,
				"headPics" : true,
				"position" : true,
				"tel" : true,
				"org_id" : true,
				"email" : true
			}
		} ],
		"as" : "<chargerInfo_meta>"
	}
}, {
	"$unwind" : {
		"path" : "<$chargerInfo_meta>",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "organization",
		"localField" : "<chargerInfo_meta.org_id>",
		"foreignField" : "_id",
		"as" : "temp_Org"
	}
}, {
	"$addFields" : {
		"<chargerInfo>" : {"$cond":["<$chargerInfo_meta.name>","<$chargerInfo_meta.name>",""]},
		"<chargerInfo_meta.orgInfo>" : {
			"$arrayElemAt" : [ "$temp_Org.fullName", 0.0 ]
		}
	}
}, {
	"$project" : {
		"temp_Org" : false
	}
} ]