package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.DepartmentManagerAssignRequest;
import com.achieveil.bizcore.dto.DepartmentManagerDTO;
import com.achieveil.bizcore.entity.DepartmentManager;
import com.achieveil.bizcore.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department-managers")
@RequiredArgsConstructor
public class DepartmentManagerController {

    private final DepartmentService departmentService;

    /**
     * 为部门设置经理（可设置多个）
     */
    @PostMapping("/departments/{deptId}/set-managers")
    public Result<Void> setDepartmentManagers(
            @PathVariable Long deptId,
            @RequestBody List<Long> managerIds,
            @RequestParam Long assignedBy) {
        try {
            departmentService.setDepartmentManagers(deptId, managerIds, assignedBy);
            return Result.success("部门经理设置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为部门设置单个经理
     */
    @PostMapping("/departments/{deptId}/manager")
    public Result<Void> setDepartmentManager(
            @PathVariable Long deptId,
            @RequestBody DepartmentManagerAssignRequest request) {
        try {
            departmentService.setDepartmentManager(deptId, request.getManagerId(), request.getAssignedBy());
            return Result.success("部门经理设置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为部门添加一个经理
     */
    @PostMapping("/departments/{deptId}/managers/{managerId}")
    public Result<DepartmentManager> addDepartmentManager(
            @PathVariable Long deptId,
            @PathVariable Long managerId,
            @RequestParam Long assignedBy) {
        try {
            DepartmentManager dm = departmentService.addDepartmentManager(deptId, managerId, assignedBy);
            return Result.success("添加部门经理成功", dm);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移除部门的某个经理
     */
    @DeleteMapping("/departments/{deptId}/managers/{managerId}")
    public Result<Void> removeDepartmentManager(
            @PathVariable Long deptId,
            @PathVariable Long managerId) {
        try {
            departmentService.removeDepartmentManager(deptId, managerId);
            return Result.success("移除部门经理成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取部门的所有经理
     */
    @GetMapping("/departments/{deptId}/managers")
    public Result<List<DepartmentManagerDTO>> getDepartmentManagers(@PathVariable Long deptId) {
        try {
            List<DepartmentManagerDTO> managers = departmentService.getDepartmentManagers(deptId);
            return Result.success(managers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取经理管理的所有部门
     */
    @GetMapping("/managers/{managerId}/departments")
    public Result<List<DepartmentManagerDTO>> getManagerDepartments(@PathVariable Long managerId) {
        try {
            List<DepartmentManagerDTO> depts = departmentService.getManagerDepartments(managerId);
            return Result.success(depts);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查某人是否是某部门的经理
     */
    @GetMapping("/departments/{deptId}/managers/{managerId}/check")
    public Result<Boolean> isManagerOfDepartment(
            @PathVariable Long deptId,
            @PathVariable Long managerId) {
        try {
            boolean isManager = departmentService.isManagerOfDepartment(deptId, managerId);
            return Result.success(isManager);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
