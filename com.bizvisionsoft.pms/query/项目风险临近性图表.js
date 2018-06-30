{
    title: {
        text: '测试函数'
    },
   xAxis: {
        splitLine: {
            lineStyle: {
                type: 'dashed'
            }
        }
    },
    yAxis: {
        splitLine: {
            lineStyle: {
                type: 'dashed'
            }
        },
        scale: true
    },
    series: [{
        name: '1990',
        data: [[28604,77,17096869,'Australia'],[31163,77.4,27662440,'Canada'],[1516,68,1154605773,'China']],
        type: 'scatter',
        symbolSize: "function (data) {return Math.sqrt(data[2]) / 5e2;}",
        itemStyle: {
            normal: {
                shadowBlur: 10,
                shadowColor: 'rgba(120, 36, 50, 0.5)',
                shadowOffsetY: 5
            }
        }
    }]
}