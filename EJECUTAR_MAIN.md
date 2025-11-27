# 🚀 Cómo Ejecutar el Sistema POS Minimarket

## 📋 **Clase Main Simplificada**

El proyecto ahora tiene una clase `Main` que **abre directamente la interfaz gráfica Swing** sin menús de consola.

---

## **🎯 Ejecución Directa - Interfaz Gráfica**

### **Opción 1: Desde el IDE (Recomendado)**

1. Navega a `src/main/java/com/minimarket/Main.java`
2. Haz clic derecho → "Run Main.main()"
3. **Resultado:** Se abre directamente el dashboard Swing

### **Opción 2: Con Maven**

```bash
# Compilar y ejecutar
mvn clean compile
mvn exec:java
```

**Resultado:** Se abre directamente la interfaz gráfica

### **Opción 3: JAR Ejecutable**

```bash
# Crear JAR ejecutable
mvn clean package

# Ejecutar
java -jar target/sistema-ventas-minimarket-1.0.0.jar
```

**Resultado:** Se abre directamente el dashboard Swing

---

## **🎯 Funcionalidades del Main**

### **Demostraciones Disponibles:**

1. **🔐 RBAC (Roles y Seguridad)**
   - Prueba control de acceso por roles
   - Demuestra que cajeras no pueden hacer funciones administrativas
   - Muestra auditoría de acciones

2. **📦 Inventario FIFO**
   - Demuestra venta con lotes (FIFO)
   - Demuestra venta sin lotes (flexible)
   - Nunca bloquea transacciones

3. **🧾 Facturación Completa**
   - Registra ventas con fecha/hora exacta
   - Incluye método de pago obligatorio
   - Genera tickets completos

4. **🚨 Alertas de Stock**
   - Simula ventas que dejan stock crítico
   - Muestra alertas automáticas no bloqueantes
   - Demuestra Observer Pattern

5. **🎯 Demo Completo**
   - Ejecuta todas las demostraciones en secuencia
   - Perfecto para presentaciones

---

## **⚠️ Nota Importante sobre Base de Datos**

El sistema funciona pero necesita que ejecutes el script de actualización:

```sql
-- En PostgreSQL/pgAdmin:
\i 'database/actualizacion_pos_rbac.sql'
```

**Sin este script verás errores como:**
- `ERROR: no existe la relación 'auditoria_log'`
- `ERROR: no existe la columna 'fecha_hora_exacta'`

**Con el script:** Todo funciona perfectamente ✅

---

## **🎉 Ventajas de la Nueva Clase Main**

- ✅ **Punto de entrada único** y claro
- ✅ **Modo interactivo** para explorar funcionalidades
- ✅ **Modo automático** compatible con Maven
- ✅ **Detección automática** del entorno de ejecución
- ✅ **Menús organizados** por funcionalidad
- ✅ **Mensajes claros** y emojis para mejor UX

---

## **🚀 Recomendación de Uso**

**Para desarrollo/testing:**
```bash
mvn exec:java
```

**Para demostración interactiva:**
```bash
java -jar target/sistema-ventas-minimarket-1.0.0.jar
```

**Para presentación automática:**
```bash
mvn clean compile exec:java
```

¡El sistema está listo para usar! 🎯
