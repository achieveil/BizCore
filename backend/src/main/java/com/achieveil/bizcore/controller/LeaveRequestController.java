package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.ApprovalRequest;
import com.achieveil.bizcore.dto.LeaveRequestCreateDTO;
import com.achieveil.bizcore.dto.LeaveRequestDTO;
import com.achieveil.bizcore.entity.LeaveRequest;
import com.achieveil.bizcore.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * 创建请假申请
     */
    @PostMapping
    public Result<LeaveRequest> createLeaveRequest(@Valid @RequestBody LeaveRequestCreateDTO dto) {
        try {
            LeaveRequest request = leaveRequestService.createLeaveRequest(dto);
            return Result.success("请假申请提交成功", request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批请假申请
     */
    @PostMapping("/approve")
    public Result<LeaveRequest> approveLeaveRequest(@Valid @RequestBody ApprovalRequest request) {
        try {
            LeaveRequest leaveRequest = leaveRequestService.approveLeaveRequest(request);
            return Result.success("审批成功", leaveRequest);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取员工的请假记录
     */
    @GetMapping("/employee/{empId}")
    public Result<List<LeaveRequestDTO>> getEmployeeLeaveRequests(@PathVariable Long empId) {
        try {
            List<LeaveRequestDTO> requests = leaveRequestService.getEmployeeLeaveRequests(empId);
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待审批的请假申请（部门经理）
     */
    @GetMapping("/pending/{managerId}")
    public Result<List<LeaveRequestDTO>> getPendingLeaveRequests(@PathVariable Long managerId) {
        try {
            List<LeaveRequestDTO> requests = leaveRequestService.getPendingLeaveRequests(managerId);
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有请假申请（管理员）
     */
    @GetMapping("/all")
    public Result<List<LeaveRequestDTO>> getAllLeaveRequests() {
        try {
            List<LeaveRequestDTO> requests = leaveRequestService.getAllLeaveRequests();
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取部门员工的请假记录（经理）
     */
    @GetMapping("/department/{deptId}")
    public Result<List<LeaveRequestDTO>> getDepartmentLeaveRequests(@PathVariable Long deptId) {
        try {
            List<LeaveRequestDTO> requests = leaveRequestService.getDepartmentLeaveRequests(deptId);
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 撤销请假申请
     */
    @DeleteMapping("/{requestId}/employee/{empId}")
    public Result<Void> cancelLeaveRequest(@PathVariable Long requestId, @PathVariable Long empId) {
        try {
            leaveRequestService.cancelLeaveRequest(requestId, empId);
            return Result.success("请假申请已撤销", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
