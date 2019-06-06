[
		{
			"$match" : {
				"work_id" : "<work_id>"
			}
		},
		{
			"$lookup" : {
				"from" : "resourceType",
				"localField" : "resTypeId",
				"foreignField" : "_id",
				"as" : "resType"
			}
		},
		{
			"$lookup" : {
				"from" : "user",
				"localField" : "usedHumanResId",
				"foreignField" : "userId",
				"as" : "user"
			}
		},
		{
			"$lookup" : {
				"from" : "equipment",
				"localField" : "usedEquipResId",
				"foreignField" : "id",
				"as" : "equipment"
			}
		},
		{
			"$project" : {
				"type" : {
					"$arrayElemAt" : [ "$resType.type", 0 ]
				},
				"resId" : {
					"$cond" : [
							"$usedHumanResId",
							"$usedHumanResId",
							{
								"$cond" : [ "$usedEquipResId",
										"$usedEquipResId", "$usedTypedResId" ]
							} ]
				},
				"name" : {
					"$cond" : [ {
						"$arrayElemAt" : [ "$user.name", 0 ]
					}, {
						"$arrayElemAt" : [ "$user.name", 0 ]
					}, {
						"$cond" : [ {
							"$arrayElemAt" : [ "$equipment.name", 0 ]
						}, {
							"$arrayElemAt" : [ "$equipment.name", 0 ]
						}, {
							"$arrayElemAt" : [ "$resType.name", 0 ]
						} ]
					} ]
				},
				"usedHumanResId" : true,
				"usedEquipResId" : true,
				"usedTypedResId" : true,
				"work_id" : true,
				"resTypeId" : true
			}
		} ]