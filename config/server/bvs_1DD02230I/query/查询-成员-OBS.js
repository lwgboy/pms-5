[ {
	"$match" : "<match>"
}, {
	"$addFields" : {
		"userId" : {
			"$concatArrays" : [ [ {
				"$ifNull" : [ "$managerId", null ]
			} ], {
				"$ifNull" : [ "$member", [

				] ]
			} ]
		}
	}
}, {
	"$unwind" : {
		"path" : "$userId"
	}
}, {
	"$match" : {
		"userId" : {
			"$ne" : null
		}
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "userId",
		"foreignField" : "userId",
		"as" : "user"
	}
}, {
	"$project" : {
		"name" : true,
		"role" : {
			"$cond" : [ {
				"$eq" : [ "$userId", "$managerId" ]
			}, "$roleName", null ]
		},
		"roleId" : {
			"$cond" : [ {
				"$eq" : [ "$userId", "$managerId" ]
			}, "$roleId", null ]
		},
		"userId" : true,
		"userName" : {
			"$arrayElemAt" : [ "$user.name", 0.0 ]
		}
	}
}, {
	"$group" : {
		"_id" : {
			"name" : "$name",
			"role" : "$role",
			"roleId" : "$roleId",
			"userId" : "$userId",
			"userName" : "$userName"
		}
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$_id"
	}
}, {
	"$match" : "<filter>"
} ]