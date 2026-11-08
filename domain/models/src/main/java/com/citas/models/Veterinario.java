package com.citas.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Veterinario {
  private String id;
  private String nombre;
}