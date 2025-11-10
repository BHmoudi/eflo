package com.eflo.order.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveOrderRequest {

    @JsonProperty("prix")
    private MovePrixData prix;

    @JsonProperty("actionCo")
    @Builder.Default
    private List<MoveActionCoData> actionCo = new ArrayList<>();

    @JsonProperty("option")
    @Builder.Default
    private List<MoveOptionData> option = new ArrayList<>();

    @JsonProperty("supplement")
    @Builder.Default
    private List<MoveSupplementData> supplement = new ArrayList<>();

    @JsonProperty("service")
    @Builder.Default
    private List<MoveServiceData> service = new ArrayList<>();

    @JsonProperty("document")
    @Builder.Default
    private List<MoveDocumentData> document = new ArrayList<>();
}
