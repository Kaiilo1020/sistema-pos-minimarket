# 🐘 Configuración de PostgreSQL para Sistema de Minimarket

## 📋 Requisitos Previos

1. **PostgreSQL instalado** (versión 12 o superior)
2. **Java 17** o superior
3. **Maven** (opcional, para gestión de dependencias)

## 🚀 Pasos de Instalación

### 1. Instalar PostgreSQL

#### En Windows:
```bash
# Descargar desde: https://www.postgresql.org/download/windows/
# O usar chocolatey:
choco install postgresql
```

#### En Ubuntu/Debian:
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

#### En macOS:
```bash
# Con Homebrew:
brew install postgresql
brew services start postgresql
```

### 2. Configurar PostgreSQL

```bash
# Conectar como superusuario
sudo -u postgres psql

# O en Windows (desde cmd como administrador):
psql -U postgres
```

### 3. Crear la Base de Datos

```sql
-- Crear la base de datos
CREATE DATABASE minimarket_db;

-- Crear usuario específico (opcional pero recomendado)
CREATE USER minimarket_app WITH PASSWORD 'app_password_123';

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE minimarket_db TO minimarket_app;

-- Conectar a la base de datos
\c minimarket_db;

-- Otorgar permisos en el esquema
GRANT ALL ON SCHEMA public TO minimarket_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO minimarket_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO minimarket_app;

-- Salir
\q
```

### 4. Ejecutar el Script de Creación

```bash
# Opción 1: Desde línea de comandos
psql -U postgres -d minimarket_db -f database/minimarket_db_postgresql.sql

# Opción 2: Desde psql
psql -U postgres -d minimarket_db
\i database/minimarket_db_postgresql.sql

# Opción 3: Con usuario específico
psql -U minimarket_app -d minimarket_db -f database/minimarket_db_postgresql.sql
```

## ⚙️ Configuración de Conexión en Java

### Actualizar DatabaseConnection.java

El archivo ya está configurado para PostgreSQL:

```java
// Configuración actual
private static final String URL = "jdbc:postgresql://localhost:5432/minimarket_db";
private static final String USERNAME = "postgres";  // o "minimarket_app"
private static final String PASSWORD = "password";  // tu password real
```

### Variables de Entorno (Recomendado para Producción)

```bash
# Crear archivo .env o configurar variables del sistema
export DB_URL=jdbc:postgresql://localhost:5432/minimarket_db
export DB_USERNAME=minimarket_app
export DB_PASSWORD=tu_password_seguro
```

## 📦 Dependencias Maven

El archivo `pom.xml` ya incluye:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```

## 🔧 Comandos de Compilación y Ejecución

### Con Maven:
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn exec:java -Dexec.mainClass="com.minimarket.SistemaVentasMinimarket"

# Crear JAR ejecutable
mvn clean package

# Ejecutar JAR
java -jar target/sistema-ventas-minimarket-1.0.0.jar
```

### Sin Maven (compilación manual):
```bash
# Descargar driver PostgreSQL
# https://jdbc.postgresql.org/download.html

# Compilar
javac -cp "postgresql-42.7.1.jar" -d bin src/main/java/com/minimarket/**/*.java

# Ejecutar
java -cp "bin:postgresql-42.7.1.jar" com.minimarket.SistemaVentasMinimarket
```

## 🗃️ Estructura de la Base de Datos

### Tablas Principales:
- **usuarios** - Cajeras, supervisores, administradores
- **productos** - Inventario con lotes y fechas
- **ventas** - Registro de ventas completas
- **detalle_ventas** - Productos vendidos por boleta
- **auditoria_precios** - Historial de cambios de precios
- **alertas_inventario** - Alertas automáticas del sistema
- **solicitudes_aprobacion** - Cadena de aprobaciones
- **configuracion_sistema** - Parámetros configurables
- **logs_sistema** - Registro de eventos

### Datos Iniciales Incluidos:
- ✅ **4 usuarios** (1 admin, 1 supervisor, 2 cajeras)
- ✅ **7 categorías** de productos
- ✅ **8 productos** de ejemplo con diferentes configuraciones
- ✅ **Configuración** del sistema
- ✅ **Índices** para rendimiento
- ✅ **Triggers** para alertas automáticas
- ✅ **Vistas** para consultas frecuentes

## 🔍 Verificación de la Instalación

### 1. Conectar a la base de datos:
```sql
psql -U postgres -d minimarket_db

-- Verificar tablas creadas
\dt

-- Verificar datos iniciales
SELECT * FROM usuarios;
SELECT * FROM productos;
SELECT * FROM configuracion_sistema;
```

### 2. Probar la conexión desde Java:
```bash
# Ejecutar el sistema
java com.minimarket.SistemaVentasMinimarket

# Debe mostrar:
# 🚀 Inicializando Sistema de Ventas del Minimarket...
# ✅ Sistema inicializado correctamente
```

## 🛠️ Solución de Problemas Comunes

### Error de Conexión:
```
org.postgresql.util.PSQLException: Connection refused
```
**Solución:**
- Verificar que PostgreSQL esté ejecutándose: `sudo service postgresql status`
- Verificar puerto: `sudo netstat -plunt | grep postgres`
- Verificar configuración en `pg_hba.conf`

### Error de Autenticación:
```
org.postgresql.util.PSQLException: password authentication failed
```
**Solución:**
- Verificar usuario y contraseña
- Resetear contraseña: `sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'nueva_password';"`

### Error de Driver:
```
java.lang.ClassNotFoundException: org.postgresql.Driver
```
**Solución:**
- Verificar que el JAR de PostgreSQL esté en el classpath
- Con Maven: `mvn clean install`

## 📊 Consultas Útiles

### Ver productos con stock bajo:
```sql
SELECT * FROM vista_productos_stock_bajo;
```

### Ver ventas del día:
```sql
SELECT * FROM vista_ventas_hoy;
```

### Ver alertas pendientes:
```sql
SELECT * FROM alertas_inventario WHERE estado = 'PENDIENTE';
```

### Resumen de cajeras:
```sql
SELECT * FROM vista_resumen_cajeras;
```

## 🔐 Configuración de Seguridad (Producción)

```sql
-- Crear usuario con permisos limitados
CREATE USER minimarket_readonly WITH PASSWORD 'readonly_pass';
GRANT CONNECT ON DATABASE minimarket_db TO minimarket_readonly;
GRANT USAGE ON SCHEMA public TO minimarket_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO minimarket_readonly;

-- Configurar SSL (recomendado)
-- Editar postgresql.conf:
-- ssl = on
-- ssl_cert_file = 'server.crt'
-- ssl_key_file = 'server.key'
```

## 📈 Monitoreo y Mantenimiento

### Backup automático:
```bash
# Crear script de backup
#!/bin/bash
pg_dump -U postgres minimarket_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Programar en crontab
0 2 * * * /path/to/backup_script.sh
```

### Limpieza de logs:
```sql
-- Limpiar logs antiguos (más de 30 días)
DELETE FROM logs_sistema WHERE fecha_log < CURRENT_DATE - INTERVAL '30 days';
```

---

## ✅ Lista de Verificación Final

- [ ] PostgreSQL instalado y ejecutándose
- [ ] Base de datos `minimarket_db` creada
- [ ] Script SQL ejecutado correctamente
- [ ] Driver PostgreSQL en classpath
- [ ] Configuración de conexión actualizada
- [ ] Sistema Java ejecutándose sin errores
- [ ] Datos de prueba cargados correctamente

¡Tu sistema está listo para funcionar! 🎉
