package com.example.lab_1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // SUGERENCIA 1: El onCreate está haciendo TODO (inicializar UI, datos, listeners). 
        // En Android, esto se llama "God Method". Es mejor separar en:
        // initViews(), setupData() y setupListeners() para que sea legible.
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerProductos = findViewById(R.id.spinnerProductos);
        spCurrency = findViewById(R.id.spinnerCurrency);

        informacion = findViewById(R.id.txtInfo);
        txtCantidad = findViewById(R.id.txtCantidad);
        txtTotal = findViewById(R.id.txtTotal);
        btnVenta = findViewById(R.id.btnAgregarVenta);
        txtEquivalencia = findViewById(R.id.txtEquivalencia);

        listaProductos = new ArrayList<>();

        listaProductos.add(new Producto("Cemento", 8.50));
        listaProductos.add(new Producto("Arena", 3.00));
        listaProductos.add(new Producto("Ladrillo", 0.75));
        listaProductos.add(new Producto("Varilla", 6.25));
        listaProductos.add(new Producto("Pintura", 12.00));

        adapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, listaProductos);
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinnerProductos.setAdapter(adapter);

        spinnerProductos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                informacion.setText("Producto seleccionado: ");
                producto = (Producto) parent.getItemAtPosition(position);
                informacion.append(producto.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnVenta.setOnClickListener( v -> {
            int cantidad = 0;
            
            // SUGERENCIA 2: Usar try-catch para validar entrada es "costoso" y no da buen feedback.
            // Mejor: if (txtCantidad.getText().toString().isEmpty()) { txtCantidad.setError("..."); return; }
            try {
                cantidad = Integer.parseInt(txtCantidad.getText().toString());
            } catch (Exception e) {
                Log.i("CONVERSION", "ERROR AL INTENTAR CONVERTIR LA CANTIDAD A INT");
            }

            if(cantidad <= 0 ) {
                Toast.makeText(MainActivity.this, "Ingrese una cantidad valida \n(mayor o igual a cero)", Toast.LENGTH_SHORT).show();
            } else {
                listaVenta.add(new Venta(producto, cantidad));
                Toast.makeText(MainActivity.this, "Venta agregada con exito", Toast.LENGTH_SHORT).show();
            }

            Log.i("LISTA_VENTAS", listaVenta.toString());
            
            // SUGERENCIA 3: Lógica de negocio en el Activity.
            // El cálculo (precio * cantidad) debería estar dentro de la clase Venta como getSubtotal().
            double precioActualTotal = producto.getPrecio() * cantidad ;

            nuevoTotal += precioActualTotal;
            
            // SUGERENCIA 4: Concatenación de strings y String.valueOf().
            // Es más limpio usar: String.format(Locale.getDefault(), "Total: $%.2f", nuevoTotal);
            // Esto asegura que siempre veas 2 decimales (ej: $8.50 en vez de $8.5).
            txtTotal.setText("Total de venta: " + String.valueOf(nuevoTotal));
        });

        listaMonedas = new ArrayList<>();
        listaMonedas.add("Euro");
        listaMonedas.add("MXN");
        listaMonedas.add("Q");
        adapterMonedas = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, listaMonedas);
        spCurrency.setAdapter(adapterMonedas);

        convertir = findViewById(R.id.btnConvert);

        spCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monedaActual = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        convertir.setOnClickListener(v -> {
            // ERROR CRÍTICO 1: En Java, los Strings se comparan con .equals(), NO con ==.
            // == compara si son el mismo objeto en memoria, .equals() compara el texto.
            // if (monedaActual.equals("Euro")) { ... }
            
            // SUGERENCIA 5: Escalabilidad.
            // Si te piden agregar 10 monedas más, tendrás un if/else gigante.
            // Es mejor crear una clase Moneda(nombre, factor) y que el Spinner maneje objetos Moneda.
            if (monedaActual == "Euro"){
                double nuevo = nuevoTotal / 1.08;
                txtEquivalencia.setText(String.valueOf(nuevoTotal+ " dolares, equivale a: " + String.format("%.2f", nuevo) + " Euros"));
            } else if (monedaActual == "MXN"){
                double nuevo = nuevoTotal / 0.049;
                txtEquivalencia.setText(String.valueOf(nuevoTotal+ " dolares, equivale a: " + String.format("%.2f", nuevo) + " MXN"));
            } else {
                double nuevo = nuevoTotal / 0.13;
                txtEquivalencia.setText(String.valueOf(nuevoTotal+ " dolares, equivale a: " + String.format("%.2f", nuevo) + " QUETZALES"));
            }
        });
    }

    private HashMap<String, Double> equivalencias;
    private TextView informacion;
    private TextView txtEquivalencia;
    private TextView txtCantidad;
    private TextView txtTotal;
    private Button btnVenta;
    
    // ERROR CRÍTICO 2: Uso de 'static' para variables de estado de la Activity.
    // 1. Causa fugas de memoria (Memory Leaks).
    // 2. Si rotas la pantalla, la Activity se recrea pero los static NO. 
    //    Si entras y sales de la App, el total se seguirá acumulando.
    // SOLUCIÓN: Quita el 'static' y úsalas como variables de instancia (private Producto producto).
    public static Producto producto = new Producto();
    public static String monedaActual = "";
    public static double nuevoTotal = 0;
    public static ArrayList<Venta> listaVenta = new ArrayList<>();
    
    private ArrayList<Producto> listaProductos;
    private ArrayList<String> listaMonedas;
    private ArrayAdapter<Producto> adapter;
    private ArrayAdapter<String> adapterMonedas;
    private Spinner spinnerProductos;
    private Spinner spCurrency;
    private Button convertir;
}
