package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowRoleHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowRoleHierarchyRepository extends JpaRepository<WorkflowRoleHierarchy, Long> {

    List<WorkflowRoleHierarchy> findByParentRoleId(Long parentRoleId);

    List<WorkflowRoleHierarchy> findByChildRoleId(Long childRoleId);

    @Query("SELECT rh FROM WorkflowRoleHierarchy rh WHERE rh.parentRole.id = :roleId AND rh.canApproveForChild = true")
    List<WorkflowRoleHierarchy> findApprovableChildRoles(Long roleId);

    @Query("SELECT rh FROM WorkflowRoleHierarchy rh WHERE rh.childRole.id = :roleId AND rh.canApproveForChild = true")
    List<WorkflowRoleHierarchy> findApprovableParentRoles(Long roleId);
}
