{
	"series": [
			{
				"name" : "<name1>",
				"type" : "gauge",
				"center" : [ "50%", "50%" ],
				"radius" : "65%",
				"min" : 0,
				"max" : 100,
				"axisLabel" : { "show" : false },
				"axisTick" : { "show" : false },
				"axisLine" : {
					"show" : true,
					"lineStyle" : {
						"width" : 4,
						"shadowBlur" : 0,
						"color" : [ [ 0.2, "#9CD6CE" ], [ 0.4, "#7CBB55" ], [ 0.6, "#DDBD4D" ], [ 0.8, "#E98E2C" ],[ 1, "#E43F3D" ] ]
					}
				},
				"splitLine" : {"length" : 4},
				"pointer" : { "width" : 20 },
				"detail" : false,
				"data" : [ {"value" : "<value1>","name" : "<name1>"} ]
			},
			{
				"type" : "pie",
				"animation" : false,
				"hoverAnimation" : false,
				"legendHoverLink" : false,
				"radius" : [ "0%", "40%" ],
				"z" : 10,
				"label" : {
					fontSize : 11,
					color : "#000",
					position : "center",
					padding : [24,0,0,0],
					rich: {
                         level0: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#9CD6CE"},
                         level1: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#7CBB55"},
                         level2: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#DDBD4D"},
                         level3: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#E98E2C"},
                         level4: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#E43F3D"}
	                    }
				},
				"emphasis" : {"show" : false},
				"labelLine" : { "normal" : {"show" : false }
				},
				"data" : [ {
					"value" : 100,
					"name" : "<title1>",
					"itemStyle" : {"color" : "#fff"}
				} ]
			},
			{
				name : "<name2>",
				center : [ "18%", "50%" ],
				radius : "65%",
				type : "gauge",
				min : 0,
				"max" : 100,
				axisLabel : {show : false},
				axisTick : {show : false},
				axisLine : {
					show : true,
					lineStyle : {
						width : 4,
						shadowBlur : 0,
						color : [ [ 0.2, "#9CD6CE" ], [ 0.4, "#7CBB55" ],
								[ 0.6, "#DDBD4D" ], [ 0.8, "#E98E2C" ],
								[ 1, "#E43F3D" ] ]
					}
				},
				splitLine : {length : 4},
				pointer : {width : 20},
				detail : false,
				data : [ {
					value : "<value2>",
					name : "<name2>"
				} ]

			},
			{
				center : [ "18%", "50%" ],
				"type" : "pie",
				"animation" : false,
				"hoverAnimation" : false,
				"legendHoverLink" : false,
				"radius" : [ "0%", "40%" ],
				"z" : 10,
				"label" : {
					fontSize : 11,
					color : "#000",
					position : "center",
					padding : [24,0,0,0],
					rich: {
                         level0: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#9CD6CE"},
                         level1: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#7CBB55"},
                         level2: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#DDBD4D"},
                         level3: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#E98E2C"},
                         level4: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#E43F3D"}
	                    }
				},
				"emphasis" : {"show" : false },
				"labelLine" : {
					"normal" : { "show" : false}
				},
				"data" : [ {
					"value" : 100,
					"name" : "<title2>",
					"itemStyle" : {
						"color" : "#fff"
					}
				} ]
			},
			{
				name : "<name3>",
				center : [ "82%", "50%" ],
				// startAngle: 180, endAngle: 0,
				type : "gauge",
				radius : "65%",
				min : 0,
				"max" : 100,
				axisLabel : { show : false },
				axisTick : { show : false },
				axisLine : {
					show : true,
					lineStyle : {
						width : 4,
						shadowBlur : 0,
						color : [ [ 0.2, "#9CD6CE" ], [ 0.4, "#7CBB55" ],
								[ 0.6, "#DDBD4D" ], [ 0.8, "#E98E2C" ],
								[ 1, "#E43F3D" ] ]
					}
				},
				splitLine : { length : 4 },
				pointer : { width : 20},
				detail : false,
				data : [ {
					value : "<value3>",
					name : "<name3>"
				} ]
			}, {
				center : [ "82%", "50%" ],
				"type" : "pie",
				"animation" : false,
				"hoverAnimation" : false,
				"legendHoverLink" : false,
				"radius" : [ "0%", "40%" ],
				"z" : 10,
				"label" : {
					fontSize : 11,
					color : "#000",
					position : "center",
					padding : [24,0,0,0],
					rich: {
                         level0: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#9CD6CE"},
                         level1: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#7CBB55"},
                         level2: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#DDBD4D"},
                         level3: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#E98E2C"},
                         level4: {fontSize:18,fontFamily: "Arial",color: "#fff",borderRadius: 4,padding: [4 8],backgroundColor: "#E43F3D"}
	                    }
				},
				"emphasis" : { "show" : false},
				"labelLine" : { "normal" : { "show" : false }},
				"data" : [ {
					"value" : 100,
					"name" : "<title3>",
					"itemStyle" : { "color" : "#fff"}
				} ]
			}

	]
}