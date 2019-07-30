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
            "$unwind" : {
                "path" : "$_iterSub"
            }
        }, 
        { 
            "$replaceRoot" : {
                "newRoot" : "$_iterSub"
            }
        }
    ]