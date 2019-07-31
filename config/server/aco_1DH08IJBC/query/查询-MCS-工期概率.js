   [
        { 
            "$match" : {
                "project_id" : "<project_id>"
            }
        }, 
        { 
            "$unwind" : {
                "path" : "$Tdata", 
                "preserveNullAndEmptyArrays" : false
            }
        }, 
        { 
            "$addFields" : {
                "T" : {
                    "$arrayElemAt" : [
                        "$Tdata", 
                        0.0
                    ]
                }, 
                "P" : {
                    "$arrayElemAt" : [
                        "$Tdata", 
                        1.0
                    ]
                }
            }
        }, 
        { 
            "$match" : {
                "T" : {
                    "$lte" : "<T>"
                }
            }
        }, 
        { 
            "$group" : {
                "_id" : "$_id", 
                "prob" : {
                    "$sum" : "$P"
                }
            }
        }
    ]