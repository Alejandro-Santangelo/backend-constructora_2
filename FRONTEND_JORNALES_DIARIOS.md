# 🚀 Guía Rápida - Jornales Diarios (Frontend)

## 📝 Ejemplos Listos para Usar

### 1️⃣ Registrar que un profesional trabajó en una obra

**Caso: Profesional trabajó día completo**
```javascript
// POST /api/jornales-diarios
const registrarDiaCompleto = async (profesionalId, obraId, fecha) => {
  const response = await fetch('/api/jornales-diarios', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      profesionalId: profesionalId,        // ej: 1
      obraId: obraId,                      // ej: 5
      fecha: fecha,                        // ej: "2026-03-14"
      horasTrabajadasDecimal: 1.0          // 1.0 = día completo
      // tarifaDiaria NO se envía, se toma automáticamente del profesional
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    alert('Error: ' + error.message);
    return;
  }
  
  const jornal = await response.json();
  console.log('Jornal creado:', jornal);
  // jornal.montoCobrado ya viene calculado automáticamente
  return jornal;
};

// Uso:
// registrarDiaCompleto(1, 5, "2026-03-14")
```

**Caso: Profesional trabajó medio día**
```javascript
const data = {
  profesionalId: 1,
  obraId: 5,
  fecha: "2026-03-14",
  horasTrabajadasDecimal: 0.5,    // 0.5 = medio día → cobra 50%
  observaciones: "Trabajó solo por la mañana"
};
```

**Caso: Profesional trabajó en 2 obras el mismo día**
```javascript
// Obra 1 - Mañana
await fetch('/api/jornales-diarios', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    profesionalId: 1,
    obraId: 5,
    fecha: "2026-03-14",
    horasTrabajadasDecimal: 0.5,
    observaciones: "Mañana"
  })
});

// Obra 2 - Tarde
await fetch('/api/jornales-diarios', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    profesionalId: 1,
    obraId: 8,
    fecha: "2026-03-14",
    horasTrabajadasDecimal: 0.5,
    observaciones: "Tarde"
  })
});
```

---

### 2️⃣ Ver todos los días que trabajó un profesional en una obra

```javascript
// GET /api/jornales-diarios/profesional/{profesionalId}/obra/{obraId}
const verJornalesProfesionalEnObra = async (profesionalId, obraId) => {
  const response = await fetch(
    `/api/jornales-diarios/profesional/${profesionalId}/obra/${obraId}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  const jornales = await response.json();
  
  // jornales es un array como:
  // [
  //   { id: 1, fecha: "2026-03-14", horasTrabajadasDecimal: 1.0, montoCobrado: 100000 },
  //   { id: 2, fecha: "2026-03-13", horasTrabajadasDecimal: 0.5, montoCobrado: 50000 },
  //   ...
  // ]
  
  return jornales;
};

// Uso:
// const jornales = await verJornalesProfesionalEnObra(1, 5);
// console.table(jornales);
```

---

### 3️⃣ Ver cuánto cobró un profesional en una obra (TOTAL)

```javascript
// GET /api/jornales-diarios/resumen/profesional/{profesionalId}/obra/{obraId}
const verTotalCobrado = async (profesionalId, obraId) => {
  const response = await fetch(
    `/api/jornales-diarios/resumen/profesional/${profesionalId}/obra/${obraId}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  const resumen = await response.json();
  
  // resumen contiene:
  // {
  //   profesionalNombre: "Juan Pérez",
  //   obraNombre: "Casa Rodríguez",
  //   cantidadJornales: 10,
  //   totalHorasDecimal: 8.5,      // 8.5 días trabajados
  //   totalCobrado: 850000.00,     // Total $$$
  //   promedioHorasPorJornal: 0.85,
  //   promedioMontoPorJornal: 85000
  // }
  
  console.log(`${resumen.profesionalNombre} cobró $${resumen.totalCobrado} en ${resumen.obraNombre}`);
  console.log(`Trabajó ${resumen.totalHorasDecimal} días en ${resumen.cantidadJornales} jornales`);
  
  return resumen;
};
```

---

### 4️⃣ Ver todos los profesionales que trabajaron en una obra

```javascript
// GET /api/jornales-diarios/resumen/obra/{obraId}/profesionales
const verProfesionalesDeObra = async (obraId) => {
  const response = await fetch(
    `/api/jornales-diarios/resumen/obra/${obraId}/profesionales`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  const profesionales = await response.json();
  
  // profesionales es un array:
  // [
  //   {
  //     profesionalNombre: "Juan Pérez",
  //     cantidadJornales: 10,
  //     totalCobrado: 850000
  //   },
  //   {
  //     profesionalNombre: "María García",
  //     cantidadJornales: 5,
  //     totalCobrado: 400000
  //   }
  // ]
  
  // Calcular costo total de la obra:
  const costoTotal = profesionales.reduce((sum, p) => sum + p.totalCobrado, 0);
  console.log(`Costo total de mano de obra: $${costoTotal}`);
  
  return profesionales;
};
```

---

### 5️⃣ Ver jornales de un profesional en un período

```javascript
// GET /api/jornales-diarios/profesional/{id}/fechas?fechaDesde=2026-03-01&fechaHasta=2026-03-31
const verJornalesPorMes = async (profesionalId, mes, anio) => {
  const fechaDesde = `${anio}-${mes.toString().padStart(2, '0')}-01`;
  const ultimoDia = new Date(anio, mes, 0).getDate();
  const fechaHasta = `${anio}-${mes.toString().padStart(2, '0')}-${ultimoDia}`;
  
  const response = await fetch(
    `/api/jornales-diarios/profesional/${profesionalId}/fechas?fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  const jornales = await response.json();
  
  return jornales;
};

// Uso:
// const jornalesMarzo = await verJornalesPorMes(1, 3, 2026);
```

---

### 6️⃣ Editar un jornal existente

```javascript
// PUT /api/jornales-diarios/{id}
const editarJornal = async (jornalId, nuevosDatos) => {
  const response = await fetch(`/api/jornales-diarios/${jornalId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(nuevosDatos)
  });
  
  if (!response.ok) {
    const error = await response.json();
    alert('Error: ' + error.message);
    return;
  }
  
  const jornalActualizado = await response.json();
  return jornalActualizado;
};

// Ejemplo: cambiar medio día a día completo
// editarJornal(123, {
//   profesionalId: 1,
//   obraId: 5,
//   fecha: "2026-03-14",
//   horasTrabajadasDecimal: 1.0  // cambió de 0.5 a 1.0
// });
```

---

### 7️⃣ Eliminar un jornal

```javascript
// DELETE /api/jornales-diarios/{id}
const eliminarJornal = async (jornalId) => {
  const confirmar = confirm('¿Eliminar este jornal?');
  if (!confirmar) return;
  
  const response = await fetch(`/api/jornales-diarios/${jornalId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  if (response.ok) {
    alert('Jornal eliminado correctamente');
    return true;
  } else {
    alert('Error al eliminar jornal');
    return false;
  }
};
```

---

## 🎨 Componente React Completo (Ejemplo)

```jsx
import React, { useState, useEffect } from 'react';

function RegistroJornal({ profesionalId, obraId }) {
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0]);
  const [horas, setHoras] = useState('1.0');
  const [observaciones, setObservaciones] = useState('');
  const [loading, setLoading] = useState(false);
  const [jornales, setJornales] = useState([]);
  const [resumen, setResumen] = useState(null);
  
  // Cargar jornales existentes
  useEffect(() => {
    cargarJornales();
    cargarResumen();
  }, [profesionalId, obraId]);
  
  const cargarJornales = async () => {
    try {
      const response = await fetch(
        `/api/jornales-diarios/profesional/${profesionalId}/obra/${obraId}`
      );
      const data = await response.json();
      setJornales(data);
    } catch (error) {
      console.error('Error al cargar jornales:', error);
    }
  };
  
  const cargarResumen = async () => {
    try {
      const response = await fetch(
        `/api/jornales-diarios/resumen/profesional/${profesionalId}/obra/${obraId}`
      );
      const data = await response.json();
      setResumen(data);
    } catch (error) {
      console.error('Error al cargar resumen:', error);
    }
  };
  
  const registrarJornal = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('/api/jornales-diarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          profesionalId,
          obraId,
          fecha,
          horasTrabajadasDecimal: parseFloat(horas),
          observaciones: observaciones || null
        })
      });
      
      if (!response.ok) {
        const error = await response.json();
        alert('Error: ' + error.message);
        return;
      }
      
      alert('Jornal registrado exitosamente');
      setObservaciones('');
      await cargarJornales();
      await cargarResumen();
      
    } catch (error) {
      alert('Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  };
  
  const eliminar = async (id) => {
    if (!confirm('¿Eliminar este jornal?')) return;
    
    try {
      await fetch(`/api/jornales-diarios/${id}`, { method: 'DELETE' });
      alert('Eliminado correctamente');
      await cargarJornales();
      await cargarResumen();
    } catch (error) {
      alert('Error al eliminar');
    }
  };
  
  return (
    <div className="registro-jornal">
      <h2>Registrar Jornal</h2>
      
      {/* Formulario */}
      <form onSubmit={registrarJornal}>
        <div>
          <label>Fecha:</label>
          <input 
            type="date" 
            value={fecha} 
            onChange={e => setFecha(e.target.value)}
            required
          />
        </div>
        
        <div>
          <label>Horas trabajadas:</label>
          <select value={horas} onChange={e => setHoras(e.target.value)}>
            <option value="0.25">Cuarto de día (0.25)</option>
            <option value="0.5">Medio día (0.5)</option>
            <option value="0.75">Tres cuartos (0.75)</option>
            <option value="1.0">Día completo (1.0)</option>
            <option value="1.25">Día y cuarto (1.25)</option>
            <option value="1.5">Día y medio (1.5)</option>
          </select>
        </div>
        
        <div>
          <label>Observaciones:</label>
          <textarea 
            value={observaciones}
            onChange={e => setObservaciones(e.target.value)}
            placeholder="Opcional"
          />
        </div>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Registrar Jornal'}
        </button>
      </form>
      
      {/* Resumen */}
      {resumen && (
        <div className="resumen">
          <h3>Resumen</h3>
          <p>Jornales trabajados: {resumen.cantidadJornales}</p>
          <p>Total días: {resumen.totalHorasDecimal}</p>
          <p>Total cobrado: ${resumen.totalCobrado.toLocaleString()}</p>
        </div>
      )}
      
      {/* Lista de jornales */}
      <div className="lista-jornales">
        <h3>Jornales registrados</h3>
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Horas</th>
              <th>Monto</th>
              <th>Observaciones</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {jornales.map(jornal => (
              <tr key={jornal.id}>
                <td>{jornal.fecha}</td>
                <td>{jornal.horasTrabajadasDecimal}</td>
                <td>${jornal.montoCobrado.toLocaleString()}</td>
                <td>{jornal.observaciones || '-'}</td>
                <td>
                  <button onClick={() => eliminar(jornal.id)}>
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default RegistroJornal;
```

---

## ⚠️ Manejo de Errores Comunes

### Error: "Ya existe un jornal para esta fecha"
```javascript
// Significa que ya registraste este profesional en esta obra en esta fecha
// Solución: editar el jornal existente en lugar de crear uno nuevo
```

### Error: "Profesional no tiene honorario diario configurado"
```javascript
// Solución 1: Configurar honorarioDia en el maestro del profesional
// Solución 2: Enviar tarifaDiaria en el request:
{
  profesionalId: 1,
  obraId: 5,
  fecha: "2026-03-14",
  horasTrabajadasDecimal: 1.0,
  tarifaDiaria: 100000.00  // ← tarifa personalizada
}
```

---

## 📊 Valores de horasTrabajadasDecimal

| Valor | Significado | % del día | Monto (si tarifa = $100,000) |
|-------|-------------|-----------|------------------------------|
| 0.25  | Cuarto día  | 25%       | $25,000                      |
| 0.5   | Medio día   | 50%       | $50,000                      |
| 0.75  | 3/4 día     | 75%       | $75,000                      |
| 1.0   | Día completo| 100%      | $100,000                     |
| 1.25  | Día y cuarto| 125%      | $125,000                     |
| 1.5   | Día y medio | 150%      | $150,000                     |

---

## ✅ Base de datos lista

La tabla `profesional_jornales_diarios` ya está creada y funcionando. Solo necesitas implementar estos endpoints en tu frontend.

Para ver la documentación completa de la API, consulta: [API_JORNALES_DIARIOS.md](API_JORNALES_DIARIOS.md)
