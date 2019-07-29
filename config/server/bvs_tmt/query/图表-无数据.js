{
    series: [
        {
            type: 'scatter',
            data: [[0,0]],
            symbolSize: 1,
            label: {
                normal: {
                    show: true,
                    formatter:'{term|没有可用数据}',
                    borderColor: 'rgb(199,86,83)',
                    borderWidth: 2,
                    borderRadius: 32,
                    padding: 16,
                    color: '#000',
                    rich: {
                        term: {
                            fontSize: 32,
                            color: 'rgb(199,86,83)'
                        }
                    }
                }
            }
        }
    ],
    xAxis: {
        axisLabel: {show: false},
        axisLine: {show: false},
        splitLine: {show: false},
        axisTick: {show: false},
        min: -1,
        max: 1
    },
    yAxis: {
        axisLabel: {show: false},
        axisLine: {show: false},
        splitLine: {show: false},
        axisTick: {show: false},
        min: -1,
        max: 1
    }
}