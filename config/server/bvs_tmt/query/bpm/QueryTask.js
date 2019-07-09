[
		{
			"$match" : {
				"archived" : 0,
				"taskData.workItemId" : "<workItemId>"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_I18NText",
				"let" : {
					"var" : "$names"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
							}
						} ],
				"as" : "names"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_I18NText",
				"let" : {
					"var" : "$subjects"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
							}
						} ],
				"as" : "subjects"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_I18NText",
				"let" : {
					"var" : "$descriptions"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
							}
						} ],
				"as" : "descriptions"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$taskData.actualOwner_id"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$eq" : [ "$_id", "$$var" ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "taskData.actualOwner"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$taskData.createdBy_id"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$eq" : [ "$_id", "$$var" ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "taskData.createdBy"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$peopleAssignments.taskInitiator_id"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$eq" : [ "$_id", "$$var" ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "peopleAssignments.taskInitiator"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$peopleAssignments.businessAdministrators"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "peopleAssignments.businessAdministrators"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$peopleAssignments.potentialOwners"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "peopleAssignments.potentialOwners"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$peopleAssignments.excludedOwners"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "peopleAssignments.excludedOwners"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$peopleAssignments.taskStakeholders"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "peopleAssignments.taskStakeholders"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$peopleAssignments.recipients"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "peopleAssignments.recipients"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_Deadline",
				"let" : {
					"var1" : "$deadlines.startDeadlines"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var1", {
										"$in" : [ "$_id", "$$var1" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : "org.jbpm.services.task.impl.model.DeadlineImpl"
							}
						},
						{
							"$lookup" : {
								"from" : "bpm_Escalation",
								"let" : {
									"var2" : "$escalations"
								},
								"pipeline" : [
										{
											"$match" : {
												"$expr" : {
													"$and" : [
															"$$var2",
															{
																"$in" : [
																		"$_id",
																		"$$var2" ]
															} ]
												}
											}
										},
										{
											"$addFields" : {
												"DTYPE" : "org.jbpm.services.task.impl.model.EscalationImpl"
											}
										},
										{
											"$lookup" : {
												"from" : "bpm_BooleanExpression",
												"let" : {
													"var3" : "$constraints"
												},
												"pipeline" : [
														{
															"$match" : {
																"$expr" : {
																	"$and" : [
																			"$$var3",
																			{
																				"$in" : [
																						"$_id",
																						"$$var3" ]
																			} ]
																}
															}
														},
														{
															"$addFields" : {
																"DTYPE" : "org.jbpm.services.task.impl.model.BooleanExpressionImpl"
															}
														} ],
												"as" : "constraints"
											}
										},
										{
											"$lookup" : {
												"from" : "bpm_Notification",
												"let" : {
													"var4" : "$notifications"
												},
												"pipeline" : [
														{
															"$match" : {
																"$expr" : {
																	"$and" : [
																			"$$var4",
																			{
																				"$in" : [
																						"$_id",
																						"$$var4" ]
																			} ]
																}
															}
														},
														{
															"$addFields" : {
																"DTYPE" : "org.jbpm.services.task.impl.model.NotificationImpl"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var5" : "$documentation"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var5",
																							{
																								"$in" : [
																										"$_id",
																										"$$var5" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "documentation"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var6" : "$names"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var6",
																							{
																								"$in" : [
																										"$_id",
																										"$$var6" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "names"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var7" : "$subjects"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var7",
																							{
																								"$in" : [
																										"$_id",
																										"$$var7" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "subjects"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var8" : "$descriptions"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var8",
																							{
																								"$in" : [
																										"$_id",
																										"$$var8" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "descriptions"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_OrganizationalEntity",
																"let" : {
																	"var9" : "$recipients"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var9",
																							{
																								"$in" : [
																										"$_id",
																										"$$var9" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : {
																					"$cond" : [
																							{
																								"$eq" : [
																										"$DTYPE",
																										"User" ]
																							},
																							"org.jbpm.services.task.impl.model.UserImpl",
																							"org.jbpm.services.task.impl.model.GroupImpl" ]
																				}
																			}
																		} ],
																"as" : "recipients"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_OrganizationalEntity",
																"let" : {
																	"var10" : "$businessAdministrators"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var10",
																							{
																								"$in" : [
																										"$_id",
																										"$$var10" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : {
																					"$cond" : [
																							{
																								"$eq" : [
																										"$DTYPE",
																										"User" ]
																							},
																							"org.jbpm.services.task.impl.model.UserImpl",
																							"org.jbpm.services.task.impl.model.GroupImpl" ]
																				}
																			}
																		} ],
																"as" : "businessAdministrators"
															}
														} ],
												"as" : "notifications"
											}
										},
										{
											"$lookup" : {
												"from" : "bpm_Reassignment",
												"let" : {
													"var11" : "$reassignments"
												},
												"pipeline" : [
														{
															"$match" : {
																"$expr" : {
																	"$and" : [
																			"$$var11",
																			{
																				"$in" : [
																						"$_id",
																						"$$var11" ]
																			} ]
																}
															}
														},
														{
															"$addFields" : {
																"DTYPE" : "org.jbpm.services.task.impl.model.ReassignmentImpl"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var12" : "$documentation"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var12",
																							{
																								"$in" : [
																										"$_id",
																										"$$var12" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "documentation"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_OrganizationalEntity",
																"let" : {
																	"var13" : "$potentialOwners"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var13",
																							{
																								"$in" : [
																										"$_id",
																										"$$var13" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : {
																					"$cond" : [
																							{
																								"$eq" : [
																										"$DTYPE",
																										"User" ]
																							},
																							"org.jbpm.services.task.impl.model.UserImpl",
																							"org.jbpm.services.task.impl.model.GroupImpl" ]
																				}
																			}
																		} ],
																"as" : "potentialOwners"
															}
														} ],
												"as" : "reassignments"
											}
										} ],
								"as" : "escalations"
							}
						},
						{
							"$lookup" : {
								"from" : "bpm_I18NText",
								"let" : {
									"var14" : "$documentation"
								},
								"pipeline" : [
										{
											"$match" : {
												"$expr" : {
													"$and" : [
															"$$var14",
															{
																"$in" : [
																		"$_id",
																		"$$var14" ]
															} ]
												}
											}
										},
										{
											"$addFields" : {
												"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
											}
										} ],
								"as" : "documentation"
							}
						} ],
				"as" : "deadlines.startDeadlines"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_Deadline",
				"let" : {
					"var1" : "$deadlines.endDeadlines"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var1", {
										"$in" : [ "$_id", "$$var1" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : "org.jbpm.services.task.impl.model.DeadlineImpl"
							}
						},
						{
							"$lookup" : {
								"from" : "bpm_Escalation",
								"let" : {
									"var2" : "$escalations"
								},
								"pipeline" : [
										{
											"$match" : {
												"$expr" : {
													"$and" : [
															"$$var2",
															{
																"$in" : [
																		"$_id",
																		"$$var2" ]
															} ]
												}
											}
										},
										{
											"$addFields" : {
												"DTYPE" : "org.jbpm.services.task.impl.model.EscalationImpl"
											}
										},
										{
											"$lookup" : {
												"from" : "bpm_BooleanExpression",
												"let" : {
													"var3" : "$constraints"
												},
												"pipeline" : [
														{
															"$match" : {
																"$expr" : {
																	"$and" : [
																			"$$var3",
																			{
																				"$in" : [
																						"$_id",
																						"$$var3" ]
																			} ]
																}
															}
														},
														{
															"$addFields" : {
																"DTYPE" : "org.jbpm.services.task.impl.model.BooleanExpressionImpl"
															}
														} ],
												"as" : "constraints"
											}
										},
										{
											"$lookup" : {
												"from" : "bpm_Notification",
												"let" : {
													"var4" : "$notifications"
												},
												"pipeline" : [
														{
															"$match" : {
																"$expr" : {
																	"$and" : [
																			"$$var4",
																			{
																				"$in" : [
																						"$_id",
																						"$$var4" ]
																			} ]
																}
															}
														},
														{
															"$addFields" : {
																"DTYPE" : "org.jbpm.services.task.impl.model.NotificationImpl"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var5" : "$documentation"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var5",
																							{
																								"$in" : [
																										"$_id",
																										"$$var5" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "documentation"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var6" : "$names"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var6",
																							{
																								"$in" : [
																										"$_id",
																										"$$var6" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "names"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var7" : "$subjects"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var7",
																							{
																								"$in" : [
																										"$_id",
																										"$$var7" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "subjects"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var8" : "$descriptions"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var8",
																							{
																								"$in" : [
																										"$_id",
																										"$$var8" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "descriptions"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_OrganizationalEntity",
																"let" : {
																	"var9" : "$recipients"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var9",
																							{
																								"$in" : [
																										"$_id",
																										"$$var9" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : {
																					"$cond" : [
																							{
																								"$eq" : [
																										"$DTYPE",
																										"User" ]
																							},
																							"org.jbpm.services.task.impl.model.UserImpl",
																							"org.jbpm.services.task.impl.model.GroupImpl" ]
																				}
																			}
																		} ],
																"as" : "recipients"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_OrganizationalEntity",
																"let" : {
																	"var10" : "$businessAdministrators"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var10",
																							{
																								"$in" : [
																										"$_id",
																										"$$var10" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : {
																					"$cond" : [
																							{
																								"$eq" : [
																										"$DTYPE",
																										"User" ]
																							},
																							"org.jbpm.services.task.impl.model.UserImpl",
																							"org.jbpm.services.task.impl.model.GroupImpl" ]
																				}
																			}
																		} ],
																"as" : "businessAdministrators"
															}
														} ],
												"as" : "notifications"
											}
										},
										{
											"$lookup" : {
												"from" : "bpm_Reassignment",
												"let" : {
													"var11" : "$reassignments"
												},
												"pipeline" : [
														{
															"$match" : {
																"$expr" : {
																	"$and" : [
																			"$$var11",
																			{
																				"$in" : [
																						"$_id",
																						"$$var11" ]
																			} ]
																}
															}
														},
														{
															"$addFields" : {
																"DTYPE" : "org.jbpm.services.task.impl.model.ReassignmentImpl"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_I18NText",
																"let" : {
																	"var12" : "$documentation"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var12",
																							{
																								"$in" : [
																										"$_id",
																										"$$var12" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
																			}
																		} ],
																"as" : "documentation"
															}
														},
														{
															"$lookup" : {
																"from" : "bpm_OrganizationalEntity",
																"let" : {
																	"var13" : "$potentialOwners"
																},
																"pipeline" : [
																		{
																			"$match" : {
																				"$expr" : {
																					"$and" : [
																							"$$var13",
																							{
																								"$in" : [
																										"$_id",
																										"$$var13" ]
																							} ]
																				}
																			}
																		},
																		{
																			"$addFields" : {
																				"DTYPE" : {
																					"$cond" : [
																							{
																								"$eq" : [
																										"$DTYPE",
																										"User" ]
																							},
																							"org.jbpm.services.task.impl.model.UserImpl",
																							"org.jbpm.services.task.impl.model.GroupImpl" ]
																				}
																			}
																		} ],
																"as" : "potentialOwners"
															}
														} ],
												"as" : "reassignments"
											}
										} ],
								"as" : "escalations"
							}
						},
						{
							"$lookup" : {
								"from" : "bpm_I18NText",
								"let" : {
									"var14" : "$documentation"
								},
								"pipeline" : [
										{
											"$match" : {
												"$expr" : {
													"$and" : [
															"$$var14",
															{
																"$in" : [
																		"$_id",
																		"$$var14" ]
															} ]
												}
											}
										},
										{
											"$addFields" : {
												"DTYPE" : "org.jbpm.services.task.impl.model.I18NTextImpl"
											}
										} ],
								"as" : "documentation"
							}
						} ],
				"as" : "deadlines.endDeadlines"
			}
		},
		{
			"$lookup" : {
				"from" : "bpm_OrganizationalEntity",
				"let" : {
					"var" : "$delegation.delegates"
				},
				"pipeline" : [
						{
							"$match" : {
								"$expr" : {
									"$and" : [ "$$var", {
										"$in" : [ "$_id", "$$var" ]
									} ]
								}
							}
						},
						{
							"$addFields" : {
								"DTYPE" : {
									"$cond" : [
											{
												"$eq" : [ "$DTYPE", "User" ]
											},
											"org.jbpm.services.task.impl.model.UserImpl",
											"org.jbpm.services.task.impl.model.GroupImpl" ]
								}
							}
						} ],
				"as" : "delegation.delegates"
			}
		}, {
			"$addFields" : {
				"taskData.actualOwner" : {
					"$arrayElemAt" : [ "$taskData.actualOwner", 0 ]
				},
				"taskData.createdBy" : {
					"$arrayElemAt" : [ "$taskData.createdBy", 0 ]
				},
				"peopleAssignments.taskInitiator" : {
					"$arrayElemAt" : [ "$peopleAssignments.taskInitiator", 0 ]
				}
			}
		} ]