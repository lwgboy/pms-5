[ {
	"$match" : "<match>"
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "_id",
		"foreignField" : "projectSet_id",
		"as" : "projects"
	}
}, {
	"$addFields" : {
		"projectids" : "$projects._id"
	}
}, {
	"$lookup" : {
		"from" : "salesItem",
		"localField" : "projectids",
		"foreignField" : "project_id",
		"as" : "salesItems"
	}
}, {
	"$lookup" : {
		"from" : "work",
		"localField" : "projectids",
		"foreignField" : "project_id",
		"as" : "works"
	}
}, {
	"$addFields" : {
		"ids" : "$works._id"
	}
}, {
	"$addFields" : {
		"cbsscope" : {
			"$concatArrays" : [ "$ids", "$projectids" ]
		},
		"totalProfit" : {
			"$sum" : "$salesItems.profit"
		}
	}
}, {
	"$lookup" : {
		"from" : "cbs",
		"localField" : "cbsscope",
		"foreignField" : "scope_id",
		"as" : "cbs"
	}
}, {
	"$addFields" : {
		"ids" : "$cbs._id"
	}
}, {
	"$lookup" : {
		"from" : "cbsSubject",
		"localField" : "ids",
		"foreignField" : "cbsItem_id",
		"as" : "cbsSubjects"
	}
}, {
	"$addFields" : {
		"totalCost" : {
			"$sum" : "$cbsSubjects.cost"
		}
	}
}, {
	"$project" : {
		"projects" : false,
		"works" : false,
		"cbs" : false,
		"ids" : false,
		"projectids" : false,
		"cbsscope" : false
	}
} ]
