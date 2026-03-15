package com.example.lab_1.models;

import androidx.annotation.NonNull;

public class Moneda {
    private final String nombre;
    private final double factorDolar; // Cuánto vale 1 unidad de esta moneda en dólares

    public Moneda(String nombre, double factorDolar) {
        this.nombre = nombre;
        this.factorDolar = factorDolar;
    }

    public String getNombre() {
        return nombre;
    }

    public double convertirDesdeDolar(double dolares) {
        return dolares / factorDolar;
    }

    @NonNull
    @Override
    public String toString() {
        return nombre;
    }
}
