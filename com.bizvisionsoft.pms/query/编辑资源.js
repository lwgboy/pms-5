[
		{
			"$group" : {
				"_id" : {
					"work_id" : "$work_id",
					"resTypeId" : "$resTypeId",
					"usedTypedResId" : "$usedTypedResId",
					"usedHumanResId" : "$usedHumanResId",
					"usedEquipResId" : "$usedEquipResId"
				}
			}
		},
		{
			"$unwind" : "$_id"
		},
		{
			"$addFields" : {
				"work_id" : "$_id.work_id",
				"resTypeId" : "$_id.resTypeId",
				"usedTypedResId" : "$_id.usedTypedResId",
				"usedHumanResId" : "$_id.usedHumanResId",
				"usedEquipResId" : "$_id.usedEquipResId"
			}
		},
		{
			"$lookup" : {
				"from" : "<resourceCollection>",
				"let" : {
					"work_id" : "$work_id",
					"resTypeId" : "$resTypeId",
					"usedTypedResId" : "$usedTypedResId",
					"usedHumanResId" : "$usedHumanResId",
					"usedEquipResId" : "$usedEquipResId"
				},
				"pipeline" : [ {
					"$match" : {
						"$expr" : {
							"$and" : [
									{
										"$eq" : [ "$work_id", "$$work_id" ]
									},
									{
										"$eq" : [ "$resTypeId", "$$resTypeId" ]
									},
									{
										"$eq" : [ "$usedTypedResId",
												"$$usedTypedResId" ]
									},
									{
										"$eq" : [ "$usedHumanResId",
												"$$usedHumanResId" ]
									},
									{
										"$eq" : [ "$usedEquipResId",
												"$$usedEquipResId" ]
									} ]
						}
					}
				} ],
				"as" : "resource"
			}
		},
		{
			"$match" : "<match>"
		},
		{
			"$project" : {
				"_id" : false
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
			"$unwind" : {
				"path" : "$user",
				"preserveNullAndEmptyArrays" : true
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
			"$unwind" : {
				"path" : "$equipment",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "resourceType",
				"localField" : "usedTypedResId",
				"foreignField" : "id",
				"as" : "resourceType"
			}
		},
		{
			"$unwind" : {
				"path" : "$resourceType",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$addFields" : {
				"type" : {
					"$cond" : [ "$user", "人力资源", {
						"$cond" : [ "$equipment", "设备设施", "资源类型" ]
					} ]
				},
				"name" : {
					"$cond" : [
							"$user.name",
							"$user.name",
							{
								"$cond" : [ "$equipment.name",
										"$equipment.name", "$resourceType.name" ]
							} ]
				},
				"resId" : {
					"$cond" : [
							"$usedHumanResId",
							"$usedHumanResId",
							{
								"$cond" : [ "$usedEquipResId",
										"$usedEquipResId", "$usedTypedResId" ]
							} ]
				}
			}
		}, {
			"$lookup" : {
				"from" : "resourceType",
				"localField" : "resTypeId",
				"foreignField" : "_id",
				"as" : "resType"
			}
		}, {
			"$unwind" : {
				"path" : "$resType",
				"preserveNullAndEmptyArrays" : true
			}
		}, {
			"$lookup" : {
				"from" : "work",
				"localField" : "work_id",
				"foreignField" : "_id",
				"as" : "work"
			}
		}, {
			"$unwind" : {
				"path" : "$work",
				"preserveNullAndEmptyArrays" : true
			}
		}, {
			"$lookup" : {
				"from" : "project",
				"localField" : "work.project_id",
				"foreignField" : "_id",
				"as" : "project"
			}
		}, {
			"$unwind" : {
				"path" : "$project",
				"preserveNullAndEmptyArrays" : true
			}
		}, {
			"$addFields" : {
				"workName" : "$work.name",
				"actualStart" : "$work.actualStart",
				"actualFinish" : "$work.actualFinish",
				"planStart" : "$work.planStart",
				"planFinish" : "$work.planFinish",
				"projectName" : "$project.name"
			}
		}, {
			"$project" : {
				"resourceType" : false,
				"resType_id" : false,
				"user" : false,
				"equipment" : false,
				"work" : false,
				"project" : false
			}
		}, {
			"$sort" : {
				"work_id" : 1,
				"type" : 1,
				"resId" : 1
			}
		} ]