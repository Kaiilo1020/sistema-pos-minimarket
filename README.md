# 🏪 Sistema POS Minimarket - Patrones de Diseño

Sistema de Punto de Venta (POS) para minimarket desarrollado en Java con implementación de múltiples patrones de diseño y base de datos PostgreSQL.

## 🎯 **Características Principales**

### ✅ **Módulos Implementados**
- **🛒 Punto de Venta (POS)** - Registro completo de ventas con carrito de compras
- **📦 Gestión de Inventario** - CRUD de productos con manejo de lotes y fechas de vencimiento
- **👥 Gestión de Usuarios** - Sistema de usuarios con roles y permisos (RBAC)
- **📄 Historial de Ventas** - Auditoría completa de todas las transacciones
- **📊 Reportes Diarios** - Métricas por trabajador y totales del día

### 🏗️ **Patrones de Diseño Aplicados**

#### **Patrones Creacionales**
- **Singleton**: Conexión única a base de datos (`DatabaseConnection`)
- **Builder**: Construcción de boletas y reportes complejos

#### **Patrones Estructurales**
- **Adapter**: Adaptación entre diferentes sistemas de pago
- **Decorator**: Extensión de funcionalidades de productos

#### **Patrones Comportamentales**
- **Command**: Registro de comandos de venta
- **Observer**: Alertas automáticas de stock bajo
- **Chain of Responsibility**: Manejo de aprobaciones por niveles

### 🔐 **Sistema RBAC (Role-Based Access Control)**
- **Administrador**: Acceso completo al sistema
- **Supervisor**: Gestión de inventario y reportes
- **Cajero**: Solo registro de ventas y consulta de precios

### 🗄️ **Base de Datos PostgreSQL**
- Esquema completo con 10+ tablas
- Triggers automáticos para auditoría
- Manejo de lotes con lógica FIFO
- Integridad referencial garantizada

## 🛠️ **Tecnologías Utilizadas**

- **Java 17** - Lenguaje principal
- **Maven** - Gestión de dependencias
- **PostgreSQL** - Base de datos
- **Swing** - Interfaz gráfica de usuario
- **JDBC** - Conectividad con base de datos

## 📋 **Requisitos del Sistema**

- Java 17 o superior
- PostgreSQL 12 o superior
- Maven 3.6 o superior
- 4GB RAM mínimo
- 500MB espacio en disco

## 🚀 **Instalación y Configuración**

### 1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/sistema-pos-minimarket.git
cd sistema-pos-minimarket
```

### 2. **Configurar PostgreSQL**
```sql
-- Crear base de datos
CREATE DATABASE minimarket_db;

-- Ejecutar script de creación
\i database/minimarket_db_postgresql.sql

-- Ejecutar script de actualización
\i database/actualizacion_pos_personalizada.sql
```

### 3. **Configurar conexión**
Editar `src/main/java/com/minimarket/patterns/creational/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/minimarket_db";
private static final String USERNAME = "tu_usuario";
private static final String PASSWORD = "tu_password";
```

### 4. **Compilar y ejecutar**
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn exec:java
```

## 📱 **Uso del Sistema**

### **Login Inicial**
- **Admin**: `admin` / `admin123`
- **Cajero**: `cajera1` / `cajera123`

### **Navegación Principal**
1. **🏠 Inicio** - Dashboard principal
2. **🛒 Caja/POS** - Registro de ventas
3. **📦 Inventario** - Gestión de productos
4. **📄 Historial** - Auditoría de ventas
5. **📊 Reportes** - Métricas diarias
6. **👥 Usuarios** - Gestión de personal (Solo Admin)

## 🎨 **Capturas de Pantalla**

### Dashboard Principal
![Dashboard](docs/screenshots/dashboard.png)

### Punto de Venta
![POS](docs/screenshots/pos.png)

### Gestión de Inventario
![Inventario](docs/screenshots/inventario.png)

## 🏆 **Problemas Solucionados**

### **Problema #1: Control de Acceso**
- ❌ **Antes**: Cajeras con acceso administrativo
- ✅ **Después**: RBAC con roles específicos y auditoría

### **Problema #2: Manejo de Lotes**
- ❌ **Antes**: Sistema bloqueaba ventas sin lote
- ✅ **Después**: Validación flexible con lógica FIFO

### **Problema #3: Datos de Facturación**
- ❌ **Antes**: Boletas sin hora exacta ni método de pago
- ✅ **Después**: Timestamp completo y método obligatorio

### **Problema #4: Alertas de Stock**
- ❌ **Antes**: Sin notificaciones de stock crítico
- ✅ **Después**: Alertas automáticas no bloqueantes

## 📁 **Estructura del Proyecto**

```
sistema-pos-minimarket/
├── src/main/java/com/minimarket/
│   ├── gui/
│   │   ├── panels/          # Paneles de la interfaz
│   │   └── swing/           # Componentes Swing
│   ├── patterns/
│   │   ├── creational/      # Patrones creacionales
│   │   ├── structural/      # Patrones estructurales
│   │   └── behavioral/      # Patrones comportamentales
│   ├── models/              # Modelos de datos
│   ├── security/            # Sistema RBAC
│   ├── inventory/           # Gestión de inventario
│   ├── billing/             # Facturación
│   └── alerts/              # Sistema de alertas
├── database/
│   ├── minimarket_db_postgresql.sql
│   └── actualizacion_pos_personalizada.sql
├── lib/
│   └── postgresql-42.7.8.jar
└── docs/
    └── screenshots/
```

## 🤝 **Contribución**

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 **Licencia**

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👨‍💻 **Autor**

**Andre** - *Desarrollo completo* - [Tu GitHub](https://github.com/tu-usuario)

## 🙏 **Agradecimientos**

- Curso de Patrones de Diseño
- Comunidad Java
- PostgreSQL Team
- Swing Documentation

---

⭐ **¡No olvides dar una estrella si te gustó el proyecto!** ⭐