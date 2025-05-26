# AR Servicios Públicos - Android App

Aplicación de realidad aumentada para Android que integra Google Maps y OpenStreetMap para visualizar redes de servicios públicos superpuestos con información geográfica en tiempo real.

## 🚀 Características

- **Realidad Aumentada**: Visualización AR de servicios públicos cercanos
- **Mapas Integrados**: Google Maps + OpenStreetMap
- **Servicios Web**: Integración con APIs gubernamentales colombianas
- **Filtros Inteligentes**: Por tipo de servicio y distancia
- **Interfaz Responsive**: Material Design

## 📱 Requisitos

- Android 7.0+ (API 24)
- Dispositivo compatible con ARCore
- Permisos de cámara y ubicación
- Conexión a Internet

## ⚙️ Configuración

### 1. Google Maps API Key
```
1. Ir a Google Cloud Console
2. Habilitar Maps SDK for Android
3. Crear API Key
4. Reemplazar en AndroidManifest.xml:
   YOUR_GOOGLE_MAPS_API_KEY
```

### 2. Compilación
```bash
./gradlew assembleDebug
```

### 3. Instalación
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🏛️ Servicios Soportados

- **Básicos**: Hospitales, colegios, policía, bomberos
- **Infraestructura**: Subestaciones eléctricas, plantas de agua
- **Telecomunicaciones**: Torres celulares, antenas
- **Gas Natural**: Estaciones de distribución

## 🌐 APIs Integradas

- OpenStreetMap Overpass API
- APIs gubernamentales colombianas (configurable)
- Servicios web personalizados

## 📝 Licencia

MIT License - Ver archivo LICENSE para más detalles.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crear feature branch
3. Commit los cambios
4. Push al branch
5. Crear Pull Request

## 📞 Soporte

Para soporte técnico, crear un issue en el repositorio.
