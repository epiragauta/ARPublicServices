# 📋 Instrucciones de Instalación y Configuración

## 🔧 Configuración Inicial

### 1. Abrir en Android Studio
```
1. Abrir Android Studio
2. File > Open > Seleccionar carpeta del proyecto
3. Esperar sincronización de Gradle
```

### 2. Configurar Google Maps
```
1. Ir a https://console.cloud.google.com/
2. Crear nuevo proyecto o seleccionar existente
3. APIs & Services > Library
4. Buscar "Maps SDK for Android" y habilitarlo
5. Credentials > Create Credentials > API Key
6. Copiar la API Key generada
7. En AndroidManifest.xml reemplazar:
   YOUR_GOOGLE_MAPS_API_KEY por tu API Key
```

### 3. Verificar ARCore
```
1. Verificar dispositivo compatible en:
   https://developers.google.com/ar/devices
2. Instalar Google Play Services for AR si es necesario
```

## 📱 Compilación e Instalación

### Método 1: Android Studio
```
1. Build > Make Project
2. Run > Run 'app'
3. Seleccionar dispositivo
```

### Método 2: Línea de comandos
```bash
# Compilar
./gradlew assembleDebug

# Instalar en dispositivo conectado
adb install app/build/outputs/apk/debug/app-debug.apk
```

## ⚠️ Solución de Problemas

### Error "ARCore not supported"
- Verificar compatibilidad del dispositivo
- Instalar Google Play Services for AR

### Error "Maps API Key"
- Verificar que la API Key esté configurada
- Verificar que Maps SDK esté habilitado
- Verificar restricciones de la API Key

### Permisos no concedidos
- Revisar permisos en Configuración > Apps
- Conceder permisos de ubicación y cámara

## 🔄 URLs de APIs Reales

Para integrar con APIs reales, actualizar en WebServiceManager.kt:

```kotlin
// APIs Gubernamentales Colombianas (Ejemplos)
private const val DANE_API = "https://www.dane.gov.co/files/geoportal/api/"
private const val IGAC_API = "https://geoportal.igac.gov.co/es/api/"
private const val MINTIC_API = "https://www.mintic.gov.co/portal/apis/"
private const val IDEAM_API = "http://www.ideam.gov.co/web/api/"
```

## 📈 Personalización

### Agregar nuevos tipos de servicios:
Editar `ServiceFilterManager.kt`:

```kotlin
enum class ServiceType(val displayName: String, val category: String) {
    // Existentes...
    NUEVO_SERVICIO("Nuevo Servicio", "categoria"),
}
```

### Cambiar colores de marcadores:
Editar `MainActivity.kt` en `getMarkerColorForType()`:

```kotlin
"nuevo_tipo" -> BitmapDescriptorFactory.HUE_PURPLE
```

## 🚀 Próximos Pasos

1. Configurar APIs reales de servicios públicos
2. Personalizar tipos de servicios según necesidades
3. Ajustar filtros y distancias
4. Configurar notificaciones push
5. Implementar sistema de reportes de usuarios
