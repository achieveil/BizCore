package com.achieveil.bizcore.service;

import com.achieveil.bizcore.dto.DepartmentManagerDTO;
import com.achieveil.bizcore.dto.DepartmentVO;
import com.achieveil.bizcore.entity.Department;
import com.achieveil.bizcore.entity.DepartmentManager;
import com.achieveil.bizcore.entity.Employee;
import com.achieveil.bizcore.repository.DepartmentManagerRepository;
import com.achieveil.bizcore.repository.DepartmentRepository;
import com.achieveil.bizcore.repository.EmployeeRepository;
import com.achieveil.bizcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentManagerRepository departmentManagerRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByDeptCode(department.getDeptCode())) {
            throw new RuntimeException("部门编码已存在");
        }
        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(Long id, Department department) {
        Department existingDept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        if (!existingDept.getDeptCode().equals(department.getDeptCode())
            && departmentRepository.existsByDeptCode(department.getDeptCode())) {
            throw new RuntimeException("部门编码已存在");
        }

        existingDept.setDeptName(department.getDeptName());
        existingDept.setDeptCode(department.getDeptCode());
        existingDept.setDescription(department.getDescription());
        existingDept.setParentId(department.getParentId());
        existingDept.setStatus(department.getStatus());

        return departmentRepository.save(existingDept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("部门不存在");
        }
        departmentRepository.deleteById(id);
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * 获取所有部门（包含经理和员工统计信息）
     */
    public List<DepartmentVO> getAllDepartmentsWithDetails() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<Department> getActiveDepartments() {
        return departmentRepository.findByStatus(1);
    }

    public List<Department> getDepartmentsByParentId(Long parentId) {
        return departmentRepository.findByParentId(parentId);
    }

    /**
     * 为部门设置经理（可以设置多个）
     */
    @Transactional
    public void setDepartmentManagers(Long deptId, List<Long> managerIds, Long assignedBy) {
        // 验证部门存在
        if (!departmentRepository.existsById(deptId)) {
            throw new RuntimeException("部门不存在");
        }

        List<DepartmentManager> existingManagers = departmentManagerRepository.findByDeptId(deptId);
        Set<Long> existingManagerIds = existingManagers.stream()
                .map(DepartmentManager::getManagerId)
                .collect(Collectors.toSet());

        Set<Long> newManagerIds = managerIds != null
                ? new HashSet<>(managerIds)
                : Collections.emptySet();

        // 清除部门的所有经理
        departmentManagerRepository.deleteByDeptId(deptId);

        // 添加新的经理
        for (Long managerId : newManagerIds) {
            // 验证员工存在
            Employee employee = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("员工ID " + managerId + " 不存在"));

            DepartmentManager dm = new DepartmentManager();
            dm.setDeptId(deptId);
            dm.setManagerId(managerId);
            dm.setAssignedBy(assignedBy);
            dm.setAssignedTime(LocalDateTime.now());
            departmentManagerRepository.save(dm);

            // 确保用户角色为MANAGER
            if (employee.getUserId() != null) {
                userRepository.findById(employee.getUserId()).ifPresent(user -> {
                    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                        user.setRole("MANAGER");
                        userRepository.save(user);
                    }
                });
            }
        }

        // 将被移除的经理角色恢复为EMPLOYEE（如果不再管理任何部门）
        for (Long managerId : existingManagerIds) {
            if (!newManagerIds.contains(managerId)) {
                List<DepartmentManager> remainingAssignments = departmentManagerRepository.findByManagerId(managerId);
                if (remainingAssignments.isEmpty()) {
                    employeeRepository.findById(managerId).ifPresent(emp -> {
                        if (emp.getUserId() != null) {
                            userRepository.findById(emp.getUserId()).ifPresent(user -> {
                                if ("MANAGER".equalsIgnoreCase(user.getRole())) {
                                    user.setRole("EMPLOYEE");
                                    userRepository.save(user);
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    /**
     * 为部门设置单一经理
     */
    @Transactional
    public void setDepartmentManager(Long deptId, Long managerId, Long assignedBy) {
        if (managerId == null) {
            setDepartmentManagers(deptId, Collections.emptyList(), assignedBy);
        } else {
            setDepartmentManagers(deptId, Collections.singletonList(managerId), assignedBy);
        }
    }

    /**
     * 为部门添加一个经理
     */
    @Transactional
    public DepartmentManager addDepartmentManager(Long deptId, Long managerId, Long assignedBy) {
        // 验证部门和员工存在
        if (!departmentRepository.existsById(deptId)) {
            throw new RuntimeException("部门不存在");
        }
        if (!employeeRepository.existsById(managerId)) {
            throw new RuntimeException("员工不存在");
        }

        // 检查是否已经是经理
        if (departmentManagerRepository.existsByDeptIdAndManagerId(deptId, managerId)) {
            throw new RuntimeException("该员工已经是该部门的经理");
        }

        DepartmentManager dm = new DepartmentManager();
        dm.setDeptId(deptId);
        dm.setManagerId(managerId);
        dm.setAssignedBy(assignedBy);
        dm.setAssignedTime(LocalDateTime.now());
        return departmentManagerRepository.save(dm);
    }

    /**
     * 移除部门的某个经理
     */
    @Transactional
    public void removeDepartmentManager(Long deptId, Long managerId) {
        departmentManagerRepository.deleteByDeptIdAndManagerId(deptId, managerId);
    }

    /**
     * 获取部门的所有经理
     */
    public List<DepartmentManagerDTO> getDepartmentManagers(Long deptId) {
        List<DepartmentManager> managers = departmentManagerRepository.findByDeptId(deptId);
        return managers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 获取经理管理的所有部门
     */
    public List<DepartmentManagerDTO> getManagerDepartments(Long managerId) {
        List<DepartmentManager> depts = departmentManagerRepository.findByManagerId(managerId);
        return depts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 检查某人是否是某部门的经理
     */
    public boolean isManagerOfDepartment(Long deptId, Long managerId) {
        return departmentManagerRepository.existsByDeptIdAndManagerId(deptId, managerId);
    }

    /**
     * 转换为DTO
     */
    private DepartmentManagerDTO convertToDTO(DepartmentManager dm) {
        DepartmentManagerDTO dto = new DepartmentManagerDTO();
        dto.setId(dm.getId());
        dto.setDeptId(dm.getDeptId());
        dto.setManagerId(dm.getManagerId());
        dto.setAssignedBy(dm.getAssignedBy());
        dto.setAssignedTime(dm.getAssignedTime());

        // 获取部门名称与员工统计
        departmentRepository.findById(dm.getDeptId()).ifPresent(dept -> {
            dto.setDeptName(dept.getDeptName());
        });
        dto.setEmployeeCount((int) employeeRepository.countByDeptId(dm.getDeptId()));

        // 获取经理信息
        employeeRepository.findById(dm.getManagerId()).ifPresent(emp -> {
            dto.setManagerName(emp.getName());
            dto.setManagerEmpNo(emp.getEmpNo());
        });

        return dto;
    }

    /**
     * 转换Department为DepartmentVO（包含经理和员工统计）
     */
    private DepartmentVO convertToVO(Department dept) {
        DepartmentVO vo = new DepartmentVO();
        vo.setId(dept.getId());
        vo.setDeptName(dept.getDeptName());
        vo.setDeptCode(dept.getDeptCode());
        vo.setDescription(dept.getDescription());
        vo.setParentId(dept.getParentId());
        vo.setStatus(dept.getStatus());
        vo.setCreatedTime(dept.getCreatedTime());
        vo.setUpdatedTime(dept.getUpdatedTime());

        // 获取部门经理列表
        List<DepartmentManager> managers = departmentManagerRepository.findByDeptId(dept.getId());
        List<DepartmentVO.ManagerInfo> managerInfos = new ArrayList<>();
        for (DepartmentManager dm : managers) {
            employeeRepository.findById(dm.getManagerId()).ifPresent(emp -> {
                DepartmentVO.ManagerInfo info = new DepartmentVO.ManagerInfo();
                info.setManagerId(emp.getId());
                info.setManagerName(emp.getName());
                info.setManagerEmpNo(emp.getEmpNo());
                managerInfos.add(info);
            });
        }
        vo.setManagers(managerInfos);

        // 统计员工数量
        long count = employeeRepository.countByDeptId(dept.getId());
        vo.setEmployeeCount((int) count);

        return vo;
    }
}
