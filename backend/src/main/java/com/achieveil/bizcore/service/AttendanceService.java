package com.achieveil.bizcore.service;

import com.achieveil.bizcore.dto.AttendanceDTO;
import com.achieveil.bizcore.dto.AttendanceStatsDTO;
import com.achieveil.bizcore.entity.Attendance;
import com.achieveil.bizcore.entity.DepartmentAttendanceRule;
import com.achieveil.bizcore.entity.Employee;
import com.achieveil.bizcore.entity.User;
import com.achieveil.bizcore.repository.AttendanceRepository;
import com.achieveil.bizcore.repository.DepartmentAttendanceRuleRepository;
import com.achieveil.bizcore.repository.DepartmentRepository;
import com.achieveil.bizcore.repository.EmployeeRepository;
import com.achieveil.bizcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentAttendanceRuleRepository ruleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /**
     * 员工签到
     */
    @Transactional
    public Attendance checkIn(Long empId, String remark) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 检查是否已签到
        Optional<Attendance> existingOpt = attendanceRepository.findByEmpIdAndAttendanceDate(empId, today);
        if (existingOpt.isPresent() && existingOpt.get().getCheckInTime() != null) {
            throw new RuntimeException("今日已签到");
        }

        // 获取员工信息
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        // 获取部门考勤规则
        DepartmentAttendanceRule rule = ruleRepository.findByDeptId(employee.getDeptId())
                .orElseGet(() -> getDefaultRule());

        // 创建或更新考勤记录
        Attendance attendance = existingOpt.orElse(new Attendance());
        attendance.setEmpId(empId);
        attendance.setDeptId(employee.getDeptId());
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(now);
        attendance.setRemark(remark);

        // 判断是否迟到
        LocalTime standardCheckIn = rule.getCheckInTime();
        LocalTime lateThreshold = standardCheckIn.plusMinutes(rule.getLateGraceMinutes());

        if (now.isAfter(lateThreshold)) {
            attendance.setIsLate(true);
            attendance.setLateMinutes((int) Duration.between(standardCheckIn, now).toMinutes());
            attendance.setStatus("LATE");
        } else {
            attendance.setIsLate(false);
            attendance.setLateMinutes(0);
            attendance.setStatus("NORMAL");
        }

        return attendanceRepository.save(attendance);
    }

    /**
     * 员工签退
     */
    @Transactional
    public Attendance checkOut(Long empId, String remark) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 查找今日签到记录
        Attendance attendance = attendanceRepository.findByEmpIdAndAttendanceDate(empId, today)
                .orElseThrow(() -> new RuntimeException("今日未签到，无法签退"));

        if (attendance.getCheckInTime() == null) {
            throw new RuntimeException("今日未签到，无法签退");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("今日已签退");
        }

        // 获取员工信息
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        // 获取部门考勤规则
        DepartmentAttendanceRule rule = ruleRepository.findByDeptId(employee.getDeptId())
                .orElseGet(() -> getDefaultRule());

        attendance.setCheckOutTime(now);
        if (remark != null && !remark.isEmpty()) {
            attendance.setRemark(attendance.getRemark() != null ?
                    attendance.getRemark() + "; " + remark : remark);
        }

        // 判断是否早退
        LocalTime standardCheckOut = rule.getCheckOutTime();
        LocalTime earlyLeaveThreshold = standardCheckOut.minusMinutes(rule.getEarlyLeaveGraceMinutes());

        if (now.isBefore(earlyLeaveThreshold)) {
            attendance.setIsEarlyLeave(true);
            attendance.setEarlyLeaveMinutes((int) Duration.between(now, standardCheckOut).toMinutes());
            if (attendance.getIsLate()) {
                attendance.setStatus("LATE_AND_EARLY");
            } else {
                attendance.setStatus("EARLY_LEAVE");
            }
        } else {
            attendance.setIsEarlyLeave(false);
            attendance.setEarlyLeaveMinutes(0);
        }

        // 计算工作时长
        LocalTime checkInTime = attendance.getCheckInTime();
        double hours = Duration.between(checkInTime, now).toMinutes() / 60.0;
        attendance.setWorkHours(Math.round(hours * 100.0) / 100.0);

        return attendanceRepository.save(attendance);
    }

    /**
     * 获取员工考勤记录
     */
    public List<AttendanceDTO> getEmployeeAttendance(Long empId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository
                .findByEmpIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(empId, startDate, endDate);

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        User user = null;
        if (employee.getUserId() != null) {
            user = userRepository.findById(employee.getUserId()).orElse(null);
        }

        User finalUser = user;
        return attendances.stream()
                .map(a -> convertToDTO(a, employee, finalUser))
                .collect(Collectors.toList());
    }

    /**
     * 获取员工月度考勤统计
     */
    public AttendanceStatsDTO getMonthlyStats(Long empId, int year, int month) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        AttendanceStatsDTO stats = new AttendanceStatsDTO();
        stats.setEmpId(empId);
        stats.setEmpName(employee.getName());
        stats.setYear(year);
        stats.setMonth(month);

        stats.setAttendanceDays(attendanceRepository.countAttendanceDaysByMonth(empId, year, month));
        stats.setLateDays(attendanceRepository.countLateDaysByMonth(empId, year, month));
        stats.setEarlyLeaveDays(attendanceRepository.countEarlyLeaveDaysByMonth(empId, year, month));

        return stats;
    }

    /**
     * 获取部门考勤记录（经理查看）
     */
    public List<AttendanceDTO> getDepartmentAttendance(Long deptId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository
                .findByDeptIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(deptId, startDate, endDate);

        return attendances.stream()
                .map(a -> {
                    Employee emp = employeeRepository.findById(a.getEmpId()).orElse(null);
                    User user = null;
                    if (emp != null && emp.getUserId() != null) {
                        user = userRepository.findById(emp.getUserId()).orElse(null);
                    }
                    return convertToDTO(a, emp, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取管理员视角的考勤列表，可按是否经理过滤
     */
    public List<AttendanceDTO> getAttendanceForAdmin(LocalDate startDate, LocalDate endDate, Boolean isManager) {
        List<Attendance> attendances;

        if (startDate != null && endDate != null) {
            attendances = attendanceRepository
                    .findByAttendanceDateBetweenOrderByAttendanceDateDesc(startDate, endDate);
        } else {
            attendances = attendanceRepository.findAll(Sort.by(Sort.Direction.DESC, "attendanceDate"));
        }

        if (attendances.isEmpty()) {
            return List.of();
        }

        Set<Long> empIds = attendances.stream()
                .map(Attendance::getEmpId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        Map<Long, Employee> employeeMap = employeeRepository.findAllById(empIds).stream()
                .collect(Collectors.toMap(Employee::getId, emp -> emp));

        Set<Long> userIds = employeeMap.values().stream()
                .map(Employee::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return attendances.stream()
                .map(attendance -> {
                    Employee employee = employeeMap.get(attendance.getEmpId());
                    User user = (employee != null && employee.getUserId() != null)
                            ? userMap.get(employee.getUserId())
                            : null;
                    boolean managerFlag = user != null && "MANAGER".equalsIgnoreCase(user.getRole());

                    if (isManager != null) {
                        if (Boolean.TRUE.equals(isManager) && !managerFlag) {
                            return null;
                        }
                        if (Boolean.FALSE.equals(isManager) && managerFlag) {
                            return null;
                        }
                    }

                    return convertToDTO(attendance, employee, user);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private AttendanceDTO convertToDTO(Attendance attendance, Employee employee, User user) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setEmpId(attendance.getEmpId());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setStatus(attendance.getStatus());
        dto.setIsLate(attendance.getIsLate());
        dto.setLateMinutes(attendance.getLateMinutes());
        dto.setIsEarlyLeave(attendance.getIsEarlyLeave());
        dto.setEarlyLeaveMinutes(attendance.getEarlyLeaveMinutes());
        dto.setWorkHours(attendance.getWorkHours());
        dto.setRemark(attendance.getRemark());

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

        if (user != null) {
            dto.setRole(user.getRole());
            dto.setManager("MANAGER".equalsIgnoreCase(user.getRole()));
        } else {
            dto.setRole(null);
            dto.setManager(false);
        }

        return dto;
    }

    /**
     * 获取默认考勤规则
     */
    private DepartmentAttendanceRule getDefaultRule() {
        DepartmentAttendanceRule rule = new DepartmentAttendanceRule();
        rule.setCheckInTime(LocalTime.of(9, 0));
        rule.setCheckOutTime(LocalTime.of(18, 0));
        rule.setLateGraceMinutes(10);
        rule.setEarlyLeaveGraceMinutes(10);
        rule.setRequireCheckOut(true);
        rule.setFlexibleWorkTime(false);
        rule.setStandardWorkHours(8.0);
        return rule;
    }

    public List<Attendance> getAttendancesByEmpId(Long empId) {
        return attendanceRepository.findByEmpIdOrderByAttendanceDateDesc(empId);
    }

    public List<Attendance> getAttendancesByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date);
    }

    public List<Attendance> getAttendancesByRange(Long empId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByEmpIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(empId, startDate, endDate);
    }

    /**
     * 更新考勤记录
     */
    @Transactional
    public Attendance updateAttendance(Long id, Attendance attendance) {
        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("考勤记录不存在"));

        // 更新可修改的字段
        if (attendance.getCheckInTime() != null) {
            existing.setCheckInTime(attendance.getCheckInTime());
        }
        if (attendance.getCheckOutTime() != null) {
            existing.setCheckOutTime(attendance.getCheckOutTime());
        }
        if (attendance.getStatus() != null) {
            existing.setStatus(attendance.getStatus());
        }
        if (attendance.getIsLate() != null) {
            existing.setIsLate(attendance.getIsLate());
        }
        if (attendance.getLateMinutes() != null) {
            existing.setLateMinutes(attendance.getLateMinutes());
        }
        if (attendance.getIsEarlyLeave() != null) {
            existing.setIsEarlyLeave(attendance.getIsEarlyLeave());
        }
        if (attendance.getEarlyLeaveMinutes() != null) {
            existing.setEarlyLeaveMinutes(attendance.getEarlyLeaveMinutes());
        }
        if (attendance.getWorkHours() != null) {
            existing.setWorkHours(attendance.getWorkHours());
        }
        if (attendance.getRemark() != null) {
            existing.setRemark(attendance.getRemark());
        }

        return attendanceRepository.save(existing);
    }

    /**
     * 删除考勤记录
     */
    @Transactional
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new RuntimeException("考勤记录不存在");
        }
        attendanceRepository.deleteById(id);
    }
}
