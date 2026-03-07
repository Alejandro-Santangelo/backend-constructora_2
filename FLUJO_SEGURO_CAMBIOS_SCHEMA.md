# FLUJO SEGURO PARA CAMBIOS DE SCHEMA EN RAILWAY

## 🎯 Problema
Necesitas ajustar columnas/tablas en BD de Railway, pero:
- ✅ **Railway**: Cliente activo, datos reales
- ✅ **Local**: Datos de prueba que NO deben mezclarse con producción

## ✅ Solución: Sincronizar SOLO estructura

---

## 📋 SCRIPTS DISPONIBLES

| Script | Qué incluye? | Para qué sirve? |
|--------|-------------|-----------------|
| `backup-railway-completo.ps1` | Schema + Datos | Backup de emergencia antes de cambios |
| `backup-railway-schema-only.ps1` | **Solo Schema** | Sincronizar estructura a local SIN datos |

---

## 🔄 FLUJO RECOMENDADO

### Escenario: Agregar columna a tabla `obras`

```
BD RAILWAY (Producción)          BD LOCAL (Desarrollo)
┌────────────────────┐           ┌────────────────────┐
│ obras:             │           │ obras:             │
│  - id              │           │  - id              │
│  - nombre          │           │  - nombre          │
│  - direccion       │           │  - direccion       │
│  + nueva_columna   │           │  (sin nueva col)   │
│                    │           │                    │
│ DATOS CLIENTE      │           │ DATOS DE PRUEBA    │
└────────────────────┘           └────────────────────┘
```

---

## 📝 PASO A PASO

### ✅ PASO 1: Backup de seguridad
```powershell
# Guardar estado actual de Railway (schema + datos)
.\backup-railway-completo.ps1
```
**Por qué?** Si algo sale mal, puedes restaurar todo.

---

### ✅ PASO 2: Exportar schema de Railway
```powershell
# Exportar SOLO estructura (sin datos del cliente)
.\backup-railway-schema-only.ps1
```
**Resultado:** Archivo `railway_schema_only_YYYYMMDD_HHMMSS.sql` con CREATE TABLE, índices, constraints.

---

### ✅ PASO 3: Importar schema a BD local

**Opción A: Sobrescribir BD local completa (⚠️ pierdes datos locales)**
```powershell
# Borrar y recrear BD local
psql -U postgres -c "DROP DATABASE IF EXISTS construccion_local;"
psql -U postgres -c "CREATE DATABASE construccion_local;"

# Importar schema de Railway
psql -U postgres -d construccion_local -f .\backups\railway_schema_only_YYYYMMDD_HHMMSS.sql
```

**Opción B: Solo actualizar estructura (preserva datos locales)**
```powershell
# Importar solo schema (puede dar warnings si ya existen tablas)
psql -U postgres -d construccion_local -f .\backups\railway_schema_only_YYYYMMDD_HHMMSS.sql
```

---

### ✅ PASO 4: Modificar código Java (Entidades)

Ejemplo: Agregar columna `nueva_columna` a tabla `obras`

**Archivo:** `src/main/java/com/constructora/models/Obra.java`

```java
@Entity
@Table(name = "obras")
public class Obra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String direccion;
    
    // 🆕 NUEVA COLUMNA
    @Column(name = "nueva_columna")
    private String nuevaColumna;
    
    // getters y setters
}
```

---

### ✅ PASO 5: Probar localmente

```powershell
# Levantar app en local
.\mvnw spring-boot:run
```

**Verificar:**
- ✅ Hibernate agrega la columna automáticamente (DDL auto=update)
- ✅ App funciona con la nueva columna
- ✅ Datos de prueba se mantienen sin problemas

---

### ✅ PASO 6: Desplegar a Railway

```powershell
# Desde rama cacho
git add .
git commit -m "feat: agregar columna nueva_columna a obras"

# Subir a develop primero (sin deploy)
git checkout develop
git pull origin develop
git merge cacho
git push origin develop

# AHORA SÍ: Deploy a Railway (main)
git checkout main
git pull origin main
git merge develop
git push origin main    # 🚀 Railway auto-deploya
```

**⚠️ Railway ejecutará:**
1. Reconstruye backend con nuevo código
2. Hibernate detecta `nuevaColumna` en entidad `Obra`
3. Ejecuta: `ALTER TABLE obras ADD COLUMN nueva_columna VARCHAR(255);`
4. **Datos del cliente se preservan** ✅

---

### ✅ PASO 7: Verificar producción

Revisar logs en Railway Dashboard:
```
Hibernate: alter table obras add column nueva_columna varchar(255)
```

**Confirmar:**
- ✅ App Railway arranca sin errores
- ✅ Cliente puede seguir usando el sistema
- ✅ Datos previos intactos

---

## 🔒 SEGURIDAD: ¿Por qué NO mezclar datos?

### ❌ FORMA INCORRECTA
```powershell
# 1. Backup completo de Railway (schema + datos)
.\backup-railway-completo.ps1

# 2. Importar TODO a local
psql -U postgres -d construccion_local -f railway_completo_*.sql
```

**Problema:** 
- BD local ahora tiene datos reales del cliente
- Si haces pruebas locales, puedes:
  - Borrar datos por error
  - Crear datos de prueba mezclados con reales
  - Confundir qué es producción y qué es prueba

### ✅ FORMA CORRECTA
```powershell
# Solo importar estructura
.\backup-railway-schema-only.ps1
psql -U postgres -d construccion_local -f railway_schema_only_*.sql
```

**Beneficios:**
- BD local: Estructura actualizada, datos de prueba controlados
- BD Railway: Datos reales intactos
- Sin riesgo de mezclar información

---

## 🛡️ INDEPENDENCIA DE BDs

```
Backend Spring Boot
├── application.properties (local)
│   └── spring.datasource.url=jdbc:postgresql://localhost:5432/construccion_local
│
└── Railway Variables (producción)
    └── DATABASE_URL=postgresql://railway-host:5432/railway_db
```

**Conclusión:**
- ✅ Cambios en código → afectan ambas BDs (cuando se deploya)
- ✅ Cambios en datos local → NO afectan Railway
- ✅ Cambios en datos Railway → NO afectan local
- ✅ Hibernate en Railway crea/modifica columnas sin borrar datos

---

## 📌 RESUMEN

| Acción | Script | Resultado |
|--------|--------|-----------|
| Backup emergencia | `backup-railway-completo.ps1` | Schema + datos cliente |
| Sincronizar estructura | `backup-railway-schema-only.ps1` | Solo schema (sin datos) |
| Importar a local | `psql ... -f schema_only.sql` | BD local con misma estructura |
| Modificar entidad Java | Editor de código | Preparar cambio de schema |
| Probar local | `mvnw spring-boot:run` | Verificar funcionalidad |
| Deploy Railway | `git push origin main` | Hibernate aplica cambios automáticamente |

---

## 🚨 ERRORES COMUNES

### Error 1: "pg_dump not found"
```powershell
# Instalar PostgreSQL tools
winget install PostgreSQL.PostgreSQL
# Reiniciar PowerShell
```

### Error 2: Railway no aplica cambios de schema
**Causa:** Hibernate DDL en `none` o `validate`
**Solución:** Verificar `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Error 3: Datos del cliente borrados
**Prevención:** NUNCA usar `ddl-auto=create` o `create-drop` en producción
**Recovery:** Restaurar desde backup completo

---

## 📞 CONTACTO

Si tienes dudas sobre este flujo, revisa:
- Documentación Hibernate DDL: https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization
- Railway Postgres: https://docs.railway.app/databases/postgresql
