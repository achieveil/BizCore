package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.DepartmentAttendanceRuleDTO;
import com.achieveil.bizcore.entity.DepartmentAttendanceRule;
import com.achieveil.bizcore.service.DepartmentAttendanceRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance-rules")
@RequiredArgsConstructor
public class DepartmentAttendanceRuleController {

    private final DepartmentAttendanceRuleService ruleService;

    /**
     * 创建或更新部门考勤规则
     */
    @PostMapping
    public Result<DepartmentAttendanceRule> saveRule(
            @Valid @RequestBody DepartmentAttendanceRuleDTO dto,
            @RequestParam Long operatorId) {
        try {
            DepartmentAttendanceRule rule = ruleService.saveRule(dto, operatorId);
            return Result.success("考勤规则设置成功", rule);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取部门考勤规则
     */
    @GetMapping("/department/{deptId}")
    public Result<DepartmentAttendanceRuleDTO> getRuleByDeptId(@PathVariable Long deptId) {
        try {
            DepartmentAttendanceRuleDTO rule = ruleService.getRuleByDeptId(deptId);
            return Result.success(rule);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有部门考勤规则
     */
    @GetMapping
    public Result<List<DepartmentAttendanceRuleDTO>> getAllRules() {
        try {
            List<DepartmentAttendanceRuleDTO> rules = ruleService.getAllRules();
            return Result.success(rules);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除部门考勤规则
     */
    @DeleteMapping("/department/{deptId}")
    public Result<Void> deleteRule(@PathVariable Long deptId) {
        try {
            ruleService.deleteRule(deptId);
            return Result.success("考勤规则已删除", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
