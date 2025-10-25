package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDeptCode(String deptCode);
    List<Department> findByParentId(Long parentId);
    List<Department> findByStatus(Integer status);
    boolean existsByDeptCode(String deptCode);
}
