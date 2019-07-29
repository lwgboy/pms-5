 {
	 "title": {
	        "text": "<标题>",
	        "textStyle": {
	            "fontSize": 14,
	            "fontWeight": 'bold'
	        },
	        "top":2,"left":2
	    },
    visualMap: {
        type: 'continuous',
        min: 0,
        max: 100,
        text:["高","低"],
        realtime: true,
        calculable : true,
        inRange: {
            color: ['#03a9f4','#ffeb3b', '#ff5722','#b0120a']
        }        },
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
			"repulsion" : 500,
			"edgeLength" : [20, 100]
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