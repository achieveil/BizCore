package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {

    /**
     * 根据员工ID查询加班记录
     */
    List<OvertimeRequest> findByEmpIdOrderByOvertimeDateDesc(Long empId);

    /**
     * 根据员工ID和状态查询加班记录
     */
    List<OvertimeRequest> findByEmpIdAndStatusOrderByOvertimeDateDesc(Long empId, String status);

    /**
     * 根据审批人ID查询待审批的加班记录
     */
    List<OvertimeRequest> findByApproverIdAndStatusOrderByOvertimeDateDesc(Long approverId, String status);

    /**
     * 查询某员工在指定日期范围内的已批准加班记录
     */
    @Query("SELECT o FROM OvertimeRequest o WHERE o.empId = ?1 AND o.status = 'APPROVED' " +
           "AND o.overtimeDate >= ?2 AND o.overtimeDate <= ?3 " +
           "ORDER BY o.overtimeDate DESC")
    List<OvertimeRequest> findApprovedOvertimesByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate);

    /**
     * 统计某员工某月的已批准加班总时长
     */
    @Query("SELECT SUM(o.hours) FROM OvertimeRequest o " +
           "WHERE o.empId = ?1 AND o.status = 'APPROVED' " +
           "AND YEAR(o.overtimeDate) = ?2 AND MONTH(o.overtimeDate) = ?3")
    Double sumApprovedOvertimeHoursByMonth(Long empId, int year, int month);

    /**
     * 统计某员工未补偿的加班时长
     */
    @Query("SELECT SUM(o.hours) FROM OvertimeRequest o " +
           "WHERE o.empId = ?1 AND o.status = 'APPROVED' AND o.compensated = false")
    Double sumUncompensatedOvertimeHours(Long empId);
}
