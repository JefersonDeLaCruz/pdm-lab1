package com.example.lab_1.models;

public class Venta {

    private Producto producto;

    private int cantidad;

    public Venta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }


    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        if (producto == null) {
            return 0;
        }
        return producto.getPrecio() * cantidad;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("[Producto: ");
      sb.append(this.producto)
              .append(" | ")
              .append("Cantidad: ")
              .append(this.cantidad)
              .append(" | ")
              .append("Subtotal: $")
              .append(String.format(java.util.Locale.US, "%.2f", getSubtotal()))
              .append(" ]");
      return sb.toString();
    }
}
