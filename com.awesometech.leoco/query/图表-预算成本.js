{
	"title": [{
			"text": "<title1>",
			"y": "1%",
			"x": "25%",
			"textAlign": "center"
		},
		{
			"text": "<title2>",
			"x": "75%",
			"y": "1%",
			"textAlign": "center"
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