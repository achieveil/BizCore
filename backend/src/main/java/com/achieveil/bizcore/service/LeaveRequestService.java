package com.achieveil.bizcore.service;

import com.achieveil.bizcore.dto.ApprovalRequest;
import com.achieveil.bizcore.dto.LeaveRequestCreateDTO;
import com.achieveil.bizcore.dto.LeaveRequestDTO;
import com.achieveil.bizcore.entity.Employee;
import com.achieveil.bizcore.entity.LeaveRequest;
import com.achieveil.bizcore.entity.User;
import com.achieveil.bizcore.repository.DepartmentRepository;
import com.achieveil.bizcore.repository.EmployeeRepository;
import com.achieveil.bizcore.repository.LeaveRequestRepository;
import com.achieveil.bizcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    private static final Map<String, String> LEAVE_TYPE_NAMES = new HashMap<>() {{
        put("SICK", "病假");
        put("ANNUAL", "年假");
        put("PERSONAL", "事假");
        put("MARRIAGE", "婚假");
        put("MATERNITY", "产假");
        put("PATERNITY", "陪产假");
        put("BEREAVEMENT", "丧假");
    }};

    private static final Map<String, String> STATUS_NAMES = new HashMap<>() {{
        put("PENDING", "待审批");
        put("APPROVED", "已批准");
        put("REJECTED", "已拒绝");
        put("CANCELLED", "已撤销");
    }};

    /**
     * 创建请假申请
     */
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequestCreateDTO dto) {
        // 验证员工存在
        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        // 计算请假天数
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        if (days <= 0) {
            throw new RuntimeException("请假日期无效");
        }

        // 查找部门经理
        Long managerId = findDepartmentManager(employee.getDeptId());

        LeaveRequest request = new LeaveRequest();
        request.setEmpId(dto.getEmpId());
        request.setLeaveType(dto.getLeaveType());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setDays((int) days);
        request.setReason(dto.getReason());
        request.setAttachmentUrl(dto.getAttachmentUrl());
        request.setStatus("PENDING");
        request.setApproverId(managerId);

        return leaveRequestRepository.save(request);
    }

    /**
     * 查找部门经理
     */
    private Long findDepartmentManager(Long deptId) {
        // 查找该部门的经理（role为MANAGER的员工）
        List<Employee> employees = employeeRepository.findByDeptId(deptId);
        for (Employee emp : employees) {
            if (emp.getUserId() != null) {
                User user = userRepository.findById(emp.getUserId()).orElse(null);
                if (user != null && "MANAGER".equals(user.getRole())) {
                    return emp.getUserId();
                }
            }
        }
        // 如果没有找到部门经理，返回null
        return null;
    }

    /**
     * 审批请假申请
     */
    @Transactional
    public LeaveRequest approveLeaveRequest(ApprovalRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new RuntimeException("该请假申请已被处理");
        }

        if ("APPROVE".equals(request.getAction())) {
            leaveRequest.setStatus("APPROVED");
        } else if ("REJECT".equals(request.getAction())) {
            leaveRequest.setStatus("REJECTED");
        } else {
            throw new RuntimeException("无效的审批操作");
        }

        leaveRequest.setApproverId(request.getApproverId());
        leaveRequest.setApprovalComment(request.getComment());
        leaveRequest.setApprovalTime(LocalDateTime.now());

        return leaveRequestRepository.save(leaveRequest);
    }

    /**
     * 获取员工的请假记录
     */
    public List<LeaveRequestDTO> getEmployeeLeaveRequests(Long empId) {
        List<LeaveRequest> requests = leaveRequestRepository.findByEmpIdOrderByCreatedTimeDesc(empId);
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取待审批的请假申请（部门经理）
     */
    public List<LeaveRequestDTO> getPendingLeaveRequests(Long managerId) {
        List<LeaveRequest> requests = leaveRequestRepository
                .findByApproverIdAndStatusOrderByCreatedTimeDesc(managerId, "PENDING");
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有请假申请（管理员）
     */
    public List<LeaveRequestDTO> getAllLeaveRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findAll();
        return requests.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .collect(Collectors.toList());
    }

    /**
     * 获取部门员工的请假记录（经理）
     */
    public List<LeaveRequestDTO> getDepartmentLeaveRequests(Long deptId) {
        // 获取部门所有员工ID
        List<Employee> employees = employeeRepository.findByDeptId(deptId);
        List<Long> empIds = employees.stream().map(Employee::getId).collect(Collectors.toList());

        if (empIds.isEmpty()) {
            return List.of();
        }

        // 获取这些员工的所有请假记录
        List<LeaveRequest> requests = leaveRequestRepository.findAll();
        return requests.stream()
                .filter(req -> empIds.contains(req.getEmpId()))
                .map(this::convertToDTO)
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .collect(Collectors.toList());
    }

    /**
     * 撤销请假申请
     */
    @Transactional
    public void cancelLeaveRequest(Long requestId, Long empId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));

        if (!request.getEmpId().equals(empId)) {
            throw new RuntimeException("无权撤销此请假申请");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("只能撤销待审批的请假申请");
        }

        request.setStatus("CANCELLED");
        leaveRequestRepository.save(request);
    }

    /**
     * 转换为DTO
     */
    private LeaveRequestDTO convertToDTO(LeaveRequest request) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(request.getId());
        dto.setEmpId(request.getEmpId());
        dto.setLeaveType(request.getLeaveType());
        dto.setLeaveTypeName(LEAVE_TYPE_NAMES.getOrDefault(request.getLeaveType(), request.getLeaveType()));
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setDays(request.getDays());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setStatusName(STATUS_NAMES.getOrDefault(request.getStatus(), request.getStatus()));
        dto.setApproverId(request.getApproverId());
        dto.setApprovalComment(request.getApprovalComment());
        dto.setApprovalTime(request.getApprovalTime());
        dto.setAttachmentUrl(request.getAttachmentUrl());
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
