package com.achieveil.bizcore.service;

import com.achieveil.bizcore.dto.DepartmentAttendanceRuleDTO;
import com.achieveil.bizcore.entity.Department;
import com.achieveil.bizcore.entity.DepartmentAttendanceRule;
import com.achieveil.bizcore.repository.DepartmentAttendanceRuleRepository;
import com.achieveil.bizcore.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentAttendanceRuleService {

    private final DepartmentAttendanceRuleRepository ruleRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 创建或更新部门考勤规则
     */
    @Transactional
    public DepartmentAttendanceRule saveRule(DepartmentAttendanceRuleDTO dto, Long operatorId) {
        // 验证部门存在
        departmentRepository.findById(dto.getDeptId())
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        DepartmentAttendanceRule rule = ruleRepository.findByDeptId(dto.getDeptId())
                .orElse(new DepartmentAttendanceRule());

        rule.setDeptId(dto.getDeptId());
        rule.setCheckInTime(dto.getCheckInTime());
        rule.setCheckOutTime(dto.getCheckOutTime());
        rule.setLateGraceMinutes(dto.getLateGraceMinutes());
        rule.setEarlyLeaveGraceMinutes(dto.getEarlyLeaveGraceMinutes());
        rule.setRequireCheckOut(dto.getRequireCheckOut());
        rule.setFlexibleWorkTime(dto.getFlexibleWorkTime());
        rule.setFlexibleCheckInStart(dto.getFlexibleCheckInStart());
        rule.setFlexibleCheckInEnd(dto.getFlexibleCheckInEnd());
        rule.setStandardWorkHours(dto.getStandardWorkHours());
        rule.setRemark(dto.getRemark());

        if (rule.getId() == null) {
            rule.setCreatedBy(operatorId);
        }
        rule.setUpdatedBy(operatorId);

        return ruleRepository.save(rule);
    }

    /**
     * 获取部门考勤规则
     */
    public DepartmentAttendanceRuleDTO getRuleByDeptId(Long deptId) {
        DepartmentAttendanceRule rule = ruleRepository.findByDeptId(deptId)
                .orElseThrow(() -> new RuntimeException("该部门尚未设置考勤规则"));
        return convertToDTO(rule);
    }

    /**
     * 获取所有部门考勤规则
     */
    public List<DepartmentAttendanceRuleDTO> getAllRules() {
        List<DepartmentAttendanceRule> rules = ruleRepository.findAll();
        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 删除部门考勤规则
     */
    @Transactional
    public void deleteRule(Long deptId) {
        DepartmentAttendanceRule rule = ruleRepository.findByDeptId(deptId)
                .orElseThrow(() -> new RuntimeException("该部门尚未设置考勤规则"));
        ruleRepository.delete(rule);
    }

    /**
     * 转换为DTO
     */
    private DepartmentAttendanceRuleDTO convertToDTO(DepartmentAttendanceRule rule) {
        DepartmentAttendanceRuleDTO dto = new DepartmentAttendanceRuleDTO();
        dto.setId(rule.getId());
        dto.setDeptId(rule.getDeptId());
        dto.setCheckInTime(rule.getCheckInTime());
        dto.setCheckOutTime(rule.getCheckOutTime());
        dto.setLateGraceMinutes(rule.getLateGraceMinutes());
        dto.setEarlyLeaveGraceMinutes(rule.getEarlyLeaveGraceMinutes());
        dto.setRequireCheckOut(rule.getRequireCheckOut());
        dto.setFlexibleWorkTime(rule.getFlexibleWorkTime());
        dto.setFlexibleCheckInStart(rule.getFlexibleCheckInStart());
        dto.setFlexibleCheckInEnd(rule.getFlexibleCheckInEnd());
        dto.setStandardWorkHours(rule.getStandardWorkHours());
        dto.setRemark(rule.getRemark());

        // 获取部门名称
        Department department = departmentRepository.findById(rule.getDeptId()).orElse(null);
        if (department != null) {
            dto.setDeptName(department.getDeptName());
        }

        return dto;
    }
}
