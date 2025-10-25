package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    /**
     * 根据员工ID查询所有考勤记录
     */
    List<Attendance> findByEmpIdOrderByAttendanceDateDesc(Long empId);

    /**
     * 根据日期查询考勤记录
     */
    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);

    /**
     * 查询员工某日期范围内的考勤记录
     */
    List<Attendance> findByEmpIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(Long empId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询员工当天的考勤记录
     */
    Optional<Attendance> findByEmpIdAndAttendanceDate(Long empId, LocalDate attendanceDate);

    /**
     * 根据状态统计考勤记录数
     */
    long countByStatus(String status);

    /**
     * 查询部门某日期范围内的考勤记录
     */
    List<Attendance> findByDeptIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(Long deptId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询指定日期范围内的全部考勤记录
     */
    List<Attendance> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * 统计员工某月的出勤天数
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.empId = ?1 " +
           "AND YEAR(a.attendanceDate) = ?2 AND MONTH(a.attendanceDate) = ?3 " +
           "AND a.checkInTime IS NOT NULL")
    long countAttendanceDaysByMonth(Long empId, int year, int month);

    /**
     * 统计员工某月的迟到次数
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.empId = ?1 " +
           "AND YEAR(a.attendanceDate) = ?2 AND MONTH(a.attendanceDate) = ?3 " +
           "AND a.isLate = true")
    long countLateDaysByMonth(Long empId, int year, int month);

    /**
     * 统计员工某月的早退次数
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.empId = ?1 " +
           "AND YEAR(a.attendanceDate) = ?2 AND MONTH(a.attendanceDate) = ?3 " +
           "AND a.isEarlyLeave = true")
    long countEarlyLeaveDaysByMonth(Long empId, int year, int month);
}
