package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.DepartmentVO;
import com.achieveil.bizcore.entity.Department;
import com.achieveil.bizcore.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public Result<Department> createDepartment(@RequestBody Department department) {
        try {
            Department created = departmentService.createDepartment(department);
            return Result.success("部门创建成功", created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        try {
            Department updated = departmentService.updateDepartment(id, department);
            return Result.success("部门信息更新成功", updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return Result.success("部门删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        try {
            Department department = departmentService.getDepartmentById(id);
            return Result.success(department);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<DepartmentVO>> getAllDepartments() {
        try {
            List<DepartmentVO> departments = departmentService.getAllDepartmentsWithDetails();
            return Result.success(departments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/active")
    public Result<List<Department>> getActiveDepartments() {
        try {
            List<Department> departments = departmentService.getActiveDepartments();
            return Result.success(departments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/parent/{parentId}")
    public Result<List<Department>> getDepartmentsByParentId(@PathVariable Long parentId) {
        try {
            List<Department> departments = departmentService.getDepartmentsByParentId(parentId);
            return Result.success(departments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
