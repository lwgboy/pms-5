    [
        { 
            "$match" : "<match>"
        }, 
        { 
            "$lookup" : {
                "from" : "salesItem", 
                "localField" : "_id", 
                "foreignField" : "project_id", 
                "as" : "salesItems"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "work", 
                "localField" : "_id", 
                "foreignField" : "project_id", 
                "as" : "works"
            }
        }, 
        { 
            "$addFields" : {
                "ids" : "$works._id"
            }
        }, 
        { 
            "$addFields" : {
                "cbsscope" : {
                    "$concatArrays" : [
                        "$ids", 
                        [
                            "$_id"
                        ]
                    ]
                }, 
                "totalProfit" : {
                    "$sum" : "$salesItems.profit"
                }
            }
        }, 
        { 
            "$lookup" : {
                "from" : "cbs", 
                "localField" : "cbsscope", 
                "foreignField" : "scope_id", 
                "as" : "cbs"
            }
        }, 
        { 
            "$addFields" : {
                "ids" : "$cbs._id"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "cbsSubject", 
                "localField" : "ids", 
                "foreignField" : "cbsItem_id", 
                "as" : "cbsSubjects"
            }
        }, 
        { 
            "$addFields" : {
                "totalCost" : {
                    "$sum" : "$cbsSubjects.cost"
                }
            }
        }, 
        { 
            "$project" : {
                "projects" : false, 
                "works" : false, 
                "cbs" : false, 
                "ids" : false, 
                "projectids" : false, 
                "cbsscope" : false
            }
        }
    ]
