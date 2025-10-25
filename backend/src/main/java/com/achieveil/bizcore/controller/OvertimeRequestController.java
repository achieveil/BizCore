package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.ApprovalRequest;
import com.achieveil.bizcore.dto.OvertimeRequestCreateDTO;
import com.achieveil.bizcore.dto.OvertimeRequestDTO;
import com.achieveil.bizcore.entity.OvertimeRequest;
import com.achieveil.bizcore.service.OvertimeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/overtime-requests")
@RequiredArgsConstructor
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;

    /**
     * 创建加班申请
     */
    @PostMapping
    public Result<OvertimeRequest> createOvertimeRequest(@Valid @RequestBody OvertimeRequestCreateDTO dto) {
        try {
            OvertimeRequest request = overtimeRequestService.createOvertimeRequest(dto);
            return Result.success("加班申请提交成功", request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批加班申请
     */
    @PostMapping("/approve")
    public Result<OvertimeRequest> approveOvertimeRequest(@Valid @RequestBody ApprovalRequest request) {
        try {
            OvertimeRequest overtimeRequest = overtimeRequestService.approveOvertimeRequest(request);
            return Result.success("审批成功", overtimeRequest);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取员工的加班记录
     */
    @GetMapping("/employee/{empId}")
    public Result<List<OvertimeRequestDTO>> getEmployeeOvertimeRequests(@PathVariable Long empId) {
        try {
            List<OvertimeRequestDTO> requests = overtimeRequestService.getEmployeeOvertimeRequests(empId);
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待审批的加班申请（部门经理）
     */
    @GetMapping("/pending/{managerId}")
    public Result<List<OvertimeRequestDTO>> getPendingOvertimeRequests(@PathVariable Long managerId) {
        try {
            List<OvertimeRequestDTO> requests = overtimeRequestService.getPendingOvertimeRequests(managerId);
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有加班申请（管理员）
     */
    @GetMapping("/all")
    public Result<List<OvertimeRequestDTO>> getAllOvertimeRequests() {
        try {
            List<OvertimeRequestDTO> requests = overtimeRequestService.getAllOvertimeRequests();
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取部门员工的加班记录（经理）
     */
    @GetMapping("/department/{deptId}")
    public Result<List<OvertimeRequestDTO>> getDepartmentOvertimeRequests(@PathVariable Long deptId) {
        try {
            List<OvertimeRequestDTO> requests = overtimeRequestService.getDepartmentOvertimeRequests(deptId);
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 撤销加班申请
     */
    @DeleteMapping("/{requestId}/employee/{empId}")
    public Result<Void> cancelOvertimeRequest(@PathVariable Long requestId, @PathVariable Long empId) {
        try {
            overtimeRequestService.cancelOvertimeRequest(requestId, empId);
            return Result.success("加班申请已撤销", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
