[ {
	"$match" : {
		"_id" : "<selected_id>"
	}
}, {
	"$lookup" : {
		"from" : "organization",
		"localField" : "impUnit_id",
		"foreignField" : "_id",
		"as" : "organization"
	}
}, {
	"$unwind" : {
		"path" : "$organization",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"impUnitOrgFullName" : "$organization.name"
	}
}, {
	"$lookup" : {
		"from" : "eps",
		"localField" : "eps_id",
		"foreignField" : "_id",
		"as" : "eps"
	}
}, {
	"$unwind" : {
		"path" : "$eps",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"epsName" : "$eps.name"
	}
}, {
	"$lookup" : {
		"from" : "user",
		"let" : {
			"userId" : "$pmId"
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
		"as" : "pmInfo_meta"
	}
}, {
	"$unwind" : {
		"path" : "$pmInfo_meta",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "organization",
		"localField" : "pmInfo_meta.org_id",
		"foreignField" : "_id",
		"as" : "temp_Org"
	}
}, {
	"$addFields" : {
		"pmInfo" : {
			"$cond" : [ "$pmInfo_meta.name", "$pmInfo_meta.name", "" ]
		},
		"pmInfo_meta.orgInfo" : {
			"$arrayElemAt" : [ "$temp_Org.fullName", 0.0 ]
		}
	}
}, {
	"$project" : {
		"temp_Org" : false,
		"organization" : false,
		"eps" : false
	}
} ]