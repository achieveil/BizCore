package com.achieveil.bizcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "department_managers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"deptId", "managerId"})
})
public class DepartmentManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long deptId;

    @Column(nullable = false)
    private Long managerId;

    @Column
    private Long assignedBy;

    @Column
    private LocalDateTime assignedTime;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
