<template>
  <v-chart :option="option" :autoresize="true" style="height: 400px" />
</template>

<script setup>
import { ref, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
])

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  xData: {
    type: Array,
    required: true
  },
  yData: {
    type: Array,
    required: true
  }
})

const option = computed(() => ({
  title: {
    text: props.title,
    left: 'center',
    top: 20
  },
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: 80,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: props.xData
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '入职人数',
      type: 'line',
      data: props.yData,
      smooth: true,
      itemStyle: {
        color: '#52c41a'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(82, 196, 26, 0.5)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.1)' }
          ]
        }
      }
    }
  ]
}))
</script>
