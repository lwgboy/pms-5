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
  grid: [{
	  top: 80,left:80,right:80,bottom:80
  }
],
toolbox: {  
    feature: {
        dataView: {show: false, readOnly: false},        // 数据试图是否在控件中显示
        //magicType: {show: true, type: ['stack', 'tiled']},
        //restore: {show: true},
        saveAsImage: {show: true}
    }
},
  legend:"<图例>",
  tooltip: {
        trigger: 'axis',
        axisPointer: {type: 'shadow'}
    },
    dataZoom: "<数据放缩>",
    dataset: "<数据集>",
    xAxis: {
            type: 'category',
		    name:'<x轴名称>',
            data: '<x轴数据>'
        },
    yAxis: {
    		name:'<y轴名称>',
            type: 'value'
        },
    series: "<系列>"
}
