package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRoleRepository extends JpaRepository<WorkflowRole, Long> {

    Optional<WorkflowRole> findByRoleCode(String roleCode);

    List<WorkflowRole> findByIsActiveTrue();

    List<WorkflowRole> findByIsActiveTrueOrderByLevelAsc();

    @Query("SELECT r FROM WorkflowRole r WHERE r.level <= :level AND r.isActive = true ORDER BY r.level ASC")
    List<WorkflowRole> findHigherOrEqualRoles(Integer level);

    @Query("SELECT r FROM WorkflowRole r WHERE r.level >= :level AND r.isActive = true ORDER BY r.level ASC")
    List<WorkflowRole> findLowerOrEqualRoles(Integer level);

    boolean existsByRoleCode(String roleCode);
}
