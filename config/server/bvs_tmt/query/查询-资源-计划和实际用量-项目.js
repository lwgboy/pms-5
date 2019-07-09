[
		{
			"$match" : "<match>"
		},
		{
			"$match" : {
				"summary" : false
			}
		},
		{
			"$lookup" : {
				"from" : "resourceActual",
				"localField" : "_id",
				"foreignField" : "work_id",
				"as" : "resourceActual"
			}
		},
		{
			"$lookup" : {
				"from" : "resourcePlan",
				"localField" : "_id",
				"foreignField" : "work_id",
				"as" : "resourcePlan"
			}
		},
		{
			"$addFields" : {
				"resource" : {
					"$concatArrays" : [ "$resourceActual", "$resourcePlan" ]
				}
			}
		},
		{
			"$unwind" : "$resource"
		},
		{
			"$replaceRoot" : {
				"newRoot" : "$resource"
			}
		},
		{
			"$group" : {
				"_id" : {
					"resTypeId" : "$resTypeId",
					"usedTypedResId" : "$usedTypedResId",
					"usedHumanResId" : "$usedHumanResId",
					"usedEquipResId" : "$usedEquipResId",
					"id" : {
						"$dateToString" : {
							"format" : "%Y%m",
							"date" : "$id"
						}
					}
				},
				"actualBasicQty" : {
					"$sum" : {
						"$multiply" : [ "$actualBasicQty", {
							"$ifNull" : [ "$qty", 1 ]
						} ]
					}
				},
				"actualOverTimeQty" : {
					"$sum" : {
						"$multiply" : [ "$actualOverTimeQty", {
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
				},
				"planOverTimeQty" : {
					"$sum" : {
						"$multiply" : [ "$planOverTimeQty", {
							"$ifNull" : [ "$qty", 1 ]
						} ]
					}
				}
			}
		},
		{
			"$lookup" : {
				"from" : "resourceType",
				"localField" : "_id.resTypeId",
				"foreignField" : "_id",
				"as" : "resType"
			}
		},
		{
			"$unwind" : {
				"path" : "$resType",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$addFields" : {
				"resource" : {
					"id" : "$_id.id",
					"actualQty" : {
						"$sum" : [ "$actualBasicQty", "$actualOverTimeQty" ]
					},
					"planQty" : {
						"$sum" : [ "$planBasicQty", "$planOverTimeQty" ]
					},
					"actualBasicQty" : {
						"$sum" : "$actualBasicQty"
					},
					"actualOverTimeQty" : {
						"$sum" : "$actualOverTimeQty"
					},
					"planBasicQty" : {
						"$sum" : "$planBasicQty"
					},
					"planOverTimeQty" : {
						"$sum" : "$planOverTimeQty"
					},
					"planAmount" : {
						"$sum" : [ {
							"$multiply" : [ {
								"$sum" : "$planBasicQty"
							}, "$resType.basicRate" ]
						}, {
							"$multiply" : [ {
								"$sum" : "$planOverTimeQty"
							}, "$resType.overtimeRate" ]
						} ]
					},
					"actualAmount" : {
						"$sum" : [ {
							"$multiply" : [ {
								"$sum" : "$actualBasicQty"
							}, "$resType.basicRate" ]
						}, {
							"$multiply" : [ {
								"$sum" : "$actualOverTimeQty"
							}, "$resType.overtimeRate" ]
						} ]
					},
					"totalPlanQty" : {
						"$sum" : [ {
							"$sum" : "$planBasicQty"
						}, {
							"$sum" : "$planOverTimeQty"
						} ]
					},
					"totalActualQty" : {
						"$sum" : [ {
							"$sum" : "$actualBasicQty"
						}, {
							"$sum" : "$actualOverTimeQty"
						} ]
					},
				}
			}
		},
		{
			"$group" : {
				"_id" : {
					"resTypeId" : "$_id.resTypeId",
					"usedTypedResId" : "$_id.usedTypedResId",
					"usedHumanResId" : "$_id.usedHumanResId",
					"usedEquipResId" : "$_id.usedEquipResId"
				},
				"resource" : {
					"$addToSet" : "$resource"
				},
				"actualQty" : {
					"$sum" : "$resource.actualQty"
				},
				"planQty" : {
					"$sum" : "$resource.planQty"
				},
				"planAmount" : {
					"$sum" : "$resource.planAmount"
				},
				"actualAmount" : {
					"$sum" : "$resource.actualAmount"
				},
				"actualBasicQty" : {
					"$sum" : "$resource.actualBasicQty"
				},
				"actualOverTimeQty" : {
					"$sum" : "$resource.actualOverTimeQty"
				},
				"planBasicQty" : {
					"$sum" : "$resource.planBasicQty"
				},
				"planOverTimeQty" : {
					"$sum" : "$resource.planOverTimeQty"
				},
				"totalPlanQty" : {
					"$sum" : "$resource.totalPlanQty"
				},
				"totalActualQty" : {
					"$sum" : "$resource.totalActualQty"
				}
			}
		},
		{
			"$addFields" : {
				"resTypeId" : "$_id.resTypeId",
				"usedTypedResId" : "$_id.usedTypedResId",
				"usedHumanResId" : "$_id.usedHumanResId",
				"usedEquipResId" : "$_id.usedEquipResId"
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
			"$project" : {
				"_id" : false,
				"user" : false,
				"equipment" : false,
				"resourceType" : false
			}
		} ]