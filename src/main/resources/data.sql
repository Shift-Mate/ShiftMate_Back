-- 1. Users
-- 1번: 관리자(Manager)
INSERT INTO users (email, name, password, created_at)
VALUES ('manager@test.com', '김점장', '1234', NOW());

-- 2번: 직원(Staff)
INSERT INTO users (email, name, password, created_at)
VALUES ('staff@test.com', '이알바', '1234', NOW());


-- 2. Stores
-- 관리자(1번 유저)가 운영하는 매장
INSERT INTO stores (name, location, open_time, close_time, n_shifts, brn, user_id, alias, created_at, updated_at)
VALUES ('시프트메이트 강남점', '서울시 강남구', '09:00:00', '22:00:00', 3, '123-45-67890', 1, '본점', NOW(), NOW());


-- 3. StoreMembers
-- 1번 유저 -> 매장 매니저(점장)
INSERT INTO store_members (store_id, user_id, role, rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 1, 'MANAGER', 'MANAGER', 'HALL', 0, 50, 'ACTIVE', 1111, NOW(), NOW());

-- 2번 유저 -> 매장 직원(알바)
INSERT INTO store_members (store_id, user_id, role, rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 2, 'STAFF', 'STAFF', 'KITCHEN', 12000, 20, 'ACTIVE', 2222, NOW(), NOW());


-- 4. ShiftTemplates (근무표 템플릿: 오픈, 미들, 마감)
INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (1, '오픈', '09:00:00', '13:00:00', 2, 'NORMAL', 'WEEKDAY');

INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (1, '미들', '13:00:00', '18:00:00', 3, 'PEAK', 'WEEKDAY');

INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (1, '마감', '18:00:00', '22:00:00', 2, 'NORMAL', 'WEEKDAY');


-- 5. EmployeePreferences (직원 선호도)
-- 이알바(2번 멤버)가 오픈조(1번 템플릿) 선호
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_seek, type, created_at, updated_at)
VALUES (2, 1, 1, 'PREFERRED', NOW(), NOW()); -- 월요일

-- 이알바(2번 멤버)가 미들조(2번 템플릿) 불가능
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_seek, type, created_at, updated_at)
VALUES (2, 2, 1, 'UNAVAILABLE', NOW(), NOW()); -- 월요일


-- 6. ShiftAssignments (근무 배정)
-- 이알바에게 오픈조 배정
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-02', '2026-02-02 09:00:00', '2026-02-02 13:00:00', NOW(), NOW());
