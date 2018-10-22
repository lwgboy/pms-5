[
		{
			"$match" : "<match>"
		},
		{
			"$lookup" : {
				"from" : "resourcePlan",
				"let" : {
					"usedHumanResId1" : "$usedHumanResId",
					"id1" : "$id"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [
											{
												"$eq" : [ "$usedHumanResId",
														"$$usedHumanResId1" ]
											}, {
												"$eq" : [ "$id", "$$id1" ]
											} ]
								}
							}
						}, {
							"$match" : {
								"usedHumanResId" : {
									"$ne" : null
								}
							}
						}, {
							"$project" : {
								"usedHumanResId" : true,
								"id" : true,
								"planWorks" : {
									"$sum" : [ {
										"$multiply" : [ "$planBasicQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
									}, {
										"$multiply" : [ "$planOverTimeQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
								}
							}
						}, {
							"$group" : {
								"_id" : {
									"usedHumanResId" : "$usedHumanResId",
									"id" : "$id"
								},
								"planWorks" : {
									"$sum" : "$planWorks"
								}
							}
						} ],
				"as" : "resourceHumanPlan"
			}
		},
		{
			"$unwind" : {
				"path" : "$resourceHumanPlan",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "resourcePlan",
				"let" : {
					"usedTypedResId1" : "$usedTypedResId",
					"id1" : "$id"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [
											{
												"$eq" : [ "$usedTypedResId",
														"$$usedTypedResId1" ]
											}, {
												"$eq" : [ "$id", "$$id1" ]
											} ]
								}
							}
						}, {
							"$match" : {
								"usedTypedResId" : {
									"$ne" : null
								}
							}
						}, {
							"$project" : {
								"usedTypedResId" : true,
								"id" : true,
								"planWorks" : {
									"$sum" : [ {
										"$multiply" : [ "$planBasicQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
									}, {
										"$multiply" : [ "$planOverTimeQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
								}
							}
						}, {
							"$group" : {
								"_id" : {
									"usedTypedResId" : "$usedTypedResId",
									"id" : "$id"
								},
								"planWorks" : {
									"$sum" : "$planWorks"
								}
							}
						} ],
				"as" : "resourceTypedPlan"
			}
		},
		{
			"$unwind" : {
				"path" : "$resourceTypedPlan",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "resourcePlan",
				"let" : {
					"usedEquipResId1" : "$usedEquipResId",
					"id1" : "$id"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [
											{
												"$eq" : [ "$usedEquipResId",
														"$$usedEquipResId1" ]
											}, {
												"$eq" : [ "$id", "$$id1" ]
											} ]
								}
							}
						}, {
							"$match" : {
								"usedEquipResId" : {
									"$ne" : null
								}
							}
						}, {
							"$project" : {
								"usedEquipResId" : true,
								"id" : true,
								"planWorks" : {
									"$sum" : [ {
										"$multiply" : [ "$planBasicQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
									}, {
										"$multiply" : [ "$planOverTimeQty", {
											"$ifNull" : [ "$qty", 1 ]
										} ]
								}
							}
						}, {
							"$group" : {
								"_id" : {
									"usedEquipResId" : "$usedEquipResId",
									"id" : "$id"
								},
								"planWorks" : {
									"$sum" : "$planWorks"
								}
							}
						} ],
				"as" : "resourceEquipPlan"
			}
		},
		{
			"$unwind" : {
				"path" : "$resourceEquipPlan",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$addFields" : {
				"conflict" : {
					"$gt" : [
							{
								"$sum" : [ "$resourceHumanPlan.planWorks",
										"$resourceTypedPlan.planWorks",
										"$resourceEquipPlan.planWorks" ]
							}, 8 ]
				}
			}
		},
		{
			"$sort" : {
				"conflict" : -1
			}
		},
		{
			"$group" : {
				"_id" : {
					"work_id" : "$work_id",
					"resTypeId" : "$resTypeId",
					"usedTypedResId" : "$usedTypedResId",
					"usedHumanResId" : "$usedHumanResId",
					"usedEquipResId" : "$usedEquipResId"
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
						}
				},
				"conflict" : {
					"$first" : "$conflict"
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