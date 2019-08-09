{
	
    "tooltip": {
        "trigger": "axis"
    },
    "legend": {                        //数据源集合
        "data": "<legendData>",
        "type": "scroll",
        "bottom": "4",
    },
    "title": {
        "text": "<title>",
        "textStyle": {
            "fontSize": 14,
            "fontWeight": "bold"
        },
        "top":2,
        "left":2
    },
    "grid": [
        {
            "left": 35,
            "right": 35,
            "bottom": "20%",
            "containLabel": false
        },
//        {
//            "left": 96,
//            "right": 96,
//            "bottom": "10%",
//            "top": "50%",
//            "containLabel": false
//        }
    ],
    "dataZoom": [
        {
            "type": "slider",
            "xAxisIndex": [
                0
            ],
            "start": 0,
            "end": 100,
            "top": "80%",
        }
    ],
  //  "dataset": "<dataset>",
 // 声明一个 X 轴，类目轴（category）。默认情况下，类目轴对应到 dataset 第一列             横轴
    "xAxis": [
        {
            "type": "category",
            "data": "<xAxisData>"
        }
    ],
 // 声明一个 Y 轴，数值轴。 竖轴
    "yAxis":[{
        name: '<yAxis>',
        type: 'value',
    },
    {
        name: '<yAxis2>',
        type: 'value',
        splitLine: {show:false}
    }
    ] ,
    //"<yAxis>",
    "series": "<series>"//参数值
}