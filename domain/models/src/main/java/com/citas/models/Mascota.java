package com.citas.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter 
@Builder
@ToString 
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {
  private String id;
  private String nombre;
  private String especie;
  private int edad;
  private Tutor tutor;
}