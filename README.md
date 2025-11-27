# Sistema POS Minimarket

Sistema de Punto de Venta para minimarket desarrollado en Java con implementación de patrones de diseño y base de datos PostgreSQL.

## Características Principales

### Módulos Implementados
- **Punto de Venta (POS)** - Registro de ventas con carrito de compras
- **Gestión de Inventario** - CRUD de productos con manejo de lotes FIFO
- **Gestión de Usuarios** - Sistema RBAC con roles y permisos
- **Historial de Ventas** - Auditoría completa de transacciones
- **Reportes Diarios** - Métricas por trabajador

### Patrones de Diseño Aplicados

#### Patrones Creacionales
- **Singleton**: Conexión única a base de datos
- **Builder**: Construcción de reportes complejos

#### Patrones Estructurales
- **Adapter**: Adaptación entre sistemas de pago
- **Decorator**: Extensión de funcionalidades

#### Patrones Comportamentales
- **Command**: Registro de comandos de venta
- **Observer**: Alertas automáticas de stock
- **Chain of Responsibility**: Manejo de aprobaciones

### Sistema RBAC
- **Administrador**: Acceso completo
- **Supervisor**: Gestión de inventario y reportes
- **Cajero**: Registro de ventas y consultas

### Base de Datos PostgreSQL
- Esquema normalizado con triggers
- Manejo de lotes con lógica FIFO
- Integridad referencial

## Tecnologías

- Java 17 + Maven
- PostgreSQL + JDBC
- Swing GUI

## Requisitos

- Java 17+
- PostgreSQL 12+
- Maven 3.6+

## Instalación

1. **Clonar repositorio**
```bash
git clone https://github.com/Kaiilo1020/sistema-pos-minimarket.git
cd sistema-pos-minimarket
```

2. **Configurar PostgreSQL**
```sql
CREATE DATABASE minimarket_db;
\i database/minimarket_db_postgresql.sql
\i database/actualizacion_pos_personalizada.sql
```

3. **Ejecutar**
```bash
mvn clean compile exec:java
```

## Uso

**Usuarios por defecto:**
- Admin: `admin` / `admin123`
- Cajero: `cajera1` / `cajera123`

**Módulos disponibles:**
- Punto de Venta
- Inventario
- Usuarios
- Reportes
- Historial

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