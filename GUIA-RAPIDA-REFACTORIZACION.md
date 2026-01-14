# 🚀 Guía Rápida de Refactorización - Backend Constructora

**Versión:** 3.0 | **Fecha:** 14 Enero 2026

## 📋 Lista de Verificación por Orden de Ejecución

### ✅ Fase 1: Fundamentos (Semana 1-2)
- [ ] 1. Usuario (175 líneas) - 1-2 días
- [ ] 2. Empresa (171 líneas) - 1-2 días  
- [ ] 3. Proveedor (142 líneas) - 1 día
- [ ] 4. GastoGeneral (115 líneas) - 1 día
- [ ] 5. Material (108 líneas) - 1 día

### ✅ Fase 2: Catálogos (Semana 3-4)
- [ ] 6. Cliente (186 líneas) - 2 días
- [ ] 7. Honorario (205 líneas) - 2 días
- [ ] 8. Costo (284 líneas) - 2-3 días
- [ ] 9. AsignacionProfesionalObra (210 líneas) - 2 días
- [ ] 10. GastoObraProfesional (188 líneas) - 2 días

### ✅ Fase 3: Inventario (Semana 5-6)
- [ ] 11. Profesional (302 líneas) - 2-3 días
- [ ] 12. StockMaterial (220 líneas) - 2 días
- [ ] 13. MovimientoMaterial (156 líneas) - 2 días
- [ ] 14. AsistenciaObra (217 líneas) - 2 días
- [ ] 15. CajaChicaObra (233 líneas) - 2 días
- [ ] 16. Jornal (243 líneas) - 2-3 días
- [ ] 17. PedidoPago (292 líneas) - 2-3 días

### ⚠️ Fase 4: Complejas (Semana 7-9)
- [ ] 18. ObraMaterial (298 líneas) - 3 días
- [ ] 19. EtapaDiaria (607 líneas) - 4-5 días
- [ ] 20. RetiroPersonal (306 líneas) - 3 días
- [ ] 21. AsignacionSemanal (397 líneas) - 3-4 días
- [ ] 22. TrabajoExtra (1,152 líneas) - 7-8 días

### 🔴 Fase 5: Financiero (Semana 10-12)
- [ ] 23. ProfesionalObra (651 líneas) - 5-6 días
- [ ] 24. CobroObra (577 líneas) - 5 días
- [ ] 25. CobroEmpresa (629 líneas) - 5-6 días
- [ ] 26. AsignacionCobroObra (207 líneas) - 2-3 días
- [ ] 27. PagoProfesionalObra (426 líneas) - 4 días
- [ ] 28. PagoTrabajoExtraObra (353 líneas) - 3 días
- [ ] 29. PagoConsolidado (665 líneas) - 5-6 días
- [ ] 30. ObraFinanciero (208 líneas) - 2-3 días

### 🔴🔴🔴 Fase 6: CRÍTICO (Semana 13-18)
- [ ] 31. **Obra** (602 líneas) - 5-7 días ⚠️⚠️
- [ ] 32. **PresupuestoNoCliente** (4,959 líneas) - 12-15 días ⚠️⚠️⚠️

---

## 🎯 Checklist por Entidad

Para cada entidad, completar:
- [ ] Crear rama Git: `refactor/nombre-entidad`
- [ ] Documentar lógica de negocio actual
- [ ] Escribir tests de integración
- [ ] Refactorizar Service
- [ ] Refactorizar Controller
- [ ] Refactorizar Repository
- [ ] Actualizar DTOs si es necesario
- [ ] Code Review
- [ ] Merge a develop
- [ ] Monitorear performance

---

## 📊 Resumen Ejecutivo

| Métrica | Valor |
|---------|-------|
| **Total Entidades** | 32 principales + 26 complementarias = 58 |
| **Total Líneas** | 15,484 líneas de servicios |
| **Promedio** | 484 líneas/servicio |
| **Más Compleja** | PresupuestoNoCliente (4,471 líneas) |
| **Más Simple** | GastoGeneral (115 líneas) |
| **Tiempo Total** | 14-18 semanas (3.5-4.5 meses) |

---

## ⚠️ Advertencias Críticas

1. **PresupuestoNoCliente es 10x más grande que el promedio**
   - Considerar dividir en múltiples servicios
   - Planificar 2-3 semanas solo para esta entidad
   
2. **Obra conecta con 12+ entidades**
   - Cualquier cambio afecta todo el sistema
   - Hacer con feature flags o modo mantenimiento
   
3. **Sistema Financiero es interdependiente**
   - Cobros, Pagos y Asignaciones deben funcionar juntos
   - Probar exhaustivamente después de cada cambio

---

## 🛠️ Patrón de Refactorización Estándar

```java
// 1. Service debe tener:
- Inyección de dependencias clara
- Métodos con responsabilidad única
- Validaciones en métodos privados
- Transacciones bien definidas
- Logs apropiados
- Manejo de excepciones

// 2. Controller debe tener:
- Endpoints RESTful claros
- DTOs para entrada/salida
- Validaciones con @Valid
- Documentación Swagger
- Response entities apropiados

// 3. Repository debe tener:
- Queries optimizadas
- Índices en columnas filtradas
- Named queries cuando sea posible
- Evitar N+1 queries
```

---

## 📈 Progreso Tracking

**Semana 1**: ⬜⬜⬜⬜⬜  
**Semana 2**: ⬜⬜⬜⬜⬜  
**Semana 3**: ⬜⬜⬜⬜⬜  
**Semana 4**: ⬜⬜⬜⬜⬜  
**Semana 5**: ⬜⬜⬜⬜⬜  
**Semana 6**: ⬜⬜⬜⬜⬜  

Actualizar con ✅ conforme se complete cada día.

---

✨ **Para más detalles, ver `Analisis-Entidades-Refactorizacion.md`** ✨

