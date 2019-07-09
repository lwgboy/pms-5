{
    title: {
        text: '<标题>',
        textStyle: {
            fontSize: 14,
            fontWeight: 'bold'
        },
        top:2,left:2
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
    series: "<系列>"
}