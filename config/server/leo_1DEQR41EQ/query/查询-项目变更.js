[ {
	"$match" : "<match>"
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "project_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$unwind" : "$project"
}, {
	"$addFields" : {
		"projectName" : "$project.name",
		"projectNumber" : "$project.id",
		"projectPMId" : "$project.pmId"
	}
}, {
	"$match" : {

	}
}, {
	"$lookup" : {
		"from" : "organization",
		"localField" : "applicantUnitId",
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
		"applicantUnit" : "$organization.fullName",
		"managerId" : "$applicantUnitId.managerId"
	}
}, {
	"$project" : {
		"organization" : false,
		"project" : false
	}
} ]