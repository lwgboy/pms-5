    [
        { 
            "$match" : "<match>"
        }, 
        { 
            "$project" : {
                "_overdue" : {
                    "$cond" : [
                        {
                            "$gt" : [
                                {
                                    "$cond" : [
                                        "$actualFinish", 
                                        {
                                            "$divide" : [
                                                {
                                                    "$subtract" : [
                                                        "$actualFinish", 
                                                        "$actualStart"
                                                    ]
                                                }, 
                                                86400000.0
                                            ]
                                        }, 
                                        {
                                            "$divide" : [
                                                {
                                                    "$subtract" : [
                                                        "<now>", 
                                                        "$actualStart"
                                                    ]
                                                }, 
                                                86400000.0
                                            ]
                                        }
                                    ]
                                }, 
                                {
                                    "$divide" : [
                                        {
                                            "$subtract" : [
                                                "$planFinish", 
                                                "$planStart"
                                            ]
                                        }, 
                                        86400000.0
                                    ]
                                }
                            ]
                        }, 
                        true, 
                        false
                    ]
                }, 
                "_catagory" : {
                    "$cond" : [
                        "$workPackageSetting.catagory", 
                        "$workPackageSetting.catagory", 
                        "其他"
                    ]
                }
            }
        }, 
        { 
            "$unwind" : {
                "path" : "$_catagory", 
                "preserveNullAndEmptyArrays" : true
            }
        }, 
        { 
            "$group" : {
                "_id" : {
                    "catagory" : "$_catagory", 
                    "overdue" : "$_overdue"
                }, 
                "sum" : {
                    "$sum" : 1.0
                }
            }
        }, 
        { 
            "$project" : {
                "_id" : "$_id.catagory", 
                "overdue" : "$_id.overdue", 
                "_sum" : "$sum"
            }
        }, 
        { 
            "$sort" : {
                "_id" : 1.0, 
                "overdue" : -1.0
            }
        }, 
        { 
            "$group" : {
                "_id" : "$_id", 
                "total" : {
                    "$sum" : "$_sum"
                }, 
                "count" : {
                    "$first" : "$_sum"
                }
            }
        }, 
        { 
            "$project" : {
                "score" : {
                    "$divide" : [
                        "$count", 
                        "$total"
                    ]
                }
            }
        }
    ]