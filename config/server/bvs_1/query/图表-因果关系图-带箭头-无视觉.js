 {
	 "title": {
	        "text": "<标题>",
	        "textStyle": {
	            "fontSize": 14,
	            "fontWeight": 'bold'
	        },
	        "top":2,"left":2
	    },
     "legend" : [ {
    	"type":"scroll",
    	 "bottom":2,
		"tooltip" : {
			"show" : true
		},
		"selectedMode" : "true",
		"data" : "<categories>"
	} ],
    "toolbox": {
        "feature": {
            "dataView": {
                "show": false,
                "readOnly": false
            }, 
            "saveAsImage": {
                "show": true
            }
        },
        "top":2,"right":2
    },
	"animationDuration" : 1000,
	"animationEasingUpdate" : "quinticInOut",
	"series" : [ {
		"name" : "关系",
		"type" : "graph",
		"layout" : "force",
		"force" : {
			"repulsion" : 400,
			"edgeLength" : [100, 200]
		},
		"edgeSymbol":["none","arrow"],
		"data" : "<data>",
		"links" : "<links>",
		"categories" : "<categories>",
		"focusNodeAdjacency" : true,
		"roam" : true,
		"label" : {
			"normal" : {
				"show" : true,
				"position" : "top",
			}
		},
		"lineStyle" : {
			"normal" : {
				"color" : "source",
				"curveness" : 0,
				"type" : "solid"
			}
		},
		"itemStyle": {
			"opacity": 1
		}
	} ]
} 