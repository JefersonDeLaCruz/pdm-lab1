package com.example.lab_1;

import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab_1.models.Producto;
import com.example.lab_1.models.Venta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LISTA_VENTAS";

    private TextView informacion;
    private TextView txtSubtotal;
    private TextView txtTotal;
    private TextView txtEquivalencia;
    private EditText txtCantidad;
    private Spinner spinnerProductos;
    private Spinner spinnerCurrency;
    private Button btnAgregarVenta;
    private Button btnConvertir;

    private ArrayAdapter<Producto> adapterProductos;
    private ArrayAdapter<String> adapterMonedas;

    private final ArrayList<Producto> listaProductos = new ArrayList<>();
    private final ArrayList<Venta> listaVenta = new ArrayList<>();
    private final LinkedHashMap<String, Double> equivalenciasDolarPorUnidad = new LinkedHashMap<>();

    private Producto productoSeleccionado;
    private String monedaSeleccionada;
    private double totalVentaUsd = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupData();
        setupListeners();
        actualizarTotalesUI(0.0);
    }

    private void initViews() {
        spinnerProductos = findViewById(R.id.spinnerProductos);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);

        informacion = findViewById(R.id.txtInfo);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtTotal = findViewById(R.id.txtTotal);
        txtEquivalencia = findViewById(R.id.txtEquivalencia);
        txtCantidad = findViewById(R.id.txtCantidad);

        btnAgregarVenta = findViewById(R.id.btnAgregarVenta);
        btnConvertir = findViewById(R.id.btnConvert);
    }

    private void setupData() {
        listaProductos.add(new Producto("Cemento", 8.50));
        listaProductos.add(new Producto("Arena", 3.00));
        listaProductos.add(new Producto("Ladrillo", 0.75));
        listaProductos.add(new Producto("Varilla", 6.25));
        listaProductos.add(new Producto("Pintura", 12.00));

        adapterProductos = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                listaProductos
        );
        adapterProductos.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinnerProductos.setAdapter(adapterProductos);



        if (!listaProductos.isEmpty()) {
            productoSeleccionado = listaProductos.get(0);
            mostrarProductoSeleccionado(productoSeleccionado);
        }

        equivalenciasDolarPorUnidad.put("Euro (EUR)", 1.08);
        equivalenciasDolarPorUnidad.put("Peso mexicano (MXN)", 0.049);
        equivalenciasDolarPorUnidad.put("Quetzal (GTQ)", 0.13);

        adapterMonedas = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                new ArrayList<>(equivalenciasDolarPorUnidad.keySet())
        );
        adapterMonedas.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapterMonedas);

        if (adapterMonedas.getCount() > 0) {
            monedaSeleccionada = adapterMonedas.getItem(0);
        }
    }

    private void setupListeners() {
        spinnerProductos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                productoSeleccionado = (Producto) parent.getItemAtPosition(position);
                mostrarProductoSeleccionado(productoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productoSeleccionado = null;
            }
        });

        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                monedaSeleccionada = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                monedaSeleccionada = null;
            }
        });

        btnAgregarVenta.setOnClickListener(v -> agregarVenta());
        btnConvertir.setOnClickListener(v -> convertirTotal());
    }

    private void agregarVenta() {
        if (productoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer cantidad = validarCantidad();
        if (cantidad == null) {
            return;
        }

        Venta venta = new Venta(productoSeleccionado, cantidad);
        listaVenta.add(venta);

        double subtotal = venta.getSubtotal();
        totalVentaUsd += subtotal;

        actualizarTotalesUI(subtotal);
        imprimirVentasEnLogcat();

        txtCantidad.setText("");
        txtCantidad.clearFocus();
        Toast.makeText(this, "Venta agregada con exito", Toast.LENGTH_SHORT).show();
    }

    private Integer validarCantidad() {
        String texto = txtCantidad.getText().toString().trim();

        if (texto.isEmpty()) {
            txtCantidad.setError("La cantidad es obligatoria");
            return null;
        }

        if (!texto.matches("\\d+")) {
            txtCantidad.setError("Ingrese solo numeros enteros");
            return null;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            txtCantidad.setError("Numero invalido");
            return null;
        }

        if (cantidad <= 0) {
            txtCantidad.setError("La cantidad debe ser mayor que cero");
            return null;
        }

        txtCantidad.setError(null);
        return cantidad;
    }

    private void convertirTotal() {
        if (totalVentaUsd <= 0) {
            Toast.makeText(this, "Primero agregue al menos una venta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (monedaSeleccionada == null) {
            Toast.makeText(this, "Seleccione una moneda valida", Toast.LENGTH_SHORT).show();
            return;
        }

        Double dolarPorUnidad = equivalenciasDolarPorUnidad.get(monedaSeleccionada);
        if (dolarPorUnidad == null || dolarPorUnidad <= 0) {
            Toast.makeText(this, "No se encontro equivalencia para la moneda", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalConvertido = totalVentaUsd / dolarPorUnidad;

        txtEquivalencia.setText(String.format(
                Locale.US,
                "USD $%.2f equivalen a %.2f %s",
                totalVentaUsd,
                totalConvertido,
                monedaSeleccionada
        ));
    }

    private void mostrarProductoSeleccionado(Producto producto) {
        informacion.setText(String.format(
                Locale.US,
                "Producto seleccionado: %s - USD $%.2f",
                producto.getNombre(),
                producto.getPrecio()
        ));
    }

    private void actualizarTotalesUI(double subtotalProducto) {
        txtSubtotal.setText(String.format(Locale.US, "Subtotal producto: USD $%.2f", subtotalProducto));
        txtTotal.setText(String.format(Locale.US, "Total acumulado: USD $%.2f", totalVentaUsd));
    }

    private void imprimirVentasEnLogcat() {
        StringBuilder sb = new StringBuilder("Ventas registradas:\n");

        for (int i = 0; i < listaVenta.size(); i++) {
            Venta venta = listaVenta.get(i);
            sb.append(i + 1)
                    .append(") ")
                    .append(venta.getProducto().getNombre())
                    .append(" x")
                    .append(venta.getCantidad())
                    .append(" -> USD $")
                    .append(String.format(Locale.US, "%.2f", venta.getSubtotal()))
                    .append('\n');
        }

        Log.i(TAG, sb.toString());
    }
}
