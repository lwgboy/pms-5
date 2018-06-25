[
		{
			"$group" : {
				"_id" : {
					"work_id" : "$work_id",
					"resTypeId" : "$resTypeId",
					"usedTypedResId" : "$usedTypedResId",
					"usedHumanResId" : "$usedHumanResId",
					"usedEquipResId" : "$usedEquipResId",
					"workReportItemId" : "$workReportItemId"
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
				"usedEquipResId" : "$_id.usedEquipResId",
				"workReportItemId" : "$_id.workReportItemId"
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
					"usedEquipResId" : "$usedEquipResId",
					"workReportItemId" : "$workReportItemId"
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
									},
									{
										"$eq" : [ "$workReportItemId",
												"$$workReportItemId" ]
									} ]
						}
					}
				} ],
				"as" : "resource"
			}
		},
		{
			"$lookup" : {
				"from" : "resourcePlan",
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
				"as" : "resourcePlan"
			}
		},
		{
			"$lookup" : {
				"from" : "resourceActual",
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
				"as" : "resourceActual"
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
			"$unwind" : {
				"path" : "$resType",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "work",
				"localField" : "work_id",
				"foreignField" : "_id",
				"as" : "work"
			}
		},
		{
			"$unwind" : {
				"path" : "$work",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "project",
				"localField" : "work.project_id",
				"foreignField" : "_id",
				"as" : "project"
			}
		},
		{
			"$unwind" : {
				"path" : "$project",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "calendar",
				"localField" : "resType.cal_id",
				"foreignField" : "_id",
				"as" : "calendar"
			}
		},
		{
			"$unwind" : {
				"path" : "$calendar",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$addFields" : {
				"workName" : "$work.name",
				"actualStart" : "$work.actualStart",
				"actualFinish" : "$work.actualFinish",
				"planStart" : "$work.planStart",
				"planFinish" : "$work.planFinish",
				"projectName" : "$project.name",
				"basicWorks" : "$calendar.basicWorks",
				"overTimeWorks" : "$calendar.overTimeWorks"
			}
		},
		{
			"$lookup" : {
				"from" : "<resourceCollection>",
				"let" : {
					"planStart" : "$planStart",
					"planFinish" : "$planFinish",
					"resTypeId" : "$resTypeId",
					"usedTypedResId" : "$usedTypedResId",
					"usedHumanResId" : "$usedHumanResId",
					"usedEquipResId" : "$usedEquipResId",
					"basicWorks" : "$basicWorks",
					"overTimeWorks" : "$overTimeWorks"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [
											{
												"$gte" : [ "$id", "$$planStart" ]
											},
											{
												"$lte" : [ "$id",
														"$$planFinish" ]
											},
											{
												"$eq" : [ "$resTypeId",
														"$$resTypeId" ]
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
						},
						{
							"$group" : {
								"_id" : "$id",
								"planBasicQty" : {
									"$sum" : "$planBasicQty"
								},
								"planOverTimeQty" : {
									"$sum" : "$planOverTimeQty"
								},
								"actualBasicQty" : {
									"$sum" : "$actualBasicQty"
								},
								"actualOverTimeQty" : {
									"$sum" : "$actualOverTimeQty"
								}
							}
						},
						{
							"$match" : {
								"$expr" : {
									"$or" : [
											{
												"$gt" : [ "$planBasicQty",
														"$$basicWorks" ]
											},
											{
												"$gt" : [ "$planOverTimeQty",
														"$$overTimeWorks" ]
											},
											{
												"$gt" : [ "$actualBasicQty",
														"$$basicWorks" ]
											},
											{
												"$gt" : [ "$actualOverTimeQty",
														"$$overTimeWorks" ]
											} ]
								}
							}
						} ],
				"as" : "conflict"
			}
		}, {
			"$addFields" : {
				"basicWorks" : "$calendar.basicWorks",
				"overTimeWorks" : "$calendar.overTimeWorks",
				"planBasicQty" : {
					"$sum" : "$resourcePlan.planBasicQty"
				},
				"planOverTimeQty" : {
					"$sum" : "$resourcePlan.planOverTimeQty"
				},
				"actualBasicQty" : {
					"$sum" : "$resourceActual.actualBasicQty"
				},
				"actualOverTimeQty" : {
					"$sum" : "$resourceActual.actualOverTimeQty"
				},
				"overtimeRate" : "$resType.overtimeRate",
				"basicRate" : "$resType.basicRate",
				"planAmount" : {
					"$sum" : [ {
						"$multiply" : [ {
							"$sum" : "$resourcePlan.planBasicQty"
						}, "$resType.basicRate" ]
					}, {
						"$multiply" : [ {
							"$sum" : "$resourcePlan.planOverTimeQty"
						}, "$resType.overtimeRate" ]
					} ]
				},
				"actualAmount" : {
					"$sum" : [ {
						"$multiply" : [ {
							"$sum" : "$resourceActual.actualBasicQty"
						}, "$resType.basicRate" ]
					}, {
						"$multiply" : [ {
							"$sum" : "$resourceActual.actualOverTimeQty"
						}, "$resType.overtimeRate" ]
					} ]
				},
				"conflict" : {
					"$gt" : [ {
						"$size" : "$conflict"
					}, 0 ]
				}
			}
		}, {
			"$project" : {
				"resourceType" : false,
				"resType_id" : false,
				"user" : false,
				"equipment" : false,
				"work" : false,
				"project" : false,
				"resourcePlan" : false,
				"resourceActual" : false
			}
		}, {
			"$sort" : {
				"work_id" : 1,
				"type" : 1,
				"resId" : 1
			}
		} ]