package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.DepartmentAttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentAttendanceRuleRepository extends JpaRepository<DepartmentAttendanceRule, Long> {

    /**
     * 根据部门ID查询考勤规则
     */
    Optional<DepartmentAttendanceRule> findByDeptId(Long deptId);

    /**
     * 检查部门是否已有考勤规则
     */
    boolean existsByDeptId(Long deptId);
}
