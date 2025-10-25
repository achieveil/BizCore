package com.achieveil.bizcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String empNo;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender;

    @Column
    private LocalDate birthDate;

    @Column(length = 18)
    private String idCard;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column
    private Long deptId;

    @Column(length = 100)
    private String position;

    @Column(length = 50)
    private String level;

    @Column
    private LocalDate hireDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(length = 200)
    private String address;

    @Column(nullable = false)
    private Integer status = 1;

    @Column
    private Long userId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;

    @Transient
    private String userRole;

    @Transient
    private Boolean manager;
}
