package com.achieveil.bizcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "attendances")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long empId;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column
    private LocalTime checkInTime;

    @Column
    private LocalTime checkOutTime;

    @Column(length = 20)
    private String status = "NORMAL";

    /**
     * 是否迟到
     */
    @Column(nullable = false)
    private Boolean isLate = false;

    /**
     * 迟到分钟数
     */
    @Column
    private Integer lateMinutes = 0;

    /**
     * 是否早退
     */
    @Column(nullable = false)
    private Boolean isEarlyLeave = false;

    /**
     * 早退分钟数
     */
    @Column
    private Integer earlyLeaveMinutes = 0;

    /**
     * 实际工作时长（小时）
     */
    @Column
    private Double workHours;

    /**
     * 部门ID（冗余字段，便于查询）
     */
    @Column
    private Long deptId;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
