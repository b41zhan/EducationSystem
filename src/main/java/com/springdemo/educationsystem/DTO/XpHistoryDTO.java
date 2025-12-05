package com.springdemo.educationsystem.DTO;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class XpHistoryDTO {
    private LocalDate date;
    private Integer xpGained;
    private Integer totalXp;
}
