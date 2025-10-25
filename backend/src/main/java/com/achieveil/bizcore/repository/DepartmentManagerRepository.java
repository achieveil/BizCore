package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.DepartmentManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentManagerRepository extends JpaRepository<DepartmentManager, Long> {

    // 查找部门的所有经理
    List<DepartmentManager> findByDeptId(Long deptId);

    // 查找经理管理的所有部门
    List<DepartmentManager> findByManagerId(Long managerId);

    // 检查某人是否是某部门的经理
    Optional<DepartmentManager> findByDeptIdAndManagerId(Long deptId, Long managerId);

    // 检查某人是否是某部门的经理（boolean返回）
    boolean existsByDeptIdAndManagerId(Long deptId, Long managerId);

    // 删除部门的所有经理
    @Modifying
    @Query("DELETE FROM DepartmentManager dm WHERE dm.deptId = :deptId")
    void deleteByDeptId(Long deptId);

    // 删除特定部门的特定经理
    @Modifying
    @Query("DELETE FROM DepartmentManager dm WHERE dm.deptId = :deptId AND dm.managerId = :managerId")
    void deleteByDeptIdAndManagerId(Long deptId, Long managerId);
}
