{
    title: [{
        show: false
    }, {
        show: false

    }, {
        show: false

    }, {
        show: false

    }, {
        text: '销售额系列占比',
        textStyle: {
            fontSize: 14
        },
        left: '75%',
        bottom: '10%',
        textAlign: 'center'
    }],
    toolbox: {
        feature: {
            dataView: {
                show: false,
                readOnly: false
            },
            saveAsImage: {
                "show": true
            }
        }
    },
    backgroundColor: '#ffffff',
    tooltip: {
        trigger: 'axis'
    },
    grid: [{
        show: false,
        left: '3%',
        top: "5%",
        containLabel: true,
        width: '44%',
        height: '25%'
    }, {
        show: false,
        left: '53%',
        top: '5%',
        containLabel: true,
        width: '44%',
        height: '25%'
    }, {
        show: false,
        left: '3%',
        top: '35%',
        containLabel: true,
        width: '44%',
        height: '25%'
    }, {
        show: false,
        left: '3%',
        top: '66%',
        containLabel: true,
        width: '44%',
        height: '25%'
    }],
    legend: [{
        data: ["<产品>", "<对标产品>", "<系列>"],
        bottom: 18
    }],
    xAxis: [{
        gridIndex: 0,
        type: 'category',
        axisTick: {
            alignWithLabel: true
        },
        "data": "<月份>"
    }, {
        gridIndex: 1,
        type: 'category',
        axisTick: {
            alignWithLabel: true
        },
        "data": "<月份>"
    }, {
        gridIndex: 2,
        type: 'category',
        axisTick: {
            alignWithLabel: true
        },
        "data": "<月份>"
    }, {
        gridIndex: 3,
        type: 'category',
        axisTick: {
            alignWithLabel: true
        },
        "data": "<月份>"
    }],
    yAxis: [{
        gridIndex: 0,
        type: 'value',
        name: '销售额',
        nameLocation: 'center',
        nameGap: 30,
        nameTextStyle: {
            fontSize: 14
        },
        splitLine: {
            show: false
        }
    }, {
        gridIndex: 1,
        type: 'value',
        name: '销售量',
        nameLocation: 'center',
        nameGap: 30,
        nameTextStyle: {
            fontSize: 14
        },
        splitLine: {
            show: false
        }
    }, {
        gridIndex: 2,
        type: 'value',
        name: '销售毛利',
        nameLocation: 'center',
        nameGap: 30,
        nameTextStyle: {
            fontSize: 14
        },
        splitLine: {
            show: false
        }
    }, {
        gridIndex: 3,
        type: 'value',
        name: '销售增长率(%)',
        nameLocation: 'center',
        nameGap: 30,
        nameTextStyle: {
            fontSize: 14
        },
        splitLine: {
            show: false
        },
        position: 'left'
    }],
    series: [{
            name: '<产品>',
            type: 'bar',
            xAxisIndex: 0,
            yAxisIndex: 0,
            label: {
                normal: {
                    show: true,
                    position: 'top',

                }
            },
            markPoint: {
                data: [{
                        type: "max",
                        name: "最大值"
                    },
                    {
                        type: "min",
                        name: "最小值"
                    }
                ]
            },
            markLine: {
                data: [{
                    type: "average",
                    name: "平均值"
                }]
            },
            "data": "<产品销售额>"
        },

        {
            name: '<对标产品>',
            type: 'bar',
            xAxisIndex: 0,
            yAxisIndex: 0,
            label: {
                normal: {
                    show: true,
                    position: 'top',

                }
            },
            markPoint: {
                data: [{
                        type: "max",
                        name: "最大值"
                    },
                    {
                        type: "min",
                        name: "最小值"
                    }
                ]
            },
            markLine: {
                data: [{
                    type: "average",
                    name: "平均值"
                }]
            },
            "data":"<对标产品销售额>"
        },
        {
            name: '<产品>',
            type: 'bar',
            xAxisIndex: 1,
            yAxisIndex: 1,
            label: {
                normal: {
                    show: true,
                    position: 'top',
                }
            },
            markPoint: {
                data: [{
                        type: "max",
                        name: "最大值"
                    },
                    {
                        type: "min",
                        name: "最小值"
                    }
                ]
            },
            markLine: {
                data: [{
                    type: "average",
                    name: "平均值"
                }]
            },
            "data": "<产品销售量>"
        },
        {
            name: '<对标产品>',
            type: 'bar',
            xAxisIndex: 1,
            yAxisIndex: 1,
            label: {
                normal: {
                    show: true,
                    position: 'top',
                }
            },
            markPoint: {
                data: [{
                        type: "max",
                        name: "最大值"
                    },
                    {
                        type: "min",
                        name: "最小值"
                    }
                ]
            },
            markLine: {
                data: [{
                    type: "average",
                    name: "平均值"
                }]
            },
            "data": "<对标产品销售量>"
        },
        {
            name: '<产品>',
            type: 'bar',
            xAxisIndex: 2,
            yAxisIndex: 2,
            smooth: true,
            symbolSize: 8,
            label: {
                normal: {
                    show: true,
                    position: 'top',
                }
            },
            "data": "<产品毛利>"
        },
        {
            name: '<对标产品>',
            type: 'bar',
            xAxisIndex: 2,
            yAxisIndex: 2,
            smooth: true,
            symbolSize: 8,
            label: {
                normal: {
                    show: true,
                    position: 'top',
                }
            },
            "data": "<对标产品毛利>"
        },
        {
            name: '<产品>',
            type: 'line',
            xAxisIndex: 3,
            yAxisIndex: 3,
            smooth: true,
            symbolSize: 8,
            label: {
                normal: {
                    show: true,
                    position: 'top',
                }
            },
            "data": "<产品销售额环比增长>"
        },
        {
            name: '<对标产品>',
            type: 'line',
            xAxisIndex: 3,
            yAxisIndex: 3,
            smooth: true,
            symbolSize: 8,
            label: {
                normal: {
                    show: true,
                    position: 'top',
                }
            },
            "data": "<对标产品销售额环比增长>"
        },
        {
            name: '<系列>',
            type: 'line',
            xAxisIndex: 3,
            yAxisIndex: 3,
            smooth: true,
            symbolSize: 8,
            label: {
                normal: {
                    show: true,
                    position: 'top',
                }
            },
            "data": "<系列产品销售额环比增长>"
        },
        {
            name: '销售额系列占比',
            type: 'pie',
            avoidLabelOverlap: false,
            radius: '40%',
            center: ['75%', '65%'],
            selectedMode: 'single',
            label: {
                normal: {
                    show: true,
                    textStyle: {
                        fontSize: '14',
                    },
                    formatter: '{b} : {d}%',
                    position: 'outer'
                }
            },
            labelLine: {
                normal: {
                    show: true
                }
            },
            data: [{
                    name: '<产品>',
                    value: '<产品销售额占比>'
                },
                {
                    name: '<对标产品>',
                    value: '<对标产品销售额占比>'
                },
                {
                    name: '其他',
                    value: '<系列其他产品销售额占比>'
                }
            ],
            itemStyle: {
                emphasis: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }
    ]
}