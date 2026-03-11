# GUÍA RÁPIDA: Usar Claude API Directamente
## Ahorra 60-75% vs solicitudes premium adicionales de Copilot

## ¿Por qué usar esto?
- **Copilot adicional**: $0.04 por solicitud = $20 por 500 solicitudes
- **API Anthropic**: ~$0.01 por solicitud = $5-7 por 500 solicitudes
- **Ahorro**: $13-15 por cada 500 solicitudes

---

## CONFIGURACIÓN (Solo 1 vez - 5 minutos)

### Paso 1: Crear cuenta Anthropic
1. Ve a: https://console.anthropic.com/
2. Sign up con tu email
3. Verifica email

### Paso 2: Obtener API Key
1. Entra a: https://console.anthropic.com/settings/keys
2. Click **"Create Key"**
3. Copia la key (empieza con `sk-ant-...`)
4. **⚠️ Guárdala segura** - solo se muestra una vez

### Paso 3: Agregar créditos
1. Ve a **Billing**: https://console.anthropic.com/settings/billing
2. Agrega **$10 USD** para empezar (te durará ~500-1000 solicitudes)
3. Acepta tarjetas de débito/crédito

### Paso 4: Configurar API Key en Windows

**Opción A - Permanente (RECOMENDADO):**
```powershell
# Ejecuta esto UNA VEZ en PowerShell:
[System.Environment]::SetEnvironmentVariable('ANTHROPIC_API_KEY', 'sk-ant-TU-KEY-AQUI', 'User')

# Cierra y abre PowerShell de nuevo
```

**Opción B - Solo sesión actual:**
```powershell
$env:ANTHROPIC_API_KEY = "sk-ant-TU-KEY-AQUI"
```

---

## USO DIARIO

### Ejemplo 1: Pregunta simple
```powershell
.\claude-chat.ps1 "¿Cómo optimizar una query SQL con múltiples JOINs?"
```

### Ejemplo 2: Revisar código
```powershell
.\claude-chat.ps1 "Explica qué hace este código: SELECT u.*, p.* FROM users u LEFT JOIN posts p ON u.id = p.user_id WHERE u.active = true"
```

### Ejemplo 3: Debug
```powershell
.\claude-chat.ps1 "Tengo un error 'NullPointerException' en Java al acceder a un Optional. ¿Cómo lo arreglo?"
```

### Ejemplo 4: Generar código
```powershell
.\claude-chat.ps1 "Dame un ejemplo de stored procedure en PostgreSQL que calcule totales por categoría"
```

### Ejemplo 5: Multilinea (con comillas)
```powershell
.\claude-chat.ps1 "
Analiza este endpoint Spring Boot:

@GetMapping('/api/users/{id}')
public ResponseEntity<User> getUser(@PathVariable Long id) {
    return userRepository.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

¿Hay algún problema de seguridad?
"
```

---

## COSTO REAL (Ejemplos prácticos)

| Tipo de consulta | Tokens aprox | Costo Claude | Costo Copilot | Ahorro |
|------------------|--------------|--------------|---------------|--------|
| Pregunta corta | 1,000 | $0.003 | $0.04 | $0.037 |
| Code review | 3,000 | $0.009 | $0.04 | $0.031 |
| Refactor completo | 8,000 | $0.024 | $0.04 | $0.016 |
| Debug + solución | 5,000 | $0.015 | $0.04 | $0.025 |

**Promedio:** Ahorro de **$0.027 por solicitud** (67.5%)

---

## MONITOREAR TU GASTO

### Ver balance actual:
https://console.anthropic.com/settings/billing

### Configurar alertas:
1. Ve a Billing
2. Set spending limit (ej: $20/mes)
3. Recibirás email al llegar al 80%

---

## WORKFLOW RECOMENDADO

### Días 1-28 del mes (antes del límite):
- ✅ Usa Copilot Chat normalmente
- 📊 Monitorea uso en: https://github.com/settings/copilot/premium-requests

### Cuando llegues a 1,450/1,500 (95%):
- ⚠️ Cambia a `claude-chat.ps1` para consultas complejas
- ✅ Sigue usando Copilot inline suggestions (ilimitado)

### Después del reset (día 1 del mes):
- ♻️ Vuelve a Copilot Chat
- 🎉 Límite resetea a 0/1,500

---

## TIPS PARA AHORRAR MÁS

1. **Usa consultas específicas** - menos tokens = menos costo
2. **Evita regenerar múltiples veces** - cada call consume
3. **Combina preguntas relacionadas** en una sola consulta
4. **Usa inline de Copilot** para autocompletado (gratis ilimitado)
5. **Guarda respuestas útiles** para reutilizar

---

## COMPARACIÓN DE OPCIONES

| Opción | Costo mensual típico | Ventajas | Desventajas |
|--------|---------------------|----------|-------------|
| **Solo Copilot Pro+** ($39) | $39-60 | Integrado en VSCode | Caro después del límite |
| **Copilot + API Anthropic** | $39 + $5-10 | Mejor precio post-límite | Script separado |
| **Solo API Anthropic** | $15-30 | Más barato total | Sin inline suggestions |

**Recomendación:** Copilot Pro+ ($39) + API Anthropic ($10) = **Mejor de ambos mundos**

---

## SOLUCIÓN DE PROBLEMAS

### Error: "ANTHROPIC_API_KEY no configurada"
```powershell
# Verifica que esté configurada:
$env:ANTHROPIC_API_KEY

# Si está vacía, configúrala de nuevo
[System.Environment]::SetEnvironmentVariable('ANTHROPIC_API_KEY', 'sk-ant-TU-KEY', 'User')
```

### Error: "Invalid API Key"
- Verifica que copiaste la key completa
- Genera una nueva en: https://console.anthropic.com/settings/keys

### Error: "Insufficient credits"
- Añade créditos en: https://console.anthropic.com/settings/billing

---

## ALTERNATIVA: Continue.dev (VSCode Extension)

Si prefieres interfaz gráfica como Copilot Chat:

1. Instala extensión "Continue" en VSCode
2. Configura en `.continue/config.json`:
```json
{
  "models": [
    {
      "title": "Claude Sonnet 4.5",
      "provider": "anthropic",
      "model": "claude-sonnet-4-20250514",
      "apiKey": "TU_API_KEY"
    }
  ]
}
```
3. Usa `Ctrl+L` para abrir chat (igual que Copilot pero con tu API)

---

## RESUMEN EJECUTIVO

**Situación actual:**
- 1,467/1,500 solicitudes premium usadas (97.8%)
- 33 solicitudes restantes antes de pagar adicionales
- Reset: 1 de abril (21 días)

**Acción recomendada:**
1. ✅ Crea cuenta Anthropic (gratis) - 5 min
2. ✅ Agrega $10 créditos (te duran ~500-1000 solicitudes)
3. ✅ Configura API key con el comando arriba
4. ✅ Usa `.\claude-chat.ps1` cuando llegues al límite

**Ahorro estimado este mes:** $13-15 en solicitudes adicionales

**Ahorro estimado anual:** $150-180 si siempre pasas el límite

---

## SOPORTE

- Documentación API: https://docs.anthropic.com/
- Pricing: https://www.anthropic.com/pricing
- Claude models: https://docs.anthropic.com/en/docs/models-overview
