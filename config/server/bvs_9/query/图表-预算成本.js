{
	"title": [{
			"text": "<leftTitle>",
			"y": "1%",
			"x": "25%",
			"textAlign": "center",
			"textStyle": {
				"fontFamily": ["Microsoft YaHei", "Segoe UI", "Tahoma", "Lucida Sans Unicode", "sans-serif"],
				"fontSize": 14,
				"fontWeight": "normal"
				
			}
		},
		{
			"text": "<rightTitle>",
			"x": "75%",
			"y": "1%",
			"textAlign": "center",
			"textStyle": {
				"fontFamily": ["Microsoft YaHei", "Segoe UI", "Tahoma", "Lucida Sans Unicode", "sans-serif"],
				"fontSize": 14,
				"fontWeight": "normal"
				
			}
		}
	],
    "dataZoom": [
        {
            "type": "slider",
            "start": 50,
            "end": 100,
            "top": "90%",
        }
    ],
	"tooltip": {},
	"grid": {
		"left": "3%",
		"right": "4%",
		"bottom": "10%",
		"top": "<gridTop>"
		"containLabel": true,
		"tooltip": {
			"trigger": "axis"
		}
	},
	"xAxis": {
		"type": "category",
		"data": "<xAxisData>"
	},
	"legend": {
		"type": "scroll",
		"bottom": 20,
		"data": "<legendData>"
	},
	"yAxis": "<yAxis>",
	"series": "<series>"
}