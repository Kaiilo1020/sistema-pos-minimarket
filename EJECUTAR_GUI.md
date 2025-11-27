# 🖥️ Cómo Ejecutar la Interfaz Gráfica (Swing)

## 🎯 **3 Formas de Abrir la Interfaz Gráfica**

### **1. 🎮 Desde el Main (Recomendado)**
1. Ejecuta `Main.java` desde tu IDE
2. En el menú, selecciona la opción **`7. 🖥️ Abrir Interfaz Gráfica (Swing)`**
3. ¡La interfaz se abrirá automáticamente!

### **2. 🚀 Ejecutar Directamente MinimarketSwingApp**
1. Ve a `src/main/java/com/minimarket/gui/MinimarketSwingApp.java`
2. Haz clic derecho → "Run MinimarketSwingApp.main()"
3. Se abrirá directamente el dashboard Swing

### **3. 💻 Desde Terminal/Consola**
```bash
# Compilar
mvn clean compile

# Ejecutar la GUI directamente
mvn exec:java -Dexec.mainClass="com.minimarket.gui.MinimarketSwingApp"
```

---

## 🎨 **¿Qué Verás en la Interfaz Gráfica?**

La interfaz Swing incluye:

- **🏠 Dashboard Principal** - Vista moderna con cards y estadísticas
- **📊 Estado del Sistema** - Conexión BD, patrones implementados
- **🎯 Patrones de Diseño** - Visualización de los 7 patrones
- **⏰ Reloj en Tiempo Real** - Fecha y hora actualizándose
- **🎨 Tema Moderno** - Colores y diseño profesional
- **📱 Responsive** - Se adapta al tamaño de ventana

---

## 🔧 **Si Hay Errores en la GUI**

Si ves errores al abrir la interfaz gráfica, puede ser por:

1. **Falta de dependencias Swing** (poco probable, viene con Java)
2. **Errores de compilación** - Ejecuta `mvn clean compile` primero
3. **Problemas de Look & Feel** - La app usa Nimbus por defecto

---

## 💡 **Consejo**

**Para desarrollo:** Usa la opción 1 (desde Main) porque puedes alternar entre consola y GUI fácilmente.

**Para demostración:** Usa la opción 2 (directo) para abrir solo la interfaz gráfica.

---

## 🎯 **Próximos Pasos**

Una vez que tengas la GUI abierta, podrás:
- Ver el dashboard completo
- Interactuar con los patrones de diseño
- Monitorear el estado del sistema
- Navegar por las diferentes secciones

¡La interfaz está lista para usar! 🚀
