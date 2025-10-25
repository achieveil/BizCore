package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.AttendanceDTO;
import com.achieveil.bizcore.dto.AttendanceStatsDTO;
import com.achieveil.bizcore.dto.CheckInRequest;
import com.achieveil.bizcore.dto.CheckOutRequest;
import com.achieveil.bizcore.entity.Attendance;
import com.achieveil.bizcore.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 管理员查看全部考勤记录
     */
    @GetMapping
    public Result<List<AttendanceDTO>> getAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean isManager) {
        try {
            List<AttendanceDTO> attendances = attendanceService.getAttendanceForAdmin(startDate, endDate, isManager);
            return Result.success(attendances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 员工签到
     */
    @PostMapping("/check-in")
    public Result<Attendance> checkIn(@RequestBody CheckInRequest request) {
        try {
            Attendance attendance = attendanceService.checkIn(request.getEmpId(), request.getRemark());
            return Result.success("签到成功", attendance);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 员工签退
     */
    @PostMapping("/check-out")
    public Result<Attendance> checkOut(@RequestBody CheckOutRequest request) {
        try {
            Attendance attendance = attendanceService.checkOut(request.getEmpId(), request.getRemark());
            return Result.success("签退成功", attendance);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取员工考勤记录
     */
    @GetMapping("/employee/{empId}/records")
    public Result<List<AttendanceDTO>> getEmployeeAttendance(
            @PathVariable Long empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceDTO> attendances = attendanceService.getEmployeeAttendance(empId, startDate, endDate);
            return Result.success(attendances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取员工月度考勤统计
     */
    @GetMapping("/employee/{empId}/stats")
    public Result<AttendanceStatsDTO> getMonthlyStats(
            @PathVariable Long empId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            AttendanceStatsDTO stats = attendanceService.getMonthlyStats(empId, year, month);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取部门考勤记录（经理查看）
     */
    @GetMapping("/department/{deptId}/records")
    public Result<List<AttendanceDTO>> getDepartmentAttendance(
            @PathVariable Long deptId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceDTO> attendances = attendanceService.getDepartmentAttendance(deptId, startDate, endDate);
            return Result.success(attendances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/employee/{empId}")
    public Result<List<Attendance>> getAttendancesByEmpId(@PathVariable Long empId) {
        try {
            List<Attendance> attendances = attendanceService.getAttendancesByEmpId(empId);
            return Result.success(attendances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/date/{date}")
    public Result<List<Attendance>> getAttendancesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Attendance> attendances = attendanceService.getAttendancesByDate(date);
            return Result.success(attendances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/employee/{empId}/range")
    public Result<List<Attendance>> getAttendancesByRange(
            @PathVariable Long empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Attendance> attendances = attendanceService.getAttendancesByRange(empId, startDate, endDate);
            return Result.success(attendances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Attendance> updateAttendance(@PathVariable Long id, @RequestBody Attendance attendance) {
        try {
            Attendance updated = attendanceService.updateAttendance(id, attendance);
            return Result.success("考勤记录更新成功", updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAttendance(@PathVariable Long id) {
        try {
            attendanceService.deleteAttendance(id);
            return Result.success("考勤记录删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
