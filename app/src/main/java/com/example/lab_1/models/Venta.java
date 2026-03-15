package com.example.lab_1.models;

import java.util.Locale;

public class Venta {
    private final Producto producto;
    private final int cantidad;

    public Venta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), 
            "Producto: %s | Cantidad: %d | Subtotal: $%.2f", 
            producto.getNombre(), cantidad, getSubtotal());
    }
}
