-- Semilla SOLO para desarrollo local (no es migración Flyway): agenda sintética 2026-09-29 a 2026-10-15.
-- Idempotente: puede ejecutarse varias veces sin duplicar usuarios, profesionales, bloques ni franjas.
-- Los profesionales semilla no pueden iniciar sesión (password_hash no es un hash válido).
SET @seed_from = DATE('2026-09-29');
SET @seed_to   = DATE('2026-10-15');

-- 1. Profesionales sintéticos: código, nombre, especialidad (code), sede (code), días (1=lunes..6=sábado), franja.
DROP TEMPORARY TABLE IF EXISTS seed_plan;
CREATE TEMPORARY TABLE seed_plan (
  code VARCHAR(20) PRIMARY KEY, first_name VARCHAR(60), last_name VARCHAR(60), document_number VARCHAR(20),
  specialty_code VARCHAR(50), location_code VARCHAR(20), weekdays VARCHAR(20), start_time TIME, end_time TIME
);
INSERT INTO seed_plan VALUES
  ('SEED-DEV-01','Laura','Semilla MG','90000001','MEDICINA_GENERAL','HIC','1,2,3,4,5,6','08:00','12:00'),
  ('SEED-DEV-02','Andrés','Semilla MG','90000002','MEDICINA_GENERAL','ICV','1,2,3,4,5','14:00','17:00'),
  ('SEED-DEV-03','Camila','Semilla Cardio','90000003','CARDIOLOGIA_ADULTO','HIC','1,3,5','08:00','11:00'),
  ('SEED-DEV-04','Julián','Semilla Pediatría','90000004','PEDIATRIA','ICV','2,4','08:00','12:00'),
  ('SEED-DEV-05','Sofía','Semilla Neuro','90000005','NEUROLOGIA','HIC','2,4','14:00','17:00'),
  ('SEED-DEV-06','Mateo','Semilla Orto','90000006','ORTOPEDIA_TRAUMATOLOGIA','ICV','1,3,6','08:00','11:00');

INSERT INTO users(first_name,last_name,document_type,document_number,email,phone,password_hash,active,email_verified)
SELECT p.first_name,p.last_name,'CC',p.document_number,CONCAT(LOWER(p.code),'@example.test'),'3000000000','SEED-NO-LOGIN',true,false
FROM seed_plan p WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email=CONCAT(LOWER(p.code),'@example.test'));

INSERT INTO user_roles(user_id,role_id)
SELECT u.id,r.id FROM seed_plan p JOIN users u ON u.email=CONCAT(LOWER(p.code),'@example.test') JOIN roles r ON r.code='PROFESSIONAL'
WHERE NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);

INSERT INTO professionals(user_id,professional_code,license_number,active)
SELECT u.id,p.code,CONCAT('LIC-',p.code),true FROM seed_plan p JOIN users u ON u.email=CONCAT(LOWER(p.code),'@example.test')
WHERE NOT EXISTS (SELECT 1 FROM professionals x WHERE x.professional_code=p.code);

INSERT INTO professional_specialties(professional_id,specialty_id,is_primary,active)
SELECT pr.id,s.id,true,true FROM seed_plan p JOIN professionals pr ON pr.professional_code=p.code JOIN specialties s ON s.code=p.specialty_code
WHERE NOT EXISTS (SELECT 1 FROM professional_specialties x WHERE x.professional_id=pr.id AND x.specialty_id=s.id);

INSERT INTO professional_locations(professional_id,location_id,active)
SELECT pr.id,l.id,true FROM seed_plan p JOIN professionals pr ON pr.professional_code=p.code JOIN locations l ON l.code=p.location_code
WHERE NOT EXISTS (SELECT 1 FROM professional_locations x WHERE x.professional_id=pr.id AND x.location_id=l.id);

-- 2. Bloques por día hábil del plan (domingos excluidos). El 29-sep solo se publica la tarde para no ofrecer horas pasadas.
INSERT INTO availability_blocks(professional_id,location_id,available_date,start_time,end_time,active)
WITH RECURSIVE days(d) AS (SELECT @seed_from UNION ALL SELECT d + INTERVAL 1 DAY FROM days WHERE d < @seed_to)
SELECT pr.id,l.id,days.d,p.start_time,p.end_time,true
FROM seed_plan p JOIN professionals pr ON pr.professional_code=p.code JOIN locations l ON l.code=p.location_code
JOIN days ON FIND_IN_SET(WEEKDAY(days.d)+1,p.weekdays) > 0
WHERE (days.d > @seed_from OR p.start_time >= '14:00')
  AND NOT EXISTS (SELECT 1 FROM availability_blocks b WHERE b.professional_id=pr.id AND b.available_date=days.d AND b.start_time=p.start_time);

-- 3. Franjas de 30 minutos (igual que SchedulingService.createSlots) para bloques semilla sin franjas.
INSERT INTO professional_slots(availability_block_id,start_at,end_at)
WITH RECURSIVE halves(n) AS (SELECT 0 UNION ALL SELECT n + 1 FROM halves WHERE n < 47)
SELECT b.id, TIMESTAMP(b.available_date,b.start_time) + INTERVAL 30*h.n MINUTE, TIMESTAMP(b.available_date,b.start_time) + INTERVAL 30*(h.n+1) MINUTE
FROM availability_blocks b JOIN professionals pr ON pr.id=b.professional_id AND pr.professional_code LIKE 'SEED-DEV-%'
JOIN halves h ON TIMESTAMP(b.available_date,b.start_time) + INTERVAL 30*(h.n+1) MINUTE <= TIMESTAMP(b.available_date,b.end_time)
WHERE NOT EXISTS (SELECT 1 FROM professional_slots s WHERE s.availability_block_id=b.id);

DROP TEMPORARY TABLE seed_plan;

SELECT pr.professional_code, COUNT(DISTINCT b.id) AS bloques, COUNT(s.id) AS franjas
FROM professionals pr JOIN availability_blocks b ON b.professional_id=pr.id LEFT JOIN professional_slots s ON s.availability_block_id=b.id
WHERE pr.professional_code LIKE 'SEED-DEV-%' GROUP BY pr.professional_code ORDER BY pr.professional_code;
