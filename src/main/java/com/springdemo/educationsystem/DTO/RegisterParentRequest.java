package com.springdemo.educationsystem.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegisterParentRequest {
    private String email;
    private String passwordHash;   // у тебя так в формах
    private String firstName;
    private String lastName;
    private String patronymic;

    private List<Long> studentIds; // выбранные дети
}
