 { 
            "$match" : {
                "_id" : "<work_id>"
            }
        }, 
        { 
            "$addFields" : {
                "stageCode" : {
                    "$arrayElemAt" : [
                        {
                            "$split" : [
                                "$wbsCode", 
                                "."
                            ]
                        }, 
                        0.0
                    ]
                }
            }
        }, 
        { 
            "$lookup" : {
                "from" : "work", 
                "localField" : "stageCode", 
                "foreignField" : "wbsCode", 
                "as" : "stage"
            }
        }, 
        { 
            "$replaceRoot" : {
                "newRoot" : {
                    "$arrayElemAt" : [
                        "$stage", 
                        0.0
                    ]
                }
            }
        }
    ]