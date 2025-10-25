package com.achieveil.bizcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 部门考勤规则实体
 */
@Data
@Entity
@Table(name = "department_attendance_rules")
public class DepartmentAttendanceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 部门ID
     */
    @Column(nullable = false, unique = true)
    private Long deptId;

    /**
     * 标准上班时间
     */
    @Column(nullable = false)
    private LocalTime checkInTime = LocalTime.of(9, 0);

    /**
     * 标准下班时间
     */
    @Column(nullable = false)
    private LocalTime checkOutTime = LocalTime.of(18, 0);

    /**
     * 迟到宽限时间（分钟）
     */
    @Column(nullable = false)
    private Integer lateGraceMinutes = 10;

    /**
     * 早退宽限时间（分钟）
     */
    @Column(nullable = false)
    private Integer earlyLeaveGraceMinutes = 10;

    /**
     * 是否需要签退
     */
    @Column(nullable = false)
    private Boolean requireCheckOut = true;

    /**
     * 是否启用弹性工作制
     */
    @Column(nullable = false)
    private Boolean flexibleWorkTime = false;

    /**
     * 弹性工作制：最早签到时间
     */
    @Column
    private LocalTime flexibleCheckInStart;

    /**
     * 弹性工作制：最晚签到时间
     */
    @Column
    private LocalTime flexibleCheckInEnd;

    /**
     * 每日标准工作时长（小时）
     */
    @Column(nullable = false)
    private Double standardWorkHours = 8.0;

    /**
     * 规则创建人ID（部门经理）
     */
    @Column
    private Long createdBy;

    /**
     * 规则更新人ID
     */
    @Column
    private Long updatedBy;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
