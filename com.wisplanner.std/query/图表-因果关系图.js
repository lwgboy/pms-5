 {
	 "tooltip": {
         "formatter": "function(params) {return params.name+'<br/>'+params.data.desc;}"
     },
     "legend" : [ {
		"tooltip" : {
			"show" : true
		},
		"selectedMode" : "true",
		"data" : "<categories>"
	} ],
	"toolbox" : {
		"show" : true,
		"feature" : {
			"dataView" : {
				"show" : true,
				"readOnly" : true
			},
			"restore" : {
				"show" : true
			},
			"saveAsImage" : {
				"show" : true
			}
		}
	},
	"animationDuration" : 1000,
	"animationEasingUpdate" : "quinticInOut",
	"series" : [ {
		"name" : "关系",
		"type" : "graph",
		"layout" : "force",
		"force" : {
			"repulsion" : 500,
			"edgeLength" : 120
		},
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
		}
	} ]
} 