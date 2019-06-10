   [
        { 
            "$match" : {
                "project_id" :"<project_id>"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "salesItem", 
                "localField" : "id", 
                "foreignField" : "productId", 
                "as" : "salesItem"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "product", 
                "localField" : "benchmarking_id", 
                "foreignField" : "_id", 
                "as" : "_bmproduct"
            }
        }, 
        { 
            "$unwind" : {
                "path" : "$_bmproduct"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "salesItem", 
                "localField" : "_bmproduct.id", 
                "foreignField" : "productId", 
                "as" : "bm_salesItem"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "product", 
                "localField" : "series", 
                "foreignField" : "series", 
                "as" : "_series"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "product", 
                "localField" : "_bmproduct.series", 
                "foreignField" : "series", 
                "as" : "bm_series"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "salesItem", 
                "localField" : "_series.id", 
                "foreignField" : "productId", 
                "as" : "seriesSalesItem"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "salesItem", 
                "localField" : "bm_series.id", 
                "foreignField" : "productId", 
                "as" : "bm_seriesSalesItem"
            }
        }, 
        { 
            "$addFields" : {
                "sum" : {
                    "$reduce" : {
                        "input" : "$salesItem", 
                        "initialValue" : {
                            "profit" : 0.0, 
                            "income" : 0.0, 
                            "volumn" : 0.0
                        }, 
                        "in" : {
                            "profit" : {
                                "$add" : [
                                    "$$value.profit", 
                                    "$$this.profit"
                                ]
                            }, 
                            "income" : {
                                "$add" : [
                                    "$$value.income", 
                                    "$$this.income"
                                ]
                            }, 
                            "volumn" : {
                                "$add" : [
                                    "$$value.volumn", 
                                    "$$this.volumn"
                                ]
                            }
                        }
                    }
                }, 
                "seriesSum" : {
                    "$reduce" : {
                        "input" : "$seriesSalesItem", 
                        "initialValue" : {
                            "profit" : 0.0, 
                            "income" : 0.0, 
                            "volumn" : 0.0
                        }, 
                        "in" : {
                            "profit" : {
                                "$add" : [
                                    "$$value.profit", 
                                    "$$this.profit"
                                ]
                            }, 
                            "income" : {
                                "$add" : [
                                    "$$value.income", 
                                    "$$this.income"
                                ]
                            }, 
                            "volumn" : {
                                "$add" : [
                                    "$$value.volumn", 
                                    "$$this.volumn"
                                ]
                            }
                        }
                    }
                }, 
                "bm_sum" : {
                    "$reduce" : {
                        "input" : "$bm_salesItem", 
                        "initialValue" : {
                            "profit" : 0.0, 
                            "income" : 0.0, 
                            "volumn" : 0.0
                        }, 
                        "in" : {
                            "profit" : {
                                "$add" : [
                                    "$$value.profit", 
                                    "$$this.profit"
                                ]
                            }, 
                            "income" : {
                                "$add" : [
                                    "$$value.income", 
                                    "$$this.income"
                                ]
                            }, 
                            "volumn" : {
                                "$add" : [
                                    "$$value.volumn", 
                                    "$$this.volumn"
                                ]
                            }
                        }
                    }
                }, 
                "bm_seriesSum" : {
                    "$reduce" : {
                        "input" : "$bm_seriesSalesItem", 
                        "initialValue" : {
                            "profit" : 0.0, 
                            "income" : 0.0, 
                            "volumn" : 0.0
                        }, 
                        "in" : {
                            "profit" : {
                                "$add" : [
                                    "$$value.profit", 
                                    "$$this.profit"
                                ]
                            }, 
                            "income" : {
                                "$add" : [
                                    "$$value.income", 
                                    "$$this.income"
                                ]
                            }, 
                            "volumn" : {
                                "$add" : [
                                    "$$value.volumn", 
                                    "$$this.volumn"
                                ]
                            }
                        }
                    }
                }
            }
        }, 
        { 
            "$lookup" : {
                "from" : "project", 
                "localField" : "project_id", 
                "foreignField" : "_id", 
                "as" : "project"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "project", 
                "localField" : "_bmproduct.project_id", 
                "foreignField" : "_id", 
                "as" : "bm_project"
            }
        }, 
        { 
            "$project" : {
                "projectId" : {
                    "$arrayElemAt" : [
                        "$project.id", 
                        0.0
                    ]
                }, 
                "projectName" : {
                    "$arrayElemAt" : [
                        "$project.name", 
                        0.0
                    ]
                }, 
                "series" : true, 
                "id" : true, 
                "name" : true, 
                "position" : true, 
                "sellPoint" : true, 
                "profit" : "$sum.profit", 
                "income" : "$sum.income", 
                "volumn" : "$sum.volumn", 
                "series_profit" : "$seriesSum.profit", 
                "series_income" : "$seriesSum.income", 
                "series_volumn" : "$seriesSum.volumn", 
                "ratioIncome" : {
                    "$divide" : [
                        "$sum.income", 
                        "$seriesSum.income"
                    ]
                }, 
                "ratioProfit" : {
                    "$divide" : [
                        "$sum.profit", 
                        "$seriesSum.profit"
                    ]
                }, 
                "price" : {
                    "$divide" : [
                        "$sum.income", 
                        "$sum.volumn"
                    ]
                }, 
                "bm_projectId" : {
                    "$arrayElemAt" : [
                        "$bm_project.id", 
                        0.0
                    ]
                }, 
                "bm_projectName" : {
                    "$arrayElemAt" : [
                        "$bm_project.name", 
                        0.0
                    ]
                }, 
                "bm_series" : "$_bmproduct.series", 
                "bm_id" : "$_bmproduct.id", 
                "bm_name" : "$_bmproduct.name", 
                "bm_sellPoint" : "$_bmproduct.sellPoint", 
                "bm_position" : "$_bmproduct.position", 
                "bm_profit" : "$bm_sum.profit", 
                "bm_income" : "$bm_sum.income", 
                "bm_volumn" : "$bm_sum.volumn", 
                "bm_price" : {
                    "$divide" : [
                        "$bm_sum.income", 
                        "$bm_sum.volumn"
                    ]
                }, 
                "bm_series_profit" : "$bm_seriesSum.profit", 
                "bm_series_income" : "$bm_seriesSum.income", 
                "bm_series_volumn" : "$bm_seriesSum.volumn", 
                "bm_ratioIncome" : {
                    "$divide" : [
                        "$bm_sum.income", 
                        "$bm_seriesSum.income"
                    ]
                }, 
                "bm_ratioProfit" : {
                    "$divide" : [
                        "$bm_sum.profit", 
                        "$bm_seriesSum.profit"
                    ]
                }
            }
        }
    ]