package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DigaApiResponse {
    private int statusCode;
    private String body;
}
