package com.achieveil.bizcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String deptName;

    @Column(nullable = false, unique = true, length = 50)
    private String deptCode;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Long parentId = 0L;

    @Column(nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
