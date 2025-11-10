package com.eflo.order.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveDocumentData {

    @JsonProperty("rDocId")
    private String docId;

    @JsonProperty("fileType")
    private String fileType;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("documentType")
    private String documentType;

    @JsonProperty("personId")
    private String personId;

    @JsonProperty("signed")
    private String signed;
}
