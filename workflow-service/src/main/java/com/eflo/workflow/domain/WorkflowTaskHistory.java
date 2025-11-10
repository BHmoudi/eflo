package com.eflo.workflow.domain;
import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.entity.WorkflowInstance;

import com.eflo.workflow.domain.enums.TaskActionType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "workflow_task_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_task_id", nullable = false)
    private WorkflowInstanceTask instanceTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private TaskActionType actionType;

    @Column(name = "action_by_user_id", nullable = false)
    private Long actionByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_by_role_id")
    private WorkflowRole actionByRole;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "old_assignee_user_id")
    private Long oldAssigneeUserId;

    @Column(name = "new_assignee_user_id")
    private Long newAssigneeUserId;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Type(JsonBinaryType.class)
    @Column(name = "changes", columnDefinition = "jsonb")
    private Map<String, Object> changes;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
