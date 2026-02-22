# 📋 Instrucciones: Auditoría Automática Semanal

## 📁 Archivos Creados

1. ✅ `auditoria_datos_huerfanos_clean.sql` - Script SQL de auditoría (sin caracteres especiales)
2. ✅ `ejecutar_auditoria_semanal.ps1` - Script PowerShell automático
3. ✅ Este archivo de instrucciones

---

## ⚙️ CONFIGURACIÓN INICIAL (Solo una vez)

### Paso 1: Configurar credenciales de base de datos

Abre PowerShell como Administrador y ejecuta:

```powershell
# Establecer la contraseña de la base de datos como variable de entorno
[System.Environment]::SetEnvironmentVariable('DB_PASSWORD', 'tu_contraseña_postgres', 'User')
```

**Reemplaza `tu_contraseña_postgres`** con tu contraseña real de PostgreSQL.

### Paso 2: Verificar configuración del script

Abre `ejecutar_auditoria_semanal.ps1` y verifica estos valores:

```powershell
$dbHost = "localhost"      # Cambiar si tu BD está en otro servidor
$dbPort = "5432"           # Cambiar si usas otro puerto
$dbName = "construccion_app_v3"  # Verificar nombre de tu BD
$dbUser = "postgres"       # Cambiar si usas otro usuario
```

### Paso 3: Probar ejecución manual

En PowerShell, navega a la carpeta del proyecto y ejecuta:

```powershell
cd "C:\Users\Usuario\Desktop\AppConstructoras\backend-constructora_2"
.\ejecutar_auditoria_semanal.ps1
```

Si todo está bien, se abrirá un reporte HTML en tu navegador.

---

## 🤖 PROGRAMAR EJECUCIÓN AUTOMÁTICA SEMANAL

### Opción A: Usando el Programador de Tareas de Windows (Recomendado)

1. **Abrir el Programador de Tareas**
   - Presiona `Win + R`
   - Escribe `taskschd.msc`
   - Presiona Enter

2. **Crear Nueva Tarea**
   - Click derecho en "Biblioteca del Programador de tareas"
   - Selecciona "Crear tarea..."

3. **Pestaña General**
   - Nombre: `Auditoría Semanal Backend Constructora`
   - Descripción: `Ejecuta auditoría de datos huérfanos cada semana`
   - ☑️ Marcar "Ejecutar con los privilegios más altos"
   - ☑️ Configurar para: Windows 10

4. **Pestaña Desencadenadores**
   - Click en "Nuevo..."
   - Iniciar la tarea: `Según una programación`
   - Configuración: `Semanal`
   - Repetir cada: `1 semana`
   - Día: `Lunes` (o el día que prefieras)
   - Hora: `09:00:00` (o la hora que prefieras)
   - ☑️ Marcar "Habilitado"

5. **Pestaña Acciones**
   - Click en "Nueva..."
   - Acción: `Iniciar un programa`
   - Programa/script: `powershell.exe`
   - Agregar argumentos:
     ```
     -ExecutionPolicy Bypass -File "C:\Users\Usuario\Desktop\AppConstructoras\backend-constructora_2\ejecutar_auditoria_semanal.ps1"
     ```
   - Iniciar en:
     ```
     C:\Users\Usuario\Desktop\AppConstructoras\backend-constructora_2
     ```

6. **Pestaña Condiciones**
   - ☐ Desmarcar "Iniciar la tarea solo si el equipo está conectado a la corriente alterna"
   - ☑️ Marcar "Activar la tarea si se omitió una ejecución programada"

7. **Guardar**
   - Click en "Aceptar"
   - Puede que te pida ingresar tu contraseña de Windows

### Opción B: Comando rápido PowerShell

Ejecuta este comando en PowerShell como Administrador:

```powershell
$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-ExecutionPolicy Bypass -File 'C:\Users\Usuario\Desktop\AppConstructoras\backend-constructora_2\ejecutar_auditoria_semanal.ps1'" -WorkingDirectory "C:\Users\Usuario\Desktop\AppConstructoras\backend-constructora_2"
$trigger = New-ScheduledTaskTrigger -Weekly -DaysOfWeek Monday -At 9am
$principal = New-ScheduledTaskPrincipal -UserId "$env:USERDOMAIN\$env:USERNAME" -LogonType ServiceAccount -RunLevel Highest
Register-ScheduledTask -Action $action -Trigger $trigger -Principal $principal -TaskName "Auditoría Semanal Backend Constructora" -Description "Ejecuta auditoría de datos huérfanos cada semana"
```

---

## 📊 UBICACIÓN DE LOS REPORTES

Los informes se guardan automáticamente en:

```
C:\Users\Usuario\Desktop\AppConstructoras\backend-constructora_2\reportes-auditoria\
```

- Cada reporte tiene formato: `auditoria_YYYY-MM-DD_HH-mm-ss.html`
- Se mantienen los últimos 12 reportes (3 meses)
- Los reportes antiguos se eliminan automáticamente

---

## 📧 INTERPRETACIÓN DEL REPORTE

### ✅ Todo OK
Si ves solo marcas verdes (✅ OK), significa:
- Sistema limpio
- No hay datos huérfanos
- Triggers funcionando correctamente

### ⚠️ Advertencia
Si ves marcas rojas (⚠️ REVISAR), significa:
- Se detectaron datos huérfanos
- **Acción requerida**: Investigar qué proceso del backend/frontend los está creando
- Revisar el detalle en la tabla del reporte

---

## 🔧 SOLUCIÓN DE PROBLEMAS

### Error: "No se puede ejecutar scripts"
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Error: "psql no se reconoce"
Agregar PostgreSQL al PATH:
1. Buscar la ruta de instalación (ej: `C:\Program Files\PostgreSQL\15\bin`)
2. Agregar al PATH del sistema

### Error de conexión a la BD
Verificar:
- PostgreSQL está corriendo
- Credenciales correctas
- Variable de entorno DB_PASSWORD configurada

### Probar conexión manual:
```powershell
psql -h localhost -U postgres -d construccion_app_v3 -c "SELECT 1;"
```

---

## 📞 SOPORTE

Si el reporte muestra datos huérfanos (⚠️):

1. **Revisar el log**: `reportes-auditoria\auditoria.log`
2. **Verificar triggers activos**: Debe mostrar "9 triggers activos"
3. **Investigar origen**: ¿Qué proceso del sistema creó los datos?
4. **Contactar**: Equipo de desarrollo si el problema persiste

---

## ✅ VERIFICACIÓN FINAL

Para confirmar que todo funciona:

1. ✅ Variable de entorno `DB_PASSWORD` configurada
2. ✅ Script PowerShell ejecutado manualmente con éxito
3. ✅ Tarea programada creada en Windows
4. ✅ Reporte HTML generado y visible
5. ✅ Primera ejecución muestra: "✅ Sistema limpio"

---

**Fecha de instalación:** 22/02/2026
**Responsable:** Administrador del Sistema
**Próxima auditoría programada:** Ver Programador de Tareas de Windows
