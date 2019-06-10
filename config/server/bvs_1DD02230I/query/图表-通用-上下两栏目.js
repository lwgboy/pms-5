{
	 title: {
	        text:'<标题>',
	        textStyle:{
	            fontSize:18,
	            fontWeight:'bold'
	        },
	        top :10,
	        x :'center'
	},
	legend:"<图例>",
    dataZoom: "<数据放缩>",
    tooltip: {
        trigger: 'axis',
        axisPointer: {type: 'shadow'}
    },
    toolbox: {  
        feature: {
            dataView: {show: false, readOnly: false},        // 数据试图是否在控件中显示
            //magicType: {show: true, type: ['stack', 'tiled']},
            //restore: {show: true},
            saveAsImage: {show: true}
        }
    },
    dataset: "<数据集>",
    xAxis: [{
	        type: 'category',
	        gridIndex: 0,
	        boundaryGap: true,  
    		name:'<x轴名称>',
	        data: '<x轴数据>'
	    },
	    {
	        type: 'category',
	        gridIndex: 1,
	        boundaryGap: true,  
	        axisLabel:{
	        	rotate:45,
	        }
	    }
    ]
    yAxis: [{
    		name:'<y轴名称>',
            gridIndex: 0
        },
        {
    		name:'<y轴名称>',
            gridIndex: 1
        }
    ],
    grid: [{
            top: '55%',left:80,right:80,bottom:100
        },
        {
        	bottom: '55%',left:80,right:'40%',top: 80
        }
    ],
    series: "<系列>"
}