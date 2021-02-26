package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class DigaApiRequest {
    private final String mediaType = "multipart/form-data";
    @Builder.Default
    private String verfahren = "EDFC0";
    @NonNull
    private String url;
    @NonNull
    private String senderIK;
    @NonNull
    private String recipientIK;
    @NonNull
    private byte[] encryptedContent;

}