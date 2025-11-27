# Sistema POS Minimarket

Sistema de Punto de Venta profesional para minimarket desarrollado en Java con implementación de patrones de diseño y base de datos PostgreSQL.

## Características Principales

### Módulos del Sistema
- **Punto de Venta** - Interfaz de caja con carrito de compras y facturación
- **Gestión de Inventario** - CRUD de productos con manejo de lotes y vencimientos
- **Gestión de Usuarios** - Sistema RBAC con roles y permisos diferenciados
- **Historial de Ventas** - Auditoría completa de transacciones
- **Reportes de Ventas** - Métricas detalladas por trabajador
- **Alertas de Vencimiento** - Monitoreo automático de productos próximos a vencer

### Patrones de Diseño Implementados

#### Patrones Creacionales
- **Singleton**: Gestión única de conexión a base de datos y sesión de usuario

#### Patrones Estructurales
- **Adapter**: Integración entre diferentes sistemas de pago

#### Patrones Comportamentales
- **Command**: Registro y ejecución de comandos de venta
- **Observer**: Sistema de alertas automáticas de stock crítico
- **Chain of Responsibility**: Manejo de flujo de aprobaciones

### Sistema de Seguridad RBAC
- **Administrador**: Acceso completo al sistema
- **Supervisor**: Gestión de inventario, usuarios y reportes
- **Cajero**: Registro de ventas y consulta de precios (acceso limitado)

### Características Técnicas
- **Base de Datos**: PostgreSQL con esquema normalizado
- **Manejo de Inventario**: Lógica FIFO para productos con lote
- **Validación Flexible**: Permite ventas sin lote con logging de advertencias
- **Integridad de Datos**: Timestamps exactos y métodos de pago obligatorios
- **Alertas No Bloqueantes**: Notificaciones de stock crítico sin interrumpir ventas

## Tecnologías Utilizadas

- **Java 17** - Lenguaje principal
- **Maven** - Gestión de dependencias
- **PostgreSQL** - Base de datos relacional
- **JDBC** - Conectividad con base de datos
- **Java Swing** - Interfaz gráfica de usuario

## Requisitos del Sistema

- Java 17 o superior
- PostgreSQL 12 o superior
- Maven 3.6 o superior
- 4GB RAM mínimo
- 500MB espacio en disco

## Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone https://github.com/Kaiilo1020/sistema-pos-minimarket.git
cd sistema-pos-minimarket
```

### 2. Configurar Base de Datos
```sql
-- Crear base de datos
CREATE DATABASE minimarket_db;

-- Ejecutar scripts (en orden)
\i database/minimarket_db_postgresql.sql
\i database/actualizacion_pos_personalizada.sql
```

### 3. Configurar Conexión
Verificar credenciales en `DatabaseConnection.java`:
- **Host**: localhost:5432
- **Database**: minimarket_db
- **Usuario**: postgres
- **Password**: postgres

### 4. Compilar y Ejecutar
```bash
# Compilar proyecto
mvn clean compile

# Ejecutar aplicación
mvn exec:java -Dexec.mainClass="com.minimarket.Main"
```

## Uso del Sistema

### Credenciales por Defecto
- **Administrador**: `admin` / `admin123`
- **Cajero**: `cajera1` / `cajera123`

### Navegación Principal
- **Inicio** - Dashboard con resumen del sistema
- **Caja** - Punto de venta y facturación
- **Inventario** - Gestión de productos
- **Usuarios** - Administración de personal
- **Historial** - Consulta de ventas realizadas
- **Reportes** - Métricas y estadísticas
- **Alertas** - Productos próximos a vencer

## Arquitectura del Proyecto

```
src/main/java/com/minimarket/
├── config/              # Configuración de base de datos
├── model/               # Modelos de datos (Producto, Usuario, etc.)
├── security/            # Sistema RBAC y auditoría
├── ui/
│   ├── panels/          # Paneles de la interfaz
│   ├── swing/           # Componentes Swing principales
│   └── util/            # Utilidades de UI
├── util/                # Utilidades generales
└── Main.java            # Punto de entrada de la aplicación

database/
├── minimarket_db_postgresql.sql      # Esquema base
└── actualizacion_pos_personalizada.sql  # Actualizaciones

lib/
└── postgresql-42.7.8.jar            # Driver PostgreSQL
```

## Funcionalidades Destacadas

### Sistema de Ventas
- Carrito de compras interactivo
- Cálculo automático de totales con precisión decimal
- Selección de método de pago
- Generación de boletas con timestamp exacto

### Gestión de Inventario
- CRUD completo de productos
- Manejo de lotes con fechas de vencimiento
- Lógica FIFO automática
- Alertas de stock crítico (≤ 10 unidades)

### Seguridad y Auditoría
- Autenticación por roles
- Registro de todas las operaciones sensibles
- Sesión de usuario segura
- Bloqueo de funciones según permisos

### Reportes y Análisis
- Historial completo de ventas
- Métricas por trabajador
- Productos próximos a vencer
- Exportación de datos

## Contribución

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

## Autor

**Andre Kailo** - [GitHub](https://github.com/Kaiilo1020)

---

*Sistema desarrollado como proyecto académico para el curso de Patrones de Diseño*