package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * 根据员工ID查询请假记录
     */
    List<LeaveRequest> findByEmpIdOrderByCreatedTimeDesc(Long empId);

    /**
     * 根据员工ID和状态查询请假记录
     */
    List<LeaveRequest> findByEmpIdAndStatusOrderByCreatedTimeDesc(Long empId, String status);

    /**
     * 根据审批人ID查询待审批的请假记录
     */
    List<LeaveRequest> findByApproverIdAndStatusOrderByCreatedTimeDesc(Long approverId, String status);

    /**
     * 查询某员工在指定日期范围内的已批准请假记录
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.empId = ?1 AND l.status = 'APPROVED' " +
           "AND ((l.startDate <= ?3 AND l.endDate >= ?2))")
    List<LeaveRequest> findApprovedLeavesByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate);

    /**
     * 统计某员工某年度的各类请假天数
     */
    @Query("SELECT l.leaveType, SUM(l.days) FROM LeaveRequest l " +
           "WHERE l.empId = ?1 AND l.status = 'APPROVED' " +
           "AND YEAR(l.startDate) = ?2 " +
           "GROUP BY l.leaveType")
    List<Object[]> countLeaveDaysByTypeAndYear(Long empId, int year);
}
