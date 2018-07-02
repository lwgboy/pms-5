 {
	  "title": {
		    "text": "工作按期完成率（%）",
		    "textStyle": {
		        "color": "#757575",
		        "fontFamily": "Microsoft YaHei",
		        "fontWeight": 400.0,
		        "fontSize": "14"
		      },
		    "padding": 8.0
		  },
		    legend: {
		        data: ["均值","项目"],
		    "orient": "horizontal",
		    "left": 12,
		    "top": 24
		    },
    tooltip: {},
    radar: {
        name: {
            textStyle: {
                color: "#fff",
                backgroundColor: "#999",
                borderRadius: 3,
                padding: [3, 5]
           }
        },
        indicator: "<indicator>"
    },
    series: [{
        type: "radar",
        name:"各类工作"
        areaStyle: {normal: {}},
        data : [
        	{ value : "<avg>",name: "均值" },
        	{ value : "<value>",name: "项目"}
        ]
    }],
    toolbox: {
        show: true,
        feature: {
            saveAsImage: {}
        }
    }
}