# 📐 Diagrama UML - Sistema POS Minimarket

## 🎯 Descripción

Este documento contiene el diagrama UML de clases del Sistema POS Minimarket, mostrando la arquitectura completa, los 7 patrones de diseño implementados y las relaciones entre clases.

## 📊 Visualización del Diagrama

### Opción 1: PlantUML (Recomendado)

El archivo `diagrama_uml.puml` contiene el diagrama completo en formato PlantUML.

**Para visualizar:**

1. **Online (Gratis):**
   - Visita: http://www.plantuml.com/plantuml/uml/
   - Copia y pega el contenido de `diagrama_uml.puml`
   - O sube el archivo directamente

2. **VS Code:**
   - Instala la extensión "PlantUML"
   - Abre `diagrama_uml.puml`
   - Presiona `Alt + D` para previsualizar

3. **IntelliJ IDEA:**
   - Instala el plugin "PlantUML integration"
   - Abre `diagrama_uml.puml`
   - Click derecho → "Preview PlantUML Diagram"

4. **Herramientas Online:**
   - [PlantText](https://www.planttext.com/)
   - [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/)

### Opción 2: Exportar a Imagen

Una vez visualizado en PlantUML, puedes exportar a:
- PNG (para presentaciones)
- SVG (para documentos)
- PDF (para documentación)

## 🎨 Leyenda de Colores

El diagrama utiliza colores para identificar los patrones:

| Color | Patrón | Clases |
|-------|--------|--------|
| 🔴 **SINGLETON_COLOR** | Singleton | `DatabaseConnection`, `AuditoriaManager`, `UsuarioSesion` |
| 🔵 **BUILDER_COLOR** | Builder | `BoletaBuilder` |
| 🟢 **ADAPTER_COLOR** | Adapter | `DashboardFrame` |
| 🟡 **DECORATOR_COLOR** | Decorator | `VentasPanel` |
| 🟣 **OBSERVER_COLOR** | Observer | `VentaService` (método `notificarStockCritico`) |
| 🌸 **COMMAND_COLOR** | Command | `VentaHandler` (métodos `ejecutarAnular`, `ejecutarImprimir`) |
| 🔵 **CHAIN_COLOR** | Chain of Responsibility | `VentaService` (método `validarVenta`) |

## 📋 Estructura del Diagrama

### Paquetes Principales

1. **`com.minimarket.config`**
   - `DatabaseConnection` (Singleton)

2. **`com.minimarket.model`**
   - `Boleta`, `BoletaBuilder` (Builder), `DetalleVenta`, `Producto`, `Usuario`

3. **`com.minimarket.security`**
   - `AuditoriaManager` (Singleton), `UsuarioSesion` (Singleton), `Rol` (enum)

4. **`com.minimarket.dao`**
   - `VentaDAO`, `ProductoDAO`, `UsuarioDAO`

5. **`com.minimarket.service.venta`**
   - `VentaService` (Chain + Observer), `VentaContext`

6. **`com.minimarket.ui.handlers`**
   - `VentaHandler` (Command), `ProductoHandler`, `UsuarioHandler`

7. **`com.minimarket.ui.swing`**
   - `DashboardFrame` (Adapter), `LoginFrame`

8. **`com.minimarket.ui.panels`**
   - `VentasPanel` (Decorator), `InventarioPanel`, `AlertasVencimientoPanel`

9. **`com.minimarket.service`**
   - `ReportePDFService`, `DashboardService`

## 🔗 Relaciones Clave

### Singleton Pattern
```
DatabaseConnection → getInstance() → DatabaseConnection (única instancia)
AuditoriaManager → getInstance() → AuditoriaManager (única instancia)
UsuarioSesion → getInstance() → UsuarioSesion (única instancia)
```

### Builder Pattern
```
VentaService → BoletaBuilder → Boleta (construcción validada)
```

### Adapter Pattern
```
DashboardFrame → obtenerOpcionesSidebar(Rol) → List<SidebarOption>
  - CAJERO: opciones limitadas
  - SUPERVISOR: opciones operativas
  - ADMINISTRADOR: acceso completo
```

### Decorator Pattern
```
VentasPanel → CustomTableCellRenderer → JTable (decora filas)
```

### Observer Pattern
```
VentaService → notificarStockCritico() → AuditoriaManager.registrarEvento()
```

### Command Pattern
```
VentasPanel → VentaHandler.ejecutarAnular()
VentasPanel → VentaHandler.ejecutarImprimir()
```

### Chain of Responsibility Pattern
```
VentaService.validarVenta():
  1. Validar Cliente (si FACTURA)
  2. Validar Carrito
  3. Validar Autorización Supervisor
```

### ACID Transactions
```
VentaService.registrarVenta():
  DatabaseConnection.beginTransaction()
  → VentaDAO.registrarVenta()
  → VentaDAO.registrarDetalle()
  → ProductoDAO.descontarStock()
  → DatabaseConnection.commit() / rollback()
```

## 📸 Capturas Recomendadas

Para la exposición, se recomienda capturar:

1. **Diagrama completo** (vista general)
2. **Sección Singleton** (zoom en DatabaseConnection, AuditoriaManager, UsuarioSesion)
3. **Sección Builder** (zoom en BoletaBuilder y su relación con Boleta)
4. **Sección Adapter** (zoom en DashboardFrame y Rol)
5. **Sección Observer** (zoom en VentaService → AuditoriaManager)
6. **Sección Command** (zoom en VentaHandler)
7. **Sección Chain of Responsibility** (zoom en VentaService.validarVenta)
8. **Sección ACID** (zoom en transacciones)

## 🛠️ Personalización

Si necesitas modificar el diagrama:

1. Edita `diagrama_uml.puml`
2. Agrega o quita clases según necesites
3. Ajusta colores cambiando las constantes `!define`
4. Agrega notas con `note right/left of Clase`

## 📚 Referencias

- **PlantUML Documentation:** https://plantuml.com/class-diagram
- **UML Class Diagram Guide:** https://www.uml-diagrams.org/class-diagrams.html

---

**Nota:** Este diagrama está optimizado para mostrar claramente los 7 patrones de diseño implementados y las transacciones ACID, facilitando la explicación durante la exposición.

