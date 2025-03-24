package com.spa.model.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("estados_tarea")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoTarea {

    @Id
    private Long id;

    @Column
    private String nombre;
}
