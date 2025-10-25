package com.achieveil.bizcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体
 */
@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工ID
     */
    @Column(nullable = false)
    private Long empId;

    /**
     * 请假类型：SICK-病假, ANNUAL-年假, PERSONAL-事假, MARRIAGE-婚假, MATERNITY-产假, PATERNITY-陪产假, BEREAVEMENT-丧假
     */
    @Column(nullable = false, length = 20)
    private String leaveType;

    /**
     * 开始日期
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * 请假天数
     */
    @Column(nullable = false)
    private Integer days;

    /**
     * 请假原因
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
     * 附件URL（如病假条等）
     */
    @Column(length = 500)
    private String attachmentUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
