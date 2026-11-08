package com.citas.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tutor {
  private String nombre;
  private String documento;
  private String telefono;
  private String correo;
}