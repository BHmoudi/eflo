package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowUserRoleRepository extends JpaRepository<WorkflowUserRole, Long> {

    List<WorkflowUserRole> findByUserId(Long userId);

    List<WorkflowUserRole> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT ur FROM WorkflowUserRole ur WHERE ur.userId = :userId AND ur.role.id = :roleId AND ur.isActive = true")
    Optional<WorkflowUserRole> findActiveByUserIdAndRoleId(Long userId, Long roleId);

    @Query("SELECT ur FROM WorkflowUserRole ur WHERE ur.userId = :userId " +
           "AND ur.isActive = true " +
           "AND (ur.validFrom IS NULL OR ur.validFrom <= :dateTime) " +
           "AND (ur.validTo IS NULL OR ur.validTo >= :dateTime)")
    List<WorkflowUserRole> findValidUserRolesAt(Long userId, LocalDateTime dateTime);

    @Query("SELECT ur FROM WorkflowUserRole ur WHERE ur.userId = :userId " +
           "AND (ur.affaireCode = :affaireCode OR ur.affaireCode IS NULL) " +
           "AND ur.isActive = true")
    List<WorkflowUserRole> findByUserIdAndAffaire(Long userId, String affaireCode);

    @Query("SELECT ur FROM WorkflowUserRole ur WHERE ur.role.id = :roleId AND ur.isActive = true")
    List<WorkflowUserRole> findByRoleId(Long roleId);

    @Query("SELECT DISTINCT ur.userId FROM WorkflowUserRole ur WHERE ur.role.id = :roleId AND ur.isActive = true")
    List<Long> findUserIdsByRoleId(Long roleId);
}
