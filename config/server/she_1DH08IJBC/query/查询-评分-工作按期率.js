    [
        { 
            "$match" : {"workType":{"$ne":null}}
        }, 
        { 
            "$match" : "<match>"
        }, 
        { 
            "$addFields" : {
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
                }
            }
        },
        { 
            "$group" : {
                "_id" : {
                    "catagory" : "$workType", 
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