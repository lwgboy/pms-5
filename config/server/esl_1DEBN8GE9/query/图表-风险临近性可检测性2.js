{
    title: {
        show:false
    },
    grid: {
        top: 64.0,
        bottom: 64.0,
        left: 64.0,
        right: 64.0
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
        },
        label: {
        	show:true,
        	position:"bottom",
        	formatter: "function (param) {  return param.data[3];     }"
        }
    }],
    toolbox: {
        show: true,
        feature: {
            saveAsImage: {}
        }
    }
}