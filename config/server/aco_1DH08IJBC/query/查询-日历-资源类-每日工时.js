[ {
	"$lookup" : {
		"from" : "resourceType",
		"localField" : "_id",
		"foreignField" : "cal_id",
		"as" : "resourceType"
	}
}, {
	"$unwind" : "$resourceType"
}, {
	"$addFields" : {
		"resTypeId" : "$resourceType._id"
	}
}, {
	"$project" : {
		"resourceType" : false
	}
}, {
	"$match" : {
		"resTypeId" : "<resTypeId>"
	}
}, {
	"$project" : {
		"basicWorks" : true,
		"overTimeWorks" : true
	}
} ]