package com.example.sample.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private Long id;
    private Double amount;
    private LocalDateTime date;
    private String type;
}
