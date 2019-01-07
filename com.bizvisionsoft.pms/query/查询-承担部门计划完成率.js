[ {
	"$match" : "<orgmatch>"
}, {
	"$lookup" : {
		"from" : "project",
		"let" : {
			"impUnit_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$impUnit_id", "$$impUnit_id" ]
					} ]
				}
			}
		}, {
			"$match" : "<projectmatch>"
		}, {
			"$lookup" : {
				"from" : "work",
				"let" : {
					"project_id" : "$_id"
				},
				"pipeline" : [ {
					"$match" : {
						"$expr" : {
							"$and" : [ {
								"$eq" : [ "$project_id", "$$project_id" ]
							} ]
						}
					}
				}, {
					"$match" : {
						"manageLevel" : {
							"$in" : [ "1", "2" ]
						},
						"actualFinish" : {
							"$ne" : null
						}
					}
				}, {
					"$match" : "<workmatch>"
				}, {
					"$addFields" : {
						"plan" : {
							"$subtract" : [ "$planFinish", "$planStart" ]
						},
						"actual" : {
							"$subtract" : [ "$actualFinish", "$actualStart" ]
						}
					}
				}, {
					"$match" : {
						"actual" : {
							"$nin" : [ 0.0, null ]
						},
						"project_id" : ObjectId("5b432afefbef260ce8166cb9")
					}
				}, {
					"$addFields" : {
						"avg" : {
							"$cond" : [ {
								"$gt" : [ {
									"$divide" : [ "$plan", "$actual" ]
								}, 1.0 ]
							}, 1.0, {
								"$divide" : [ "$plan", "$actual" ]
							} ]
						}
					}
				}, {
					"$group" : {
						"_id" : {
							"project_id" : "$project_id",
							"manageLevel" : "$manageLevel",
							"stage" : "$stage",
							"month" : {
								"$dateToString" : {
									"format" : "%Y-%m",
									"date" : "$actualFinish"
								}
							}
						},
						"sar" : {
							"$avg" : "$avg"
						}
					}
				}, {
					"$addFields" : {
						"stageSar" : {
							"$cond" : [ "$_id.stage", "$sar", 0.0 ]
						},
						"sar1" : {
							"$cond" : [ {
								"$and" : [ {
									"$eq" : [ "$_id.manageLevel", "1" ]
								}, {
									"$not" : "$_id.stage"
								} ]
							}, "$sar", 0.0 ]
						},
						"sar2" : {
							"$cond" : [ {
								"$and" : [ {
									"$eq" : [ "$_id.manageLevel", "2" ]
								}, {
									"$not" : "$_id.stage"
								} ]
							}, "$sar", 0.0 ]
						}
					}
				}, {
					"$group" : {
						"_id" : {
							"_id" : "$_id.project_id",
							"month" : "$_id.month"
						},
						"stageSar" : {
							"$sum" : {
								"$multiply" : [ "$stageSar", 100 ]
							}
						},
						"sar1" : {
							"$sum" : {
								"$multiply" : [ "$sar1", 100 ]
							}
						},
						"sar2" : {
							"$sum" : {
								"$multiply" : [ "$sar2", 100 ]
							}
						}
					}
				}, {
					"$addFields" : {
						"_id" : "$_id._id",
						"month" : "$_id.month"
					}
				} ],
				"as" : "work"
			}
		}, {
			"$unwind" : "$work"
		}, {
			"$addFields" : {
				"month" : "$work.month",
				"stageSar" : "$work.stageSar",
				"sar1" : "$work.sar1",
				"sar2" : "$work.sar2"
			}
		}, {
			"$group" : {
				"_id" : {
					"_id" : "$impUnit_id",
					"month" : "$month"
				},
				"stageSar" : {
					"$avg" : "$stageSar"
				},
				"sar1" : {
					"$avg" : "$sar1"
				},
				"sar2" : {
					"$avg" : "$sar2"
				}
			}
		}, {
			"$addFields" : {
				"_id" : "$_id._id",
				"month" : "$_id.month"
			}
		} ],
		"as" : "project"
	}
}, {
	"$match" : {
		"qualifiedContractor" : true
	}
}, {
	"$sort" : {
		"_id" : 1.0
	}
} ]