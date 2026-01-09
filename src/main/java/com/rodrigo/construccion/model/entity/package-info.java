/**
 * Paquete de entidades del modelo de dominio.
 * 
 * Contiene la definición global del filtro multi-tenant 'empresaFilter'
 * que se aplica automáticamente a todas las entidades que tengan la anotación @Filter.
 */
@FilterDef(name = "empresaFilter", parameters = @ParamDef(name = "empresaId", type = Long.class))
package com.rodrigo.construccion.model.entity;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
