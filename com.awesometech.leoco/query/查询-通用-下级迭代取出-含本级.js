    [
        { 
            "$graphLookup" : {
                "from" : "<from>", 
                "startWith" : "<startWith>", 
                "connectFromField" : "<connectFromField>", 
                "connectToField" : "<connectToField>", 
                "as" : "_iterSub"
            }
        }, 
        { 
            "$addFields" : {
                "_iterSub" : {
                    "$concatArrays" : [
                        [
                            "$$ROOT"
                        ], 
                        "$_iterSub"
                    ]
                }
            }
        }, 
        { 
            "$unwind" : {
                "path" : "$_iterSub", 
                "preserveNullAndEmptyArrays" : true
            }
        }, 
        { 
            "$replaceRoot" : {
                "newRoot" : "$_iterSub"
            }
        }, 
        { 
            "$project" : {
                "_iterSub" : false
            }
        }
    ]