{
    title: {
        text: '<标题>',
        textStyle: {
            fontSize: 14,
            fontWeight: 'bold'
        },
        top:2,left:2
    },
    grid: {
        left: 8,
        right: 8,
        bottom: 32,
        top:64,
        containLabel: true,
    },
    toolbox: {
        feature: {
            dataView: {
                show: false,
                readOnly: false
            }, 
            saveAsImage: {
                show: true
            }
        },
        top:2,right:2
    },
    legend: {type:'scroll',bottom:4},
    tooltip: {
        trigger: 'axis',
        axisPointer: {
            type: 'shadow'
        }
    },
    dataset: "<数据集>",
    xAxis: {
        type: 'category',
        data: '<x轴数据>'
    },
    yAxis: [{
        name: '<y1轴名称>',
        type: 'value',
    },
    {
        name: '<y2轴名称>',
        type: 'value',
        splitLine: {show:false}
    }
    ],
    series: "<系列>"
}