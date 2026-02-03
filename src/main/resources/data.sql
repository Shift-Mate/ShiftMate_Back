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

-- 관리자(1번 유저)가 운영하는 두 번째 매장
INSERT INTO stores (name, location, open_time, close_time, n_shifts, brn, user_id, alias, created_at, updated_at)
VALUES ('시프트메이트 홍대점', '서울시 마포구', '10:00:00', '23:00:00', 2, '987-65-43210', 1, '홍대', NOW(), NOW());

-- 3. StoreMembers
-- 1번 유저 -> 매장 매니저(점장)
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 1, 'MANAGER', 'MANAGER', 'HALL', 0, 50, 'ACTIVE', 1111, NOW(), NOW());

-- 2번 유저 -> 매장 직원(알바)
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
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
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (2, 1, 1, 'PREFERRED', NOW(), NOW()); -- 월요일

-- 이알바(2번 멤버)가 미들조(2번 템플릿) 불가능
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (2, 2, 1, 'UNAVAILABLE', NOW(), NOW()); -- 월요일


-- 6. ShiftAssignments (근무 배정)
-- 이알바에게 오픈조 배정
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-02', '2026-02-02 09:00:00', '2026-02-02 13:00:00', NOW(), NOW());
-- [추가] 2번 배정: 2/3(화) 오픈조 -> 이알바 (대타 요청할 근무)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-03', '2026-02-03 09:00:00', '2026-02-03 13:00:00', NOW(), NOW());


-- 7. Attendance (출퇴근 기록)
-- 1번 배정(2/2)에 대해서는 정상 출근 기록 생성
-- INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, created_at, updated_at)
-- VALUES (1, '2026-02-02 08:55:00', '2026-02-02 13:05:00', 'NORMAL', NOW(), NOW());


-- [추가 데이터] 대타 시나리오를 위한 제3의 인물 (박대타)
INSERT INTO users (email, name, password, created_at)
VALUES ('sub@test.com', '박대타', '1234', NOW());

INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 3, 'STAFF', 'PART_TIME', 'HALL', 11000, 15, 'ACTIVE', 3333, NOW(), NOW());


-- 8. SubstituteRequests (대타 요청)
-- 상황: 이알바(2번 멤버)가 *2번 배정(2/3)* 근무에 대해 대타 요청
INSERT INTO substitute_requests (shiftassignment_id, requester_id, status, reason, created_at, updated_at)
VALUES (2, 2, 'PENDING', '화요일에 개인 사정이 생겼습니다.', NOW(), NOW());


-- 9. SubstituteApplications (대타 지원)
-- 박대타(3번 멤버)가 위의 대타 요청(request_id = 1)에 지원
INSERT INTO substitute_applications (request_id, applicant_id, status, created_at, updated_at)
VALUES (1, 3, 'WAITING', NOW(), NOW());