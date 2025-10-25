package com.achieveil.bizcore.service;

import com.achieveil.bizcore.dto.ApprovalRequest;
import com.achieveil.bizcore.dto.OvertimeRequestCreateDTO;
import com.achieveil.bizcore.dto.OvertimeRequestDTO;
import com.achieveil.bizcore.entity.Employee;
import com.achieveil.bizcore.entity.OvertimeRequest;
import com.achieveil.bizcore.entity.User;
import com.achieveil.bizcore.repository.DepartmentRepository;
import com.achieveil.bizcore.repository.EmployeeRepository;
import com.achieveil.bizcore.repository.OvertimeRequestRepository;
import com.achieveil.bizcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    private static final Map<String, String> OVERTIME_TYPE_NAMES = new HashMap<>() {{
        put("WEEKDAY", "工作日加班");
        put("WEEKEND", "周末加班");
        put("HOLIDAY", "节假日加班");
    }};

    private static final Map<String, String> STATUS_NAMES = new HashMap<>() {{
        put("PENDING", "待审批");
        put("APPROVED", "已批准");
        put("REJECTED", "已拒绝");
        put("CANCELLED", "已撤销");
    }};

    /**
     * 创建加班申请
     */
    @Transactional
    public OvertimeRequest createOvertimeRequest(OvertimeRequestCreateDTO dto) {
        // 验证员工存在
        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        // 计算加班时长
        double hours = Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() / 60.0;
        if (hours <= 0) {
            throw new RuntimeException("加班时间无效");
        }

        // 查找部门经理
        Long managerId = findDepartmentManager(employee.getDeptId());

        OvertimeRequest request = new OvertimeRequest();
        request.setEmpId(dto.getEmpId());
        request.setOvertimeDate(dto.getOvertimeDate());
        request.setStartTime(dto.getStartTime());
        request.setEndTime(dto.getEndTime());
        request.setHours(Math.round(hours * 100.0) / 100.0);
        request.setOvertimeType(dto.getOvertimeType());
        request.setReason(dto.getReason());
        request.setStatus("PENDING");
        request.setApproverId(managerId);
        request.setCompensated(false);

        return overtimeRequestRepository.save(request);
    }

    /**
     * 查找部门经理
     */
    private Long findDepartmentManager(Long deptId) {
        List<Employee> employees = employeeRepository.findByDeptId(deptId);
        for (Employee emp : employees) {
            if (emp.getUserId() != null) {
                User user = userRepository.findById(emp.getUserId()).orElse(null);
                if (user != null && "MANAGER".equals(user.getRole())) {
                    return emp.getUserId();
                }
            }
        }
        return null;
    }

    /**
     * 审批加班申请
     */
    @Transactional
    public OvertimeRequest approveOvertimeRequest(ApprovalRequest request) {
        OvertimeRequest overtimeRequest = overtimeRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new RuntimeException("加班申请不存在"));

        if (!"PENDING".equals(overtimeRequest.getStatus())) {
            throw new RuntimeException("该加班申请已被处理");
        }

        if ("APPROVE".equals(request.getAction())) {
            overtimeRequest.setStatus("APPROVED");
        } else if ("REJECT".equals(request.getAction())) {
            overtimeRequest.setStatus("REJECTED");
        } else {
            throw new RuntimeException("无效的审批操作");
        }

        overtimeRequest.setApproverId(request.getApproverId());
        overtimeRequest.setApprovalComment(request.getComment());
        overtimeRequest.setApprovalTime(LocalDateTime.now());

        return overtimeRequestRepository.save(overtimeRequest);
    }

    /**
     * 获取员工的加班记录
     */
    public List<OvertimeRequestDTO> getEmployeeOvertimeRequests(Long empId) {
        List<OvertimeRequest> requests = overtimeRequestRepository.findByEmpIdOrderByOvertimeDateDesc(empId);
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取待审批的加班申请（部门经理）
     */
    public List<OvertimeRequestDTO> getPendingOvertimeRequests(Long managerId) {
        List<OvertimeRequest> requests = overtimeRequestRepository
                .findByApproverIdAndStatusOrderByOvertimeDateDesc(managerId, "PENDING");
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有加班申请（管理员）
     */
    public List<OvertimeRequestDTO> getAllOvertimeRequests() {
        List<OvertimeRequest> requests = overtimeRequestRepository.findAll();
        return requests.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .collect(Collectors.toList());
    }

    /**
     * 获取部门员工的加班记录（经理）
     */
    public List<OvertimeRequestDTO> getDepartmentOvertimeRequests(Long deptId) {
        // 获取部门所有员工ID
        List<Employee> employees = employeeRepository.findByDeptId(deptId);
        List<Long> empIds = employees.stream().map(Employee::getId).collect(Collectors.toList());

        if (empIds.isEmpty()) {
            return List.of();
        }

        // 获取这些员工的所有加班记录
        List<OvertimeRequest> requests = overtimeRequestRepository.findAll();
        return requests.stream()
                .filter(req -> empIds.contains(req.getEmpId()))
                .map(this::convertToDTO)
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .collect(Collectors.toList());
    }

    /**
     * 撤销加班申请
     */
    @Transactional
    public void cancelOvertimeRequest(Long requestId, Long empId) {
        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("加班申请不存在"));

        if (!request.getEmpId().equals(empId)) {
            throw new RuntimeException("无权撤销此加班申请");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("只能撤销待审批的加班申请");
        }

        request.setStatus("CANCELLED");
        overtimeRequestRepository.save(request);
    }

    /**
     * 转换为DTO
     */
    private OvertimeRequestDTO convertToDTO(OvertimeRequest request) {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setId(request.getId());
        dto.setEmpId(request.getEmpId());
        dto.setOvertimeDate(request.getOvertimeDate());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        dto.setHours(request.getHours());
        dto.setOvertimeType(request.getOvertimeType());
        dto.setOvertimeTypeName(OVERTIME_TYPE_NAMES.getOrDefault(request.getOvertimeType(), request.getOvertimeType()));
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setStatusName(STATUS_NAMES.getOrDefault(request.getStatus(), request.getStatus()));
        dto.setApproverId(request.getApproverId());
        dto.setApprovalComment(request.getApprovalComment());
        dto.setApprovalTime(request.getApprovalTime());
        dto.setCompensated(request.getCompensated());
        dto.setCreatedTime(request.getCreatedTime());

        // 获取员工信息
        Employee employee = employeeRepository.findById(request.getEmpId()).orElse(null);
        if (employee != null) {
            dto.setEmpName(employee.getName());
            dto.setEmpNo(employee.getEmpNo());
            dto.setDeptId(employee.getDeptId());
            dto.setPosition(employee.getPosition());

            // 获取部门名称
            if (employee.getDeptId() != null) {
                departmentRepository.findById(employee.getDeptId()).ifPresent(dept -> {
                    dto.setDeptName(dept.getDeptName());
                });
            }
        }

        // 获取审批人信息
        if (request.getApproverId() != null) {
            User approver = userRepository.findById(request.getApproverId()).orElse(null);
            if (approver != null) {
                dto.setApproverName(approver.getRealName());
            }
        }

        return dto;
    }
}
