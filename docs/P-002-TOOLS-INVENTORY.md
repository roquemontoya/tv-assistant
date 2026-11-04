# P-002 — Inventario funcional de herramientas Pamela

Fecha: 2026-11-04
Rama: `feature/pamela-samsung-flowbox`

## Resultado de inventario estático

La implementación registra **40 herramientas** en `Tools.register()`. Se verificaron los 40 identificadores contra el código fuente, no solamente contra la pantalla. La presencia en el esquema significa que la herramienta está declarada; no prueba por sí sola que cada ruta funcione en Flowbox F1. La verificación de ejecución debe hacerse por herramienta y por capacidad/permisos del dispositivo.

## Herramientas registradas

### Planes y automatización

- `action_plan`
- `workflow`
- `ui_target`
- `wait`

### Aplicaciones y enlaces

- `list_apps`
- `open_app`
- `search_app`
- `open_url`

### Web y navegador

- `web_page`
- `web_search`
- `browser_read`
- `browser_action`
- `browser_navigation`

### YouTube y multimedia

- `youtube_search`
- `youtube_latest`
- `youtube_play`
- `find_media`
- `media_details`
- `list_sessions`
- `playback`

### Dispositivo

- `volume`
- `open_settings`
- `device_info`
- `clock`
- `weather`
- `calculate`

### Datos y utilidades

- `notes`
- `preferences`
- `routines`
- `timer`

### Pantalla, accesibilidad y entrada

- `screen_read`
- `screen_see`
- `screen_tap`
- `keyboard_keys`
- `screen_swipe`
- `screen_click`
- `screen_type`
- `screen_scroll`
- `navigate`
- `speak`

## Criterios de validación pendientes

Para cada herramienta se debe registrar: contrato y parámetros; validación de argumentos; efectos secundarios; permisos requeridos; autorización humana; tratamiento de errores/cancelación; evidencia de resultado; compatibilidad con Android TV/Flowbox. Las funciones de accesibilidad, captura, gestos y teclado requieren pruebas separadas y autorización explícita. Las funciones web dependen de red y contenido remoto. Las funciones de reproducción dependen de la aplicación y su sesión multimedia.

## Estado

- **Inventario estático:** completado; 40/40 identificadores encontrados en `Tools.register()`.
- **Prueba funcional individual en Flowbox:** pendiente.
- **Nombres visibles localizados:** se incorporó un primer diccionario para los 40 identificadores, conservando los nombres internos sin cambios.
- **Descripciones detalladas de herramientas:** siguen en inglés en esta iteración; deben localizarse en una fase posterior y no se consideran completadas.
- **Autorizaciones y permisos:** no se modificaron los contratos ni las políticas de ejecución en este cambio.
