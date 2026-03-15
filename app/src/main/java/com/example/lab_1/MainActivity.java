package com.example.lab_1;

import android.os.Bundle;
import android.util.Log;
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

import com.example.lab_1.models.Moneda;
import com.example.lab_1.models.Producto;
import com.example.lab_1.models.Venta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "VentaApp";

    private Spinner spinnerProductos, spinnerMonedas;
    private EditText etCantidad;
    private TextView tvInfoProducto, tvTotalVenta, tvEquivalencia;
    private Button btnAgregarVenta, btnConvertir;

    private List<Producto> productos;
    private List<Moneda> monedas;
    private final List<Venta> ventasRealizadas = new ArrayList<>();
    private double totalVentaDolares = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Manejo de Insets para diseño Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupData();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        spinnerProductos = findViewById(R.id.spinnerProductos);
        spinnerMonedas = findViewById(R.id.spinnerCurrency);
        etCantidad = findViewById(R.id.txtCantidad);
        tvInfoProducto = findViewById(R.id.txtInfo);
        tvTotalVenta = findViewById(R.id.txtTotal);
        tvEquivalencia = findViewById(R.id.txtEquivalencia);
        btnAgregarVenta = findViewById(R.id.btnAgregarVenta);
        btnConvertir = findViewById(R.id.btnConvert);
    }

    private void setupData() {
        productos = new ArrayList<>();
        productos.add(new Producto("Cemento", 8.50));
        productos.add(new Producto("Arena", 3.00));
        productos.add(new Producto("Ladrillo", 0.75));
        productos.add(new Producto("Varilla", 6.25));
        productos.add(new Producto("Pintura", 12.00));

        monedas = new ArrayList<>();
        monedas.add(new Moneda("Euro", 1.08));
        monedas.add(new Moneda("Peso Mexicano", 0.049));
        monedas.add(new Moneda("Quetzal", 0.13));
    }

    private void setupSpinners() {
        ArrayAdapter<Producto> adapterProductos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, productos);
        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductos.setAdapter(adapterProductos);

        ArrayAdapter<Moneda> adapterMonedas = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, monedas);
        adapterMonedas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonedas.setAdapter(adapterMonedas);
    }

    private void setupListeners() {
        btnAgregarVenta.setOnClickListener(v -> procesarVenta());
        btnConvertir.setOnClickListener(v -> realizarConversion());
    }

    private void procesarVenta() {
        String strCantidad = etCantidad.getText().toString().trim();

        // III. Validaciones de Entrada
        if (strCantidad.isEmpty()) {
            etCantidad.setError("Ingrese una cantidad");
            return;
        }

        try {
            int cantidad = Integer.parseInt(strCantidad);

            if (cantidad <= 0) {
                etCantidad.setError("La cantidad debe ser mayor a cero");
                return;
            }

            // IV. Cálculo y Acumulación
            Producto productoSeleccionado = (Producto) spinnerProductos.getSelectedItem();
            Venta nuevaVenta = new Venta(productoSeleccionado, cantidad);
            
            ventasRealizadas.add(nuevaVenta);
            totalVentaDolares += nuevaVenta.getSubtotal();

            // V. Visualización de Resultados
            tvInfoProducto.setText(String.format(Locale.getDefault(), 
                    "Subtotal item: $%.2f", nuevaVenta.getSubtotal()));
            tvTotalVenta.setText(String.format(Locale.getDefault(), 
                    "Total acumulado: $%.2f", totalVentaDolares));
            
            etCantidad.setText("");
            Toast.makeText(this, "Venta agregada con éxito", Toast.LENGTH_SHORT).show();

            // VI. Trazabilidad (Logcat)
            loguearVentas();

        } catch (NumberFormatException e) {
            etCantidad.setError("Ingrese un número válido");
        }
    }

    private void realizarConversion() {
        if (totalVentaDolares == 0) {
            Toast.makeText(this, "Primero agregue alguna venta", Toast.LENGTH_SHORT).show();
            return;
        }

        // VII. Conversión de Monedas
        Moneda monedaSeleccionada = (Moneda) spinnerMonedas.getSelectedItem();
        double totalConvertido = monedaSeleccionada.convertirDesdeDolar(totalVentaDolares);

        tvEquivalencia.setText(String.format(Locale.getDefault(),
                "Total en %s: %.2f", 
                monedaSeleccionada.getNombre(), totalConvertido));
    }

    private void loguearVentas() {
        Log.i(TAG, "--- Listado de Productos Vendidos ---");
        for (Venta v : ventasRealizadas) {
            Log.i(TAG, v.toString());
        }
        Log.i(TAG, String.format(Locale.getDefault(), "TOTAL GENERAL: $%.2f", totalVentaDolares));
    }
}
