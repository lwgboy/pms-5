{
    title: {
        text: '项目风险可监测性和临近性分析',
        	textStyle: {
		        color: "#757575",
		        fontFamily: "Microsoft YaHei",
		        fontWeight: 400.0,
		        fontSize: "14"
		      },
		    padding: 8.0
    },
    tooltip: {
    	formatter: "function (param) { return param.data[3];}"
    },
    grid: {
        top: 32.0,
        bottom: 32.0,
        left: 32.0,
        right: 32.0
   },
   xAxis: {
        splitLine: {
            lineStyle: {
                type: 'dashed'
            }
        },
        nameLocation:"center",
        nameGap: 8,
        name:"临近性（近->远）",
        axisTick:{ show: false },
        axisLabel:{show:false}
    },
    yAxis: {
        splitLine: {
            lineStyle: {
                type: 'dashed'
            }
        },
        scale: true,
        nameLocation:"center",
        nameGap: 8,
        name:"可探测性（低->高）",
        axisTick:{ show: false }
        axisLabel:{show:false}
    },
    series: [{
        name: '风险',
        data: "<data>",
        type: 'scatter',
        symbolSize: "<size>",
        itemStyle: {
            normal: {
                shadowBlur: 10,
                shadowColor: 'rgba(120, 36, 50, 0.5)',
                shadowOffsetY: 5
            }
        }
    }],
    toolbox: {
        show: true,
        feature: {
            saveAsImage: {}
        }
    }
}