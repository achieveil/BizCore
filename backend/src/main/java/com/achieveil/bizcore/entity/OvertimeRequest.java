package com.achieveil.bizcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 加班申请实体
 */
@Data
@Entity
@Table(name = "overtime_requests")
public class OvertimeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工ID
     */
    @Column(nullable = false)
    private Long empId;

    /**
     * 加班日期
     */
    @Column(nullable = false)
    private LocalDate overtimeDate;

    /**
     * 加班开始时间
     */
    @Column(nullable = false)
    private LocalTime startTime;

    /**
     * 加班结束时间
     */
    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * 加班时长（小时）
     */
    @Column(nullable = false)
    private Double hours;

    /**
     * 加班类型：WEEKDAY-工作日加班, WEEKEND-周末加班, HOLIDAY-节假日加班
     */
    @Column(nullable = false, length = 20)
    private String overtimeType;

    /**
     * 加班原因
     */
    @Column(nullable = false, length = 500)
    private String reason;

    /**
     * 审批状态：PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已撤销
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * 审批人ID（部门经理）
     */
    @Column
    private Long approverId;

    /**
     * 审批意见
     */
    @Column(length = 500)
    private String approvalComment;

    /**
     * 审批时间
     */
    @Column
    private LocalDateTime approvalTime;

    /**
     * 是否已补偿（调休或加班费）
     */
    @Column(nullable = false)
    private Boolean compensated = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
