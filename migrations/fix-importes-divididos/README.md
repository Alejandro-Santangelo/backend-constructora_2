# Fix: Importes Divididos Incorrectamente 🔧

## 📌 Problema

El frontend estaba dividiendo importes/precios por la cantidad de semanas antes de enviarlos al backend, causando que se guardaran valores incorrectos en la base de datos.

**Ejemplo:** 
- Usuario ingresa: $1.000.000 para un trabajo de 20 semanas
- Frontend dividía: $1.000.000 ÷ 20 = $50.000
- Backend guardaba: $50.000 (❌ INCORRECTO)
- Debería guardar: $1.000.000 (✅ CORRECTO)

---

## 🎯 Solución Aplicada

### 1. Base de Datos ✅ CORREGIDA
Los datos en la base de datos ya fueron corregidos automáticamente mediante SQL.

### 2. Frontend ⚠️ PENDIENTE
El código frontend necesita modificarse para dejar de dividir los importes.

---

## 📚 Archivos en esta Carpeta

### 🚀 Para Empezar (Desarrollador Frontend)
1. **[RESUMEN_EJECUTIVO.md](RESUMEN_EJECUTIVO.md)** - Leer primero
2. **[EJEMPLOS_CODIGO_FRONTEND.md](EJEMPLOS_CODIGO_FRONTEND.md)** - Ejemplos concretos antes/después

### 📖 Documentación Completa
3. **[PROMPT_FRONTEND.md](PROMPT_FRONTEND.md)** - Instrucciones detalladas para IA/desarrollador
4. **[README.md](README.md)** - Este archivo

### 🛠️ Scripts SQL (Referencia)
5. **[1_DIAGNOSTICO_completo.sql](1_DIAGNOSTICO_completo.sql)** - Detecta datos divididos
6. **[2_CORRECCION_masiva.sql](2_CORRECCION_masiva.sql)** - Corrección con transacción
7. **[3_EJECUTAR_CORRECCION.sql](3_EJECUTAR_CORRECCION.sql)** - Corrección directa (✅ ejecutado)

---

## 🎯 Guía Rápida para Frontend

### Paso 1: Entender el Problema
Lee [RESUMEN_EJECUTIVO.md](RESUMEN_EJECUTIVO.md) (5 minutos)

### Paso 2: Ver Ejemplos de Código
Abre [EJEMPLOS_CODIGO_FRONTEND.md](EJEMPLOS_CODIGO_FRONTEND.md) y revisa:
- Sección 1: Servicios HTTP
- Sección 2: Componentes de Formulario
- Sección 3: Helpers
- Sección 6: Validación en DevTools

### Paso 3: Buscar Divisiones en tu Código
```bash
# Ejecutar en el proyecto frontend:
grep -r "/ semanas" src/
grep -r "/ tiempoEstimado" src/
grep -r "precioUnitario.*/" src/
```

### Paso 4: Aplicar Correcciones
Usa los ejemplos de [EJEMPLOS_CODIGO_FRONTEND.md](EJEMPLOS_CODIGO_FRONTEND.md) como referencia.

**Regla simple:** 
- ❌ `precioUnitario: precio / semanas` → ✅ `precioUnitario: precio`
- ❌ `subtotal: total / semanas` → ✅ `subtotal: total`

### Paso 5: Validar
1. Abrir DevTools → Network
2. Crear trabajo extra de prueba (10 semanas, gasto $100.000)
3. Verificar Request Payload: debe enviar `100000`, NO `10000`

---

## 🔍 Archivos Frontend Prioritarios

Revisar en este orden:

### Alta Prioridad
1. `trabajo-extra.service.ts` - Métodos `crear()`, `actualizar()`
2. `presupuesto-no-cliente.service.ts` - Preparación de payloads
3. `trabajo-extra-form.component.ts` - Método `onSubmit()`
4. `presupuesto-form.component.ts` - Método `guardar()`

### Media Prioridad
5. `calculadora.helper.ts` - Funciones de cálculo
6. `*.interceptor.ts` - Transformaciones HTTP
7. `*-mapper.ts` - Transformadores de datos

---

## ✅ Checklist de Corrección

- [ ] Leí RESUMEN_EJECUTIVO.md
- [ ] Revisé EJEMPLOS_CODIGO_FRONTEND.md
- [ ] Busqué divisiones en servicios HTTP
- [ ] Busqué divisiones en componentes
- [ ] Busqué divisiones en helpers
- [ ] Eliminé TODAS las divisiones por semanas
- [ ] Separé valores para guardar vs valores para display
- [ ] Probé crear trabajo extra → Verificado en DevTools
- [ ] Probé crear trabajo extra → Verificado en Base de Datos
- [ ] Probé editar trabajo extra → Valores correctos
- [ ] Probé GET trabajo extra → Valores correctos

---

## 🆘 FAQ

### ¿El backend está dividiendo los valores?
**NO.** El backend fue exhaustivamente revisado. Los datos se guardan exactamente como llegan del frontend.

### ¿Necesito modificar el backend?
**NO.** El backend está correcto. Solo necesitas corregir el frontend.

### ¿Puedo mostrar "precio semanal" en la UI?
**SÍ**, pero solo para visualización. Calcula `precioSemanal = precioTotal / semanas` para mostrar, pero **NUNCA** envíes ese valor al backend.

### ¿Cómo verifico que mi fix funciona?
1. DevTools → Network → Request Payload (debe ser valor total)
2. Base de Datos → Query `SELECT precio_unitario FROM ...` (debe ser valor total)

### ¿Qué hago si el problema persiste?
Revisa:
- State management (NgRx, Akita) - Effects y reducers
- Pipes personalizados en formularios
- Directivas que modifiquen inputs
- Validadores que transformen valores

---

## 📊 Estado del Fix

| Componente | Estado | Detalles |
|------------|--------|----------|
| Base de Datos | ✅ CORREGIDA | 2 trabajos extra + 2 presupuestos corregidos |
| Backend Java | ✅ VERIFICADO | No modifica valores, transparente |
| Frontend | ⚠️ PENDIENTE | Necesita eliminar divisiones |

---

## 📞 Contacto

Si tienes dudas después de revisar toda la documentación:
1. Lee nuevamente [EJEMPLOS_CODIGO_FRONTEND.md](EJEMPLOS_CODIGO_FRONTEND.md)
2. Verifica el Request Payload en DevTools
3. Ejecuta `1_DIAGNOSTICO_completo.sql` para ver datos en BD

**Última actualización:** 13 de enero de 2026
