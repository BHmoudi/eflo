package com.eflo.docgen.domain.repository;

import com.eflo.docgen.domain.entity.DocumentTemplate;
import com.eflo.docgen.domain.enums.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    Optional<DocumentTemplate> findByTemplateCode(String templateCode);

    List<DocumentTemplate> findByTemplateType(TemplateType templateType);

    List<DocumentTemplate> findByIsActiveTrue();

    List<DocumentTemplate> findByTemplateTypeAndIsActiveTrue(TemplateType templateType);

    @Query("SELECT t FROM DocumentTemplate t WHERE t.applicableOrderTypes LIKE %:orderType% AND t.isActive = true")
    List<DocumentTemplate> findByOrderType(@Param("orderType") String orderType);

    @Query("SELECT t FROM DocumentTemplate t WHERE :stateCode MEMBER OF t.autoGenerateOnStates AND t.isActive = true")
    List<DocumentTemplate> findByAutoGenerateOnState(@Param("stateCode") String stateCode);

    boolean existsByTemplateCode(String templateCode);
}
