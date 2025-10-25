<template>
  <div class="attendance-rule-settings">
    <a-card title="考勤规则设置" :bordered="false">
      <a-form
        :model="ruleForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 12 }"
        @finish="handleSave"
      >
        <a-form-item label="上班时间" required>
          <a-time-picker
            v-model:value="ruleForm.checkInTime"
            format="HH:mm"
            :minute-step="15"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="下班时间" required>
          <a-time-picker
            v-model:value="ruleForm.checkOutTime"
            format="HH:mm"
            :minute-step="15"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="迟到宽限（分钟）">
          <a-input-number
            v-model:value="ruleForm.lateGraceMinutes"
            :min="0"
            :max="60"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="早退宽限（分钟）">
          <a-input-number
            v-model:value="ruleForm.earlyLeaveGraceMinutes"
            :min="0"
            :max="60"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="是否需要签退">
          <a-switch v-model:checked="ruleForm.requireCheckOut" />
        </a-form-item>

        <a-form-item label="弹性工作时间">
          <a-switch v-model:checked="ruleForm.flexibleWorkTime" />
        </a-form-item>

        <a-form-item
          v-if="ruleForm.flexibleWorkTime"
          label="弹性签到开始"
        >
          <a-time-picker
            v-model:value="ruleForm.flexibleCheckInStart"
            format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item
          v-if="ruleForm.flexibleWorkTime"
          label="弹性签到结束"
        >
          <a-time-picker
            v-model:value="ruleForm.flexibleCheckInEnd"
            format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="标准工作时长（小时）">
          <a-input-number
            v-model:value="ruleForm.standardWorkHours"
            :min="0"
            :max="24"
            :step="0.5"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="备注">
          <a-textarea
            v-model:value="ruleForm.remark"
            :rows="3"
            placeholder="规则说明"
          />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 6, span: 12 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="saving">
              保存规则
            </a-button>
            <a-button @click="loadRule">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { attendanceRuleApi } from '../api/attendanceRule'
import dayjs from 'dayjs'

const saving = ref(false)
const user = ref(null)
const deptId = ref(null)

const ruleForm = reactive({
  deptId: null,
  checkInTime: null,
  checkOutTime: null,
  lateGraceMinutes: 10,
  earlyLeaveGraceMinutes: 10,
  requireCheckOut: true,
  flexibleWorkTime: false,
  flexibleCheckInStart: null,
  flexibleCheckInEnd: null,
  standardWorkHours: 8.0,
  remark: ''
})

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    deptId.value = 1 // 临时硬编码
  }
  loadRule()
})

const loadRule = async () => {
  try {
    const res = await attendanceRuleApi.getByDepartment(deptId.value)
    const rule = res.data
    Object.assign(ruleForm, {
      ...rule,
      checkInTime: rule.checkInTime ? dayjs(rule.checkInTime, 'HH:mm:ss') : null,
      checkOutTime: rule.checkOutTime ? dayjs(rule.checkOutTime, 'HH:mm:ss') : null,
      flexibleCheckInStart: rule.flexibleCheckInStart ? dayjs(rule.flexibleCheckInStart, 'HH:mm:ss') : null,
      flexibleCheckInEnd: rule.flexibleCheckInEnd ? dayjs(rule.flexibleCheckInEnd, 'HH:mm:ss') : null
    })
  } catch (error) {
    console.log('未找到规则，使用默认值')
  }
}

const handleSave = async () => {
  if (!ruleForm.checkInTime || !ruleForm.checkOutTime) {
    message.warning('请设置上下班时间')
    return
  }

  saving.value = true
  try {
    const data = {
      deptId: deptId.value,
      checkInTime: ruleForm.checkInTime.format('HH:mm:ss'),
      checkOutTime: ruleForm.checkOutTime.format('HH:mm:ss'),
      lateGraceMinutes: ruleForm.lateGraceMinutes,
      earlyLeaveGraceMinutes: ruleForm.earlyLeaveGraceMinutes,
      requireCheckOut: ruleForm.requireCheckOut,
      flexibleWorkTime: ruleForm.flexibleWorkTime,
      flexibleCheckInStart: ruleForm.flexibleCheckInStart ? ruleForm.flexibleCheckInStart.format('HH:mm:ss') : null,
      flexibleCheckInEnd: ruleForm.flexibleCheckInEnd ? ruleForm.flexibleCheckInEnd.format('HH:mm:ss') : null,
      standardWorkHours: ruleForm.standardWorkHours,
      remark: ruleForm.remark
    }

    await attendanceRuleApi.save(data, user.value.userId)
    message.success('考勤规则保存成功！')
  } catch (error) {
    message.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.attendance-rule-settings {
  padding: 0;
}
</style>
