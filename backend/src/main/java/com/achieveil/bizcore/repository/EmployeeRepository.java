package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByEmpNo(String empNo);
    Optional<Employee> findByUserId(Long userId);
    List<Employee> findByDeptId(Long deptId);
    List<Employee> findByStatus(Integer status);
    List<Employee> findByNameContaining(String name);
    List<Employee> findByUserIdIn(List<Long> userIds);
    boolean existsByEmpNo(String empNo);
    long countByDeptId(Long deptId);
    long countByStatus(Integer status);
}
