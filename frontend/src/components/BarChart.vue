<template>
  <v-chart :option="option" :autoresize="true" style="height: 400px" />
</template>

<script setup>
import { ref, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  BarChart,
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
  data: {
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
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
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
    data: props.data.map(item => item.name),
    axisTick: {
      alignWithLabel: true
    }
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '数量',
      type: 'bar',
      barWidth: '60%',
      data: props.data.map(item => item.value),
      itemStyle: {
        color: '#1890ff',
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
}))
</script>
