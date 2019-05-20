[
		{
			"$match" : "<match>"
		},
		{
			"$group" : {
				"_id" : {
					"work_id" : "$work_id",
					"usedEquipResId" : "$usedEquipResId",
					"usedHumanResId" : "$usedHumanResId",
					"usedTypedResId" : "$usedTypedResId",
					"resTypeId" : "$resTypeId"
				},
				"actualOverTimeQty" : {
					"$sum" : {
						"$multiply" : [ "$actualOverTimeQty", {
							"$ifNull" : [ "$qty", 1 ]
						} ]
					}
				},
				"actualBasicQty" : {
					"$sum" : {
						"$multiply" : [ "$actualBasicQty", {
							"$ifNull" : [ "$qty", 1 ]
						} ]
					}
				}
			}
		},
		{
			"$addFields" : {
				"work_id" : "$_id.work_id",
				"usedEquipResId" : "$_id.usedEquipResId",
				"usedHumanResId" : "$_id.usedHumanResId",
				"usedTypedResId" : "$_id.usedTypedResId",
				"resTypeId" : "$_id.resTypeId"
			}
		},
		{
			"$project" : {
				"_id" : false
			}
		},
		{
			"$lookup" : {
				"from" : "resourcePlan",
				"let" : {
					"work_id1" : "$work_id",
					"usedEquipResId1" : "$usedEquipResId",
					"usedHumanResId1" : "$usedHumanResId",
					"usedTypedResId1" : "$usedTypedResId",
					"resTypeId1" : "$resTypeId"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [
											{
												"$eq" : [ "$work_id",
														"$$work_id1" ]
											},
											{
												"$eq" : [ "$usedEquipResId",
														"$$usedEquipResId1" ]
											},
											{
												"$eq" : [ "$usedHumanResId",
														"$$usedHumanResId1" ]
											},
											{
												"$eq" : [ "$usedTypedResId",
														"$$usedTypedResId1" ]
											},
											{
												"$eq" : [ "$resTypeId",
														"$$resTypeId1" ]
											} ]
								}
							}
						}, {
							"$group" : {
								"_id" : {
									"work_id" : "$work_id",
									"usedEquipResId" : "$usedEquipResId",
									"usedHumanResId" : "$usedHumanResId",
									"usedTypedResId" : "$usedTypedResId",
									"resTypeId" : "$resTypeId"
								},
								"planOverTimeQty" : {
									"$sum" : {
										"$multiply" : [ "$planOverTimeQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
									}
								},
								"planBasicQty" : {
									"$sum" : {
										"$multiply" : [ "$planBasicQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
									}
								}
							}
						} ],
				"as" : "resourcePlan"
			}
		},
		{
			"$unwind" : {
				"path" : "$resourcePlan",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$addFields" : {
				"planOverTimeQty" : "$resourcePlan.planOverTimeQty",
				"planBasicQty" : "$resourcePlan.planBasicQty"
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
			"$project" : {
				"resourceType" : false,
				"resType_id" : false,
				"user" : false,
				"equipment" : false
			}
		}, {
			"$sort" : {
				"type" : 1,
				"resId" : 1
			}
		} ]