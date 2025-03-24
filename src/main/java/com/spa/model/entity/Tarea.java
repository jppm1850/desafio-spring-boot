package com.spa.model.entity;


import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("tareas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {

    @Id
    private Long id;

    @Column
    private String titulo;

    @Column
    private String descripcion;

    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column("fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column("estado_id")
    private Long estadoId;

    @Column("usuario_id")
    private Long usuarioId;

    @Transient
    private EstadoTarea estado;

    @Transient
    private Usuario usuario;
}
