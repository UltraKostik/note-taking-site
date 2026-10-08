package com.example.note_taking_site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteRequest {

    @NotBlank(message = "Название обязательно")
    @Size(max = 120, message = "Название не длиннее 120 символов")
    private String name;

    @Size(max = 10000, message = "Описание не длиннее 10000 символов")
    private String description;
}