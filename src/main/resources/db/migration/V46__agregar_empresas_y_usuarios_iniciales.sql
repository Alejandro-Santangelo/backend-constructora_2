-- ============================================
-- AGREGAR EMPRESAS Y USUARIOS INICIALES
-- ============================================
-- Agregar empresas faltantes (2 y 3)
-- Agregar usuarios con PINs para autenticación

-- Empresa 2: Construcciones SRL
INSERT INTO empresas (id_empresa, nombre_empresa, email, activa) 
VALUES (2, 'Construcciones SRL', 'contacto@construcciones-srl.com', true)
ON CONFLICT (id_empresa) DO NOTHING;

-- Empresa 3: TNT (Super Admin)
INSERT INTO empresas (id_empresa, nombre_empresa, email, activa) 
VALUES (3, 'TNT', 'contacto@tnt.com', true)
ON CONFLICT (id_empresa) DO NOTHING;

-- Usuario 1: Empresa Gisel (contratista/Admin) - PIN: 1111
INSERT INTO usuarios (id_usuario, nombre, email, password_hash, rol, id_empresa, activo, fecha_creacion)
VALUES (
  1,
  'Usuario Gisel',
  'gisel@contratista.com',
  '1111',
  'contratista',
  1,
  true,
  CURRENT_TIMESTAMP
)
ON CONFLICT (id_usuario) DO UPDATE SET
  password_hash = '1111',
  rol = 'contratista';

-- Usuario 2: Empresa Construcciones SRL (contratista/Admin) - PIN: 2222
INSERT INTO usuarios (id_usuario, nombre, email, password_hash, rol, id_empresa, activo, fecha_creacion)
VALUES (
  2,
  'Usuario Construcciones SRL',
  'construcciones@contratista.com',
  '2222',
  'contratista',
  2,
  true,
  CURRENT_TIMESTAMP
)
ON CONFLICT (id_usuario) DO UPDATE SET
  password_hash = '2222',
  rol = 'contratista';

-- Usuario 3: Empresa TNT (Super Administrador) - PIN: 3333
INSERT INTO usuarios (id_usuario, nombre, email, password_hash, rol, id_empresa, activo, fecha_creacion)
VALUES (
  3,
  'Super Admin TNT',
  'admin@tnt.com',
  '3333',
  'SUPER_ADMINISTRADOR',
  3,
  true,
  CURRENT_TIMESTAMP)
ON CONFLICT (id_usuario) DO UPDATE SET
  password_hash = '3333',
  rol = 'SUPER_ADMINISTRADOR';
