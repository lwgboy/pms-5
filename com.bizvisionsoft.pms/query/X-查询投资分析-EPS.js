    [
        { 
            "$match" : "<match>"
        }, 
        { 
            "$lookup" : {
                "from" : "projectSet", 
                "localField" : "_id", 
                "foreignField" : "eps_id", 
                "as" : "projectSet"
            }
        }, 
        { 
            "$addFields" : {
                "projectSetids" : "$projectSet._id"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "project", 
                "let" : {"projectSet_id":"$projectSetids"}, 
                "pipeline" : [{"$match":{"$expr":{"$and":[{"$in":["$projectSet_id","$$projectSet_id"]}]}}}], 
                "as" : "projects2"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "project", 
                "localField" : "_id", 
                "foreignField" : "eps_id", 
                "as" : "projects1"
            }
        }, 
        { 
            "$addFields" : {
                "projectids" : {
                    "$concatArrays" : [
                        "$projects1._id", 
                         "$projects2._id"
                    ]
                }
            }
        }, 
        { 
            "$lookup" : {
                "from" : "salesItem", 
                "localField" : "projectids", 
                "foreignField" : "project_id", 
                "as" : "salesItems"
            }
        }, 
        { 
            "$lookup" : {
                "from" : "work", 
                "localField" : "projectids", 
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
                        "$projectids"
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
                "projectSet" : false, 
                "projectSetids" : false, 
                "projects1" : false, 
                "projects2" : false, 
                "works" : false, 
                "cbs" : false, 
                "ids" : false, 
                "projectids" : false, 
                "cbsscope" : false
            }
        }
    ]
