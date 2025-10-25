package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.CreateEmployeeWithAccountRequest;
import com.achieveil.bizcore.dto.EmployeeAccountRequest;
import com.achieveil.bizcore.entity.Employee;
import com.achieveil.bizcore.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public Result<Employee> createEmployee(@RequestBody Employee employee) {
        try {
            Employee created = employeeService.createEmployee(employee);
            return Result.success("员工创建成功", created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        try {
            Employee updated = employeeService.updateEmployee(id, employee);
            return Result.success("员工信息更新成功", updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return Result.success("员工删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Employee> getEmployeeById(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            return Result.success(employee);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            return Result.success(employees);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<Page<Employee>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Employee> employees = employeeService.searchEmployees(keyword, deptId, status, page, size);
            return Result.success(employees);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/department/{deptId}")
    public Result<List<Employee>> getEmployeesByDeptId(@PathVariable Long deptId) {
        try {
            List<Employee> employees = employeeService.getEmployeesByDeptId(deptId);
            return Result.success(employees);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", employeeService.getTotalCount());
            stats.put("active", employeeService.getActiveEmployeeCount());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics/gender")
    public Result<?> getGenderStatistics() {
        try {
            return Result.success(employeeService.getGenderStatistics());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics/department")
    public Result<?> getDepartmentStatistics() {
        try {
            return Result.success(employeeService.getDepartmentStatistics());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics/salary")
    public Result<?> getSalaryStatistics() {
        try {
            return Result.success(employeeService.getSalaryStatistics());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics/hire-trend")
    public Result<?> getHireTrendStatistics() {
        try {
            return Result.success(employeeService.getHireTrendStatistics());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建员工并自动创建用户账户
     */
    @PostMapping("/with-account")
    public Result<Employee> createEmployeeWithAccount(@RequestBody CreateEmployeeWithAccountRequest request) {
        try {
            if (request.getEmployee() == null) {
                return Result.error("员工信息不能为空");
            }
            Employee created = employeeService.createEmployeeWithAccount(
                    request.getEmployee(),
                    request.getPassword(),
                    request.getSecurityQuestion(),
                    request.getSecurityAnswer());
            return Result.success("员工和账户创建成功", created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 变更员工角色
     */
    @PutMapping("/{empId}/role")
    public Result<Void> changeEmployeeRole(
            @PathVariable Long empId,
            @RequestParam String newRole) {
        try {
            employeeService.changeEmployeeRole(empId, newRole);
            return Result.success("员工角色变更成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有经理
     */
    @GetMapping("/managers")
    public Result<List<Employee>> getAllManagers() {
        try {
            List<Employee> managers = employeeService.getAllManagers();
            return Result.success(managers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据userId获取员工信息
     */
    @GetMapping("/by-user/{userId}")
    public Result<Employee> getEmployeeByUserId(@PathVariable Long userId) {
        try {
            Employee employee = employeeService.getEmployeeByUserId(userId);
            if (employee == null) {
                return Result.error("未找到员工信息");
            }
            return Result.success(employee);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为员工注册账号
     */
    @PostMapping("/{id}/register-account")
    public Result<Employee> registerAccount(@PathVariable Long id, @RequestBody EmployeeAccountRequest request) {
        try {
            if (request == null) {
                return Result.error("请求体不能为空");
            }
            Employee employee = employeeService.registerEmployeeAccount(
                    id,
                    request.getPassword(),
                    request.getSecurityQuestion(),
                    request.getSecurityAnswer());
            return Result.success("账号注册成功", employee);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置员工账号密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        try {
            String newPassword = request != null ? request.get("password") : null;
            employeeService.resetEmployeePassword(id, newPassword);
            return Result.success("密码重置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
