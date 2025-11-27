# 📐 PATRONES DE DISEÑO APLICADOS EN EL SISTEMA POS MINIMARKET

## 🎯 INTRODUCCIÓN

Este documento explica detalladamente los **patrones de diseño** implementados en el Sistema POS del Minimarket, con ejemplos concretos del código y justificaciones técnicas.

---

## 1. 🔒 PATRÓN SINGLETON

### **Definición:**
Garantiza que una clase tenga una única instancia y proporciona un punto de acceso global a ella.

### **Problema que Resuelve:**
- Evita múltiples conexiones a la base de datos (costoso y problemático)
- Centraliza la gestión de recursos compartidos
- Ahorra memoria y recursos del sistema

### **Implementación en el Proyecto:**

#### **1.1. DatabaseConnection (Conexión a BD)**

**Ubicación:** `src/main/java/com/minimarket/config/DatabaseConnection.java`

**Código Clave:**
```java
public class DatabaseConnection {
    private static DatabaseConnection instance;  // Instancia única
    private Connection connection;
    
    // Constructor PRIVADO - previene instanciación externa
    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }
    
    // Método estático para obtener la instancia única (THREAD-SAFE)
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}
```

**Uso en el Proyecto:**
```java
// En cualquier parte del código:
Connection conn = DatabaseConnection.getInstance().getConnection();
```

**Ventajas:**
- ✅ Solo una conexión a PostgreSQL (eficiente)
- ✅ Thread-safe con `synchronized`
- ✅ Reutilización de la conexión existente
- ✅ Control centralizado de la configuración

---

#### **1.2. AuditoriaManager (Gestión de Auditoría)**

**Ubicación:** `src/main/java/com/minimarket/security/AuditoriaManager.java`

**Código Clave:**
```java
public class AuditoriaManager {
    private static AuditoriaManager instance;
    
    private AuditoriaManager() {
        // Constructor privado
    }
    
    public static synchronized AuditoriaManager getInstance() {
        if (instance == null) {
            instance = new AuditoriaManager();
        }
        return instance;
    }
    
    public void registrarEvento(String usuario, String tipoEvento, String detalles) {
        // Registra eventos en la base de datos
        // Ejemplo: LOGIN, LOGOUT, MODIFICACION_PRECIO, etc.
    }
}
```

**Uso en el Proyecto:**
```java
// Cuando un usuario inicia sesión:
AuditoriaManager.getInstance().registrarEvento(
    usuario.getUsername(), 
    "LOGIN", 
    "Inicio de sesión exitoso"
);
```

**Ventajas:**
- ✅ Punto único de registro de eventos
- ✅ Consistencia en el formato de auditoría
- ✅ Fácil de extender (agregar más tipos de eventos)

---

#### **1.3. UsuarioSesion (Gestión de Sesión)**

**Ubicación:** `src/main/java/com/minimarket/security/UsuarioSesion.java`

**Código Clave:**
```java
public class UsuarioSesion {
    private static UsuarioSesion instance;
    private Usuario usuarioActual;  // Usuario logueado actualmente
    
    private UsuarioSesion() {
        // Constructor privado
    }
    
    public static synchronized UsuarioSesion getInstance() {
        if (instance == null) {
            instance = new UsuarioSesion();
        }
        return instance;
    }
    
    public void login(Usuario usuario) {
        this.usuarioActual = usuario;
        AuditoriaManager.getInstance().registrarEvento(
            usuario.getUsername(), "LOGIN", "Inicio de sesión exitoso"
        );
    }
    
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
```

**Uso en el Proyecto:**
```java
// En LoginFrame.java después de autenticar:
UsuarioSesion.getInstance().login(usuarioAutenticado);

// En DashboardFrame.java para obtener el usuario actual:
Usuario usuario = UsuarioSesion.getInstance().getUsuarioActual();
```

**Ventajas:**
- ✅ Estado global del usuario logueado
- ✅ Acceso desde cualquier parte del sistema
- ✅ Integración automática con auditoría

---

## 2. 🏗️ PATRÓN BUILDER

### **Definición:**
Construye objetos complejos paso a paso, permitiendo diferentes representaciones del mismo objeto.

### **Problema que Resuelve:**
- Construcción de objetos con muchos parámetros opcionales
- Validación antes de crear el objeto
- Código más legible y mantenible

### **Implementación en el Proyecto:**

#### **Boleta.Builder (Construcción de Boletas)**

**Ubicación:** `src/main/java/com/minimarket/model/Boleta.java`

**Código Clave:**
```java
public class Boleta {
    // Constructor PRIVADO - solo se crea mediante Builder
    private Boleta() {
        this.detalles = new ArrayList<>();
        this.fechaHora = LocalDateTime.now();
    }
    
    // Clase Builder interna
    public static class Builder {
        private Boleta boleta;
        
        public Builder() {
            this.boleta = new Boleta();
        }
        
        // Métodos que retornan 'this' para encadenamiento
        public Builder setNumero(String numero) {
            boleta.numero = numero;
            return this;  // Permite encadenar métodos
        }
        
        public Builder setCajera(Usuario cajera) {
            boleta.cajera = cajera;
            return this;
        }
        
        public Builder setMetodoPago(MetodoPago metodoPago) {
            boleta.metodoPago = metodoPago;
            return this;
        }
        
        public Builder agregarProducto(Producto producto, Integer cantidad, BigDecimal precio) {
            DetalleVenta detalle = new DetalleVenta(producto, cantidad, precio);
            boleta.detalles.add(detalle);
            return this;
        }
        
        // Método final que construye y valida
        public Boleta build() {
            // Calcular totales automáticamente
            boleta.calcularTotales();
            
            // VALIDACIÓN: Solo construye si es válida
            if (!boleta.esValida()) {
                throw new IllegalStateException(
                    "La boleta no tiene todos los datos requeridos"
                );
            }
            
            return boleta;
        }
    }
}
```

**Uso en el Proyecto:**
```java
// En VentasPanel.java al crear una nueva venta:
Boleta boleta = new Boleta.Builder()
    .setNumero(generarNumeroBoleta())
    .setCajera(UsuarioSesion.getInstance().getUsuarioActual())
    .setMetodoPago(MetodoPago.EFECTIVO)
    .agregarProducto(producto1, 2, precio1)
    .agregarProducto(producto2, 1, precio2)
    .setObservaciones("Cliente frecuente")
    .build();  // Construye y valida la boleta
```

**Ventajas:**
- ✅ Código más legible (métodos descriptivos)
- ✅ Validación automática antes de crear el objeto
- ✅ Flexibilidad (puedes agregar productos en cualquier orden)
- ✅ Inmutabilidad (el objeto se crea completo y válido)

**Comparación SIN Builder:**
```java
// ❌ SIN BUILDER (problemático):
Boleta boleta = new Boleta();
boleta.setNumero("B001");
boleta.setCajera(usuario);
boleta.setMetodoPago(MetodoPago.EFECTIVO);
boleta.agregarDetalle(detalle1);
boleta.agregarDetalle(detalle2);
boleta.calcularTotales();
if (!boleta.esValida()) {
    // Error - objeto inválido ya creado
}
```

---

## 3. 🎭 PATRÓN STRATEGY

### **Definición:**
Define una familia de algoritmos, los encapsula y los hace intercambiables.

### **Problema que Resuelve:**
- Diferentes comportamientos según el contexto
- Evita múltiples condicionales (if/switch)
- Facilita agregar nuevos comportamientos

### **Implementación en el Proyecto:**

#### **3.1. Sistema de Roles (RBAC - Role-Based Access Control)**

**Ubicación:** `src/main/java/com/minimarket/security/Rol.java`

**Código Clave:**
```java
public enum Rol {
    ADMINISTRADOR("ADMIN", "Acceso total al sistema", 3),
    SUPERVISOR("SUPERVISOR", "Acceso a reportes y supervisión", 2),
    CAJERO("CAJERO", "Solo ventas y consultas de precios", 1);
    
    private final int nivel;
    
    // Cada rol tiene su propia estrategia de permisos
    public boolean puedeModificarPrecios() {
        return this == ADMINISTRADOR;  // Solo admin puede
    }
    
    public boolean puedeAccederReportes() {
        return this == ADMINISTRADOR || this == SUPERVISOR;  // Admin y supervisor
    }
    
    public boolean puedeGestionarInventario() {
        return this == ADMINISTRADOR || this == SUPERVISOR;  // Admin y supervisor
    }
}
```

**Uso en el Proyecto:**
```java
// En DashboardFrame.java - mostrar módulos según rol:
Rol rolUsuario = usuarioActual.getRol();

if (rolUsuario == Rol.ADMINISTRADOR) {
    // Mostrar todos los módulos
} else if (rolUsuario == Rol.SUPERVISOR) {
    // Ocultar "Usuarios y Permisos"
} else if (rolUsuario == Rol.CAJERO) {
    // Solo mostrar "Inicio" y "Caja/Punto de Venta"
}
```

**Ventajas:**
- ✅ Comportamiento diferente según el rol
- ✅ Fácil agregar nuevos roles
- ✅ Lógica de permisos centralizada
- ✅ Sin múltiples if/else anidados

---

#### **3.2. Métodos de Pago**

**Ubicación:** `src/main/java/com/minimarket/model/Boleta.java`

**Código Clave:**
```java
public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA_DEBITO("Tarjeta de Débito"),
    TARJETA_CREDITO("Tarjeta de Crédito"),
    YAPE("Yape"),
    PLIN("Plin"),
    TRANSFERENCIA("Transferencia Bancaria");
    
    private final String descripcion;
    
    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }
}
```

**Uso en el Proyecto:**
```java
// En MetodoPagoDialog.java:
JComboBox<Boleta.MetodoPago> comboMetodo = new JComboBox<>(Boleta.MetodoPago.values());
// El usuario selecciona un método de pago
Boleta.MetodoPago metodoSeleccionado = (Boleta.MetodoPago) comboMetodo.getSelectedItem();
```

**Ventajas:**
- ✅ Tipo seguro (no strings mágicos)
- ✅ Fácil agregar nuevos métodos de pago
- ✅ Validación en tiempo de compilación

---

## 4. ⚡ PATRÓN COMMAND

### **Definición:**
Encapsula una solicitud como un objeto, permitiendo parametrizar clientes con diferentes solicitudes.

### **Problema que Resuelve:**
- Desacopla el objeto que invoca la operación del que la ejecuta
- Permite encolar, registrar y deshacer operaciones
- Facilita agregar nuevas acciones sin modificar código existente

### **Implementación en el Proyecto:**

#### **Botones de Acceso Rápido (Dashboard)**

**Ubicación:** `src/main/java/com/minimarket/ui/swing/DashboardFrame.java`

**Código Clave:**
```java
// Método que crea botones con acciones encapsuladas
private JButton createQuickActionButton(
    String text, 
    String tooltip, 
    Color backgroundColor, 
    java.awt.event.ActionListener action  // ← COMMAND encapsulado
) {
    JButton button = new JButton(text);
    button.setToolTipText(tooltip);
    button.setBackground(backgroundColor);
    button.addActionListener(action);  // La acción se ejecuta al hacer clic
    return button;
}

// Uso: Cada botón encapsula una acción diferente
JButton btnNuevaVenta = createQuickActionButton(
    "Nueva Venta 🛒", 
    "Iniciar proceso de venta",
    new Color(40, 167, 69),
    e -> navegarAPanel("pos")  // ← COMMAND: navegar a panel de ventas
);

JButton btnInventario = createQuickActionButton(
    "Ver Inventario 📦", 
    "Revisar stock y productos",
    new Color(255, 159, 64),
    e -> navegarAPanel("inventario")  // ← COMMAND: navegar a inventario
);

JButton btnReportePDF = createQuickActionButton(
    "Reporte PDF 📄", 
    "Generar reporte de ventas del día en PDF",
    new Color(220, 53, 69),
    e -> generarReportePDF()  // ← COMMAND: generar PDF
);
```

**Ventajas:**
- ✅ Cada botón encapsula su propia acción
- ✅ Fácil agregar nuevos botones sin modificar código existente
- ✅ Las acciones pueden ser reutilizadas
- ✅ Desacoplamiento entre UI y lógica de negocio

**Sin Command Pattern (problemático):**
```java
// ❌ SIN COMMAND (todo en un solo método):
if (e.getSource() == btnNuevaVenta) {
    cardLayout.show(mainContentArea, "pos");
} else if (e.getSource() == btnInventario) {
    cardLayout.show(mainContentArea, "inventario");
} else if (e.getSource() == btnReportePDF) {
    generarReportePDF();
}
// ... muchos más if/else
```

---

## 5. 🏭 PATRÓN FACTORY / UTILITY CLASS

### **Definición:**
Proporciona una interfaz para crear objetos sin especificar sus clases exactas.

### **Problema que Resuelve:**
- Centraliza la creación de objetos complejos
- Reduce duplicación de código
- Facilita mantenimiento y cambios

### **Implementación en el Proyecto:**

#### **UIUtils (Factory de Componentes UI)**

**Ubicación:** `src/main/java/com/minimarket/ui/util/UIUtils.java`

**Código Clave:**
```java
public class UIUtils {
    // Constantes centralizadas
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    public static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    
    // Factory methods para crear componentes estilizados
    public static JPanel configurarPanel(LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        return panel;
    }
    
    public static void configurarBotonExito(JButton button) {
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(BOLD_FONT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public static void configurarBotonPeligro(JButton button) {
        button.setBackground(DANGER_COLOR);
        button.setForeground(Color.WHITE);
        // ... configuración estándar
    }
    
    public static void configurarTabla(JTable table) {
        table.setFont(DEFAULT_FONT);
        table.setRowHeight(25);
        table.getTableHeader().setFont(BOLD_FONT);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        // ... más configuración
    }
}
```

**Uso en el Proyecto:**
```java
// En InventarioPanel.java:
JButton btnAgregar = new JButton("Agregar Producto");
UIUtils.configurarBotonExito(btnAgregar);  // ← Factory method

JButton btnEliminar = new JButton("Eliminar Producto");
UIUtils.configurarBotonPeligro(btnEliminar);  // ← Factory method

JPanel panel = UIUtils.configurarPanel(new BorderLayout(), 20);  // ← Factory method
```

**Ventajas:**
- ✅ **Eliminó 361+ líneas de código duplicado** en todo el proyecto
- ✅ Consistencia visual en toda la aplicación
- ✅ Cambios globales desde un solo lugar
- ✅ Código más limpio y mantenible

**Antes (código duplicado):**
```java
// ❌ ANTES: Código repetido en cada panel
JButton btn = new JButton("Agregar");
btn.setBackground(new Color(46, 125, 50));
btn.setForeground(Color.WHITE);
btn.setFont(new Font("Arial", Font.BOLD, 12));
btn.setBorderPainted(false);
btn.setFocusPainted(false);
// ... repetido 20+ veces en diferentes archivos
```

---

## 6. 🎨 PATRÓN FACADE

### **Definición:**
Proporciona una interfaz unificada para un conjunto de interfaces en un subsistema.

### **Problema que Resuelve:**
- Simplifica interfaces complejas
- Oculta la complejidad del subsistema
- Facilita el uso del sistema

### **Implementación en el Proyecto:**

#### **UIUtils como Facade de Swing**

**Ubicación:** `src/main/java/com/minimarket/ui/util/UIUtils.java`

**Código Clave:**
```java
// Facade que simplifica operaciones complejas de Swing
public static void mostrarError(Component parent, String message) {
    JOptionPane.showMessageDialog(
        parent, 
        message, 
        "Error - Sistema POS Minimarket", 
        JOptionPane.ERROR_MESSAGE
    );
}

public static void mostrarExito(Component parent, String message) {
    JOptionPane.showMessageDialog(
        parent, 
        message, 
        "Éxito - Sistema POS Minimarket", 
        JOptionPane.INFORMATION_MESSAGE
    );
}

public static boolean confirmar(Component parent, String message) {
    int respuesta = JOptionPane.showConfirmDialog(
        parent, 
        message, 
        "Confirmar - Sistema POS Minimarket", 
        JOptionPane.YES_NO_OPTION
    );
    return respuesta == JOptionPane.YES_OPTION;
}
```

**Uso en el Proyecto:**
```java
// En lugar de escribir esto cada vez:
JOptionPane.showMessageDialog(
    this, 
    "Error al guardar", 
    "Error", 
    JOptionPane.ERROR_MESSAGE
);

// Simplemente escribes:
UIUtils.mostrarError(this, "Error al guardar");
```

**Ventajas:**
- ✅ Interfaz simplificada
- ✅ Consistencia en mensajes
- ✅ Menos código repetitivo
- ✅ Fácil cambiar el estilo de todos los diálogos

---

## 7. 🔐 PATRÓN RBAC (Role-Based Access Control)

### **Definición:**
Sistema de control de acceso basado en roles, donde los permisos se asignan a roles y los usuarios se asignan a roles.

### **Problema que Resuelve:**
- Control granular de acceso según el rol del usuario
- Seguridad y auditoría
- Escalabilidad (fácil agregar nuevos roles)

### **Implementación en el Proyecto:**

#### **Sistema Completo de Roles y Permisos**

**Componentes:**
1. **Rol.java** - Define los roles y sus permisos
2. **Usuario.java** - Asocia usuarios con roles
3. **UsuarioSesion.java** - Gestiona la sesión actual
4. **DashboardFrame.java** - Aplica permisos en la UI

**Código Clave:**
```java
// En Usuario.java:
public boolean tienePermiso(String accion) {
    if (!activo || rol == null) return false;
    
    switch (accion.toUpperCase()) {
        case "MODIFICAR_PRECIOS":
            return rol.puedeModificarPrecios();  // Solo ADMINISTRADOR
        case "ACCEDER_REPORTES":
            return rol.puedeAccederReportes();  // ADMINISTRADOR y SUPERVISOR
        case "GESTIONAR_INVENTARIO":
            return rol.puedeGestionarInventario();  // ADMINISTRADOR y SUPERVISOR
        case "REGISTRAR_VENTAS":
            return true;  // Todos los roles pueden
        default:
            return false;
    }
}

// En DashboardFrame.java:
private void createSidebarPanel() {
    Rol rolUsuario = usuarioActual.getRol();
    
    // INICIO - Todos pueden ver
    JButton btnInicio = createSidebarButton("🏠 Inicio", "inicio", true);
    
    // CAJA/POS - Todos pueden usar
    JButton btnPOS = createSidebarButton("💰 Caja / Punto de Venta", "pos", false);
    
    // INVENTARIO - Todos pueden ver (pero con permisos diferentes)
    JButton btnInventario = createSidebarButton("📦 Inventario", "inventario", false);
    
    // HISTORIAL - Solo SUPERVISOR y ADMINISTRADOR
    if (rolUsuario == Rol.SUPERVISOR || rolUsuario == Rol.ADMINISTRADOR) {
        JButton btnHistorial = createSidebarButton("📋 Historial de Ventas", "historial", false);
    }
    
    // USUARIOS - Solo ADMINISTRADOR
    if (rolUsuario == Rol.ADMINISTRADOR) {
        JButton btnUsuarios = createSidebarButton("👥 Usuarios y Permisos", "usuarios", false);
    }
}
```

**Flujo de RBAC:**
```
1. Usuario inicia sesión → LoginFrame autentica
2. UsuarioSesion.getInstance().login(usuario) → Guarda usuario actual
3. DashboardFrame obtiene rol → usuarioActual.getRol()
4. DashboardFrame muestra módulos → Según permisos del rol
5. Cada acción verifica permisos → Usuario.tienePermiso("ACCION")
```

**Ventajas:**
- ✅ Seguridad: Cajeros no pueden modificar precios
- ✅ Auditoría: Todos los eventos se registran
- ✅ Escalabilidad: Fácil agregar nuevos roles
- ✅ Mantenibilidad: Lógica centralizada

---

## 8. 📊 PATRÓN TEMPLATE METHOD (Implícito)

### **Definición:**
Define el esqueleto de un algoritmo, delegando algunos pasos a subclases.

### **Implementación en el Proyecto:**

#### **Estructura Común de Paneles**

**Ubicación:** Todos los paneles en `src/main/java/com/minimarket/ui/panels/`

**Patrón:**
```java
public class InventarioPanel extends JPanel {
    // 1. Inicializar componentes (template)
    private void initializeComponents() {
        // Crear tabla, botones, campos de búsqueda
    }
    
    // 2. Cargar datos (template)
    private void cargarProductos() {
        // Consulta SQL y llenar tabla
    }
    
    // 3. Configurar eventos (template)
    private void setupEventHandlers() {
        // ActionListeners de botones
    }
}
```

**Ventajas:**
- ✅ Estructura consistente en todos los paneles
- ✅ Fácil de entender y mantener
- ✅ Patrón común de inicialización

---

## 📈 RESUMEN DE PATRONES APLICADOS

| Patrón | Ubicación | Propósito | Impacto |
|--------|-----------|-----------|---------|
| **Singleton** | DatabaseConnection, AuditoriaManager, UsuarioSesion | Una única instancia | ⭐⭐⭐⭐⭐ Crítico |
| **Builder** | Boleta.Builder | Construcción de boletas | ⭐⭐⭐⭐ Muy útil |
| **Strategy** | Rol, MetodoPago | Comportamiento por rol | ⭐⭐⭐⭐⭐ Crítico |
| **Command** | Botones de acceso rápido | Encapsular acciones | ⭐⭐⭐⭐ Muy útil |
| **Factory** | UIUtils | Crear componentes UI | ⭐⭐⭐⭐⭐ Eliminó 361+ líneas |
| **Facade** | UIUtils | Simplificar Swing | ⭐⭐⭐⭐ Muy útil |
| **RBAC** | Sistema completo | Control de acceso | ⭐⭐⭐⭐⭐ Crítico |
| **Template Method** | Estructura de paneles | Consistencia | ⭐⭐⭐ Útil |

---

## 🎓 PUNTOS CLAVE PARA LA EXPOSICIÓN

### **1. Singleton - "Una sola conexión a la BD"**
- **Problema:** Múltiples conexiones desperdician recursos
- **Solución:** Una única instancia compartida
- **Ejemplo:** `DatabaseConnection.getInstance()`
- **Beneficio:** Eficiencia y control centralizado

### **2. Builder - "Boletas válidas siempre"**
- **Problema:** Objetos incompletos o inválidos
- **Solución:** Construcción paso a paso con validación
- **Ejemplo:** `new Boleta.Builder().setNumero("B001").build()`
- **Beneficio:** Objetos siempre válidos

### **3. Strategy - "Permisos según rol"**
- **Problema:** Múltiples if/else para cada rol
- **Solución:** Cada rol define su estrategia de permisos
- **Ejemplo:** `rol.puedeModificarPrecios()`
- **Beneficio:** Código limpio y extensible

### **4. Factory - "UI consistente"**
- **Problema:** 361+ líneas de código duplicado
- **Solución:** Métodos factory centralizados
- **Ejemplo:** `UIUtils.configurarBotonExito(btn)`
- **Beneficio:** Mantenimiento fácil, cambios globales

### **5. RBAC - "Seguridad por roles"**
- **Problema:** Control de acceso granular
- **Solución:** Sistema de roles con permisos
- **Ejemplo:** Cajeros no pueden modificar precios
- **Beneficio:** Seguridad y auditoría completa

---

## 💡 DEMOSTRACIÓN PRÁCTICA

### **Ejemplo 1: Crear una Boleta (Builder)**
```java
// Código limpio y legible:
Boleta boleta = new Boleta.Builder()
    .setNumero("B001")
    .setCajera(usuario)
    .setMetodoPago(MetodoPago.YAPE)
    .agregarProducto(producto1, 2, precio1)
    .agregarProducto(producto2, 1, precio2)
    .build();  // ← Valida automáticamente
```

### **Ejemplo 2: Control de Acceso (RBAC)**
```java
// El sistema automáticamente muestra/oculta módulos:
if (rolUsuario == Rol.CAJERO) {
    // Solo muestra: Inicio, Caja/POS, Consultar Inventario (solo lectura)
} else if (rolUsuario == Rol.ADMINISTRADOR) {
    // Muestra: Todos los módulos
}
```

### **Ejemplo 3: UI Consistente (Factory)**
```java
// Antes: 15 líneas de código por botón
// Ahora: 1 línea
UIUtils.configurarBotonExito(btnAgregar);
```

---

## ✅ CONCLUSIÓN

El proyecto implementa **8 patrones de diseño** que mejoran:
- ✅ **Mantenibilidad:** Código más limpio y organizado
- ✅ **Escalabilidad:** Fácil agregar nuevas funcionalidades
- ✅ **Seguridad:** Control de acceso granular (RBAC)
- ✅ **Eficiencia:** Reutilización de recursos (Singleton)
- ✅ **Consistencia:** UI uniforme (Factory/Facade)
- ✅ **Robustez:** Validación automática (Builder)

**Resultado:** Sistema profesional, mantenible y escalable.

---

**Documento generado para:** Exposición del Sistema POS Minimarket  
**Fecha:** Noviembre 2024  
**Autor:** Sistema de Documentación Automática

