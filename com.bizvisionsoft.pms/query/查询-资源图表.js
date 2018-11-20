[ {
	"$lookup" : {
		"from" : "resourceType",
		"localField" : "resTypeId",
		"foreignField" : "_id",
		"as" : "resourceType"
	}
}, {
	"$unwind" : "$resourceType"
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "usedHumanResId",
		"foreignField" : "userId",
		"as" : "user"
	}
}, {
	"$unwind" : {
		"path" : "$user",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "equipment",
		"localField" : "usedEquipResId",
		"foreignField" : "id",
		"as" : "equipment"
	}
}, {
	"$unwind" : {
		"path" : "$equipment",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"resource_id" : {
			"$ifNull" : [ "$equipment._id", {
				"$ifNull" : [ "$user._id", "$resourceType._id" ]
			} ]
		},
		"resourceId" : {
			"$ifNull" : [ "$equipment.id", {
				"$ifNull" : [ "$user.userId", "$resourceType.id" ]
			} ]
		},
		"resourceName" : {
			"$ifNull" : [ "$equipment.name", {
				"$ifNull" : [ "$user.name", "$resourceType.name" ]
			} ]
		},
		"basicQty" : {
			"$sum" : [ {
				"$multiply" : [ "<$basicQty>", {
					"$ifNull" : [ "$qty", 1.0 ]
				} ]
			} ]
		},
		"overTimeQty" : {
			"$sum" : [ {
				"$multiply" : [ "<$overTimeQty>", {
					"$ifNull" : [ "$qty", 1.0 ]
				} ]
			} ]
		},
		"totalQty" : {
			"$sum" : [ {
				"$multiply" : [ "<$basicQty>", {
					"$ifNull" : [ "$qty", 1.0 ]
				} ]
			}, {
				"$multiply" : [ "<$overTimeQty>", {
					"$ifNull" : [ "$qty", 1.0 ]
				} ]
			} ]
		},
		"basicAmount" : {
			"$sum" : [ {
				"$multiply" : [ {
					"$sum" : {
						"$multiply" : [ "<$basicQty>", {
							"$ifNull" : [ "$qty", 1.0 ]
						} ]
					}
				}, "$resourceType.basicRate" ]
			} ]
		},
		"overTimeAmount" : {
			"$sum" : [ {
				"$multiply" : [ {
					"$sum" : {
						"$multiply" : [ "<$overTimeQty>", {
							"$ifNull" : [ "$qty", 1.0 ]
						} ]
					}
				}, "$resourceType.overtimeRate" ]
			} ]
		},
		"totalAmount" : {
			"$sum" : [ {
				"$multiply" : [ {
					"$sum" : {
						"$multiply" : [ "<$basicQty>", {
							"$ifNull" : [ "$qty", 1.0 ]
						} ]
					}
				}, "$resourceType.basicRate" ]
			}, {
				"$multiply" : [ {
					"$sum" : {
						"$multiply" : [ "<$overTimeQty>", {
							"$ifNull" : [ "$qty", 1.0 ]
						} ]
					}
				}, "$resourceType.overtimeRate" ]
			} ]
		}
	}
}, {
	"$match" : "<match>"
}, {
	"$group" : {
		"_id" : "<group_id>",
		"basicQty" : {
			"$sum" : "$basicQty"
		},
		"overTimeQty" : {
			"$sum" : "$overTimeQty"
		},
		"totalQty" : {
			"$sum" : "$totalQty"
		},
		"basicAmount" : {
			"$sum" : "$basicAmount"
		},
		"overTimeAmount" : {
			"$sum" : "$overTimeAmount"
		},
		"totalAmount" : {
			"$sum" : "$totalAmount"
		}
	}
}, {
	"$sort" : {
		"_id" : 1
	}
} ]