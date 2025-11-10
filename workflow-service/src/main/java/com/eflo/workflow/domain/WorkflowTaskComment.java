package com.eflo.workflow.domain;

import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WorkflowTaskComment Entity
 * Maps to workflow_task_comments table
 * Stores comments and discussions related to task instances
 */
@Entity
@Table(name = "workflow_task_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_task_id", nullable = false)
    private WorkflowInstanceTask instanceTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private WorkflowTaskComment parentComment;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String commentText;

    @Column(name = "is_internal", nullable = false)
    @Builder.Default
    private Boolean isInternal = false;

    @Column(name = "mentions", columnDefinition = "bigint[]")
    private Long[] mentions;

    @Type(JsonBinaryType.class)
    @Column(name = "attachments", columnDefinition = "jsonb")
    private Map<String, Object> attachments;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void edit(String newText) {
        this.commentText = newText;
        this.editedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowTaskComment)) return false;
        WorkflowTaskComment that = (WorkflowTaskComment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkflowTaskComment{" +
                "id=" + id +
                ", userId=" + userId +
                ", isInternal=" + isInternal +
                ", isDeleted=" + (deletedAt != null) +
                '}';
    }
}
