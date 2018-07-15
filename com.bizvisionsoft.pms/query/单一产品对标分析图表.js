
    {
        backgroundColor: '#FFFFFF',
        title: {
            text: "<title>",
            textStyle: {
                fontSize: 14
            }
        },

        tooltip: { 
            trigger: 'axis',
            axisPointer: { 
                type: 'shadow' 
            }
        },
        legend: {
            data: ['<prodname>', '<bmname>'],
            top: '18'
        },
        grid: {
            left: '3%',
            right: '5%',
            bottom: '3%',
            containLabel: true,
            show: false 
        },
        toolbox: {
            feature: {
                dataView: {
                    show: false,
                    readOnly: false
                }, 
                magicType: {show: true, type: ['stack', 'tiled']},
                restore: {show: true},
                saveAsImage: {
                    show: true
                }
            }
        },
        xAxis: {
            type: 'category',
            boundaryGap: true, 
            splitLine: { 
                show: false
            },
            data: "<month>"
        },

        yAxis: [ 
            {
                name: '<y>',
                type: 'value',
                splitLine: { 
                    show: false
                },
                axisLabel: {
                    formatter: '{value}'
                }
            }
        ],

        series: [{
                name: '<prodname>',
                type: 'bar',
                color: '#00BFFF',
                markPoint: {
                    data: [{
                            type: 'max',
                            name: '最大值'
                        },
                        {
                            type: 'min',
                            name: '最小值'
                        }
                    ]
                },
                markLine: {
                    data: [{
                        type: 'average',
                        name: '平均值'
                    }]
                },
                data: "<product>"
            },
            {
                name: '<bmname>',
                type: 'bar',
                color: '#DC143C',
                markPoint: {
                    data: [{
                            type: 'max',
                            name: '最大值'
                        },
                        {
                            type: 'min',
                            name: '最小值'
                        }
                    ]
                },
                markLine: {
                    data: [{
                        type: 'average',
                        name: '平均值'
                    }]
                },
                data: "<bm>"
            }
        ]
    }
    

