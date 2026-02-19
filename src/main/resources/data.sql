-- 1. Users
-- 1번: 관리자(Manager)
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('manager@test.com', '김점장', '$2a$12$6FOiv9dZY05vhTR2a9x4zO6IMFsFhWLG085AxYZSExuYHGMsAEHJe', '01000000001', NOW());

-- 2번: 직원(Staff)
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('staff@test.com', '이알바', '$2a$12$6FOiv9dZY05vhTR2a9x4zO6IMFsFhWLG085AxYZSExuYHGMsAEHJe', '01000000002', NOW());

-- 3번: 알바(Part_Time)
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('sub@test.com', '박대타', '$2a$12$6FOiv9dZY05vhTR2a9x4zO6IMFsFhWLG085AxYZSExuYHGMsAEHJe', '01000000003', NOW());

-- 4번: 알바(Part_Time)
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('sub2@test.com', '박알바', '$2a$12$6FOiv9dZY05vhTR2a9x4zO6IMFsFhWLG085AxYZSExuYHGMsAEHJe', '01000000004', NOW());

-- 2. Stores
-- 관리자(1번 유저)가 운영하는 매장
INSERT INTO stores (name, location, open_time, close_time, n_shifts, brn, user_id, alias, created_at, updated_at,template_type)
VALUES ('시프트메이트 강남점', '서울시 강남구', '09:00:00', '22:00:00', 3, '123-45-67890', 1, '본점', NOW(), NOW(),null);

-- 관리자(1번 유저)가 운영하는 두 번째 매장
INSERT INTO stores (name, location, open_time, close_time, n_shifts, brn, user_id, alias, created_at, updated_at,template_type)
VALUES ('시프트메이트 홍대점', '서울시 마포구', '10:00:00', '23:00:00', 2, '987-65-43210', 1, '홍대', NOW(), NOW(), null);

-- 3. StoreMembers
-- 1번 유저 -> 매장 매니저(점장)
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 1, 'MANAGER', 'MANAGER', 'HALL', 0, 50, 'ACTIVE', 1111, NOW(), NOW());

-- 2번 유저 -> 매장 직원
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 2, 'STAFF', 'STAFF', 'KITCHEN', 12000, 20, 'ACTIVE', 2222, NOW(), NOW());

-- 3번 유저 -> 매장 직원(알바)
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 3, 'STAFF', 'PART_TIME', 'HALL', 11000, 15, 'ACTIVE', 3333, NOW(), NOW());

-- 4번 유저 -> 매장 직원(알바)
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 4, 'STAFF', 'PART_TIME', 'HALL', 11000, 15, 'ACTIVE', 4444, NOW(), NOW());

-- 2번 매장 1번 유저 -> 매장 직원(알바)
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (2, 1, 'STAFF', 'PART_TIME', 'HALL', 11000, 15, 'ACTIVE', 5555, NOW(), NOW());



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
VALUES (2, 1, 'MONDAY', 'PREFERRED', NOW(), NOW()); -- 월요일

-- 이알바(2번 멤버)가 미들조(2번 템플릿) 불가능
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (2, 2, 'MONDAY', 'UNAVAILABLE', NOW(), NOW()); -- 월요일


-- 6. ShiftAssignments (근무 배정)
-- 이알바에게 오픈조 배정
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-02', '2026-02-02 09:00:00', '2026-02-02 13:00:00', NOW(), NOW());
-- [추가] 2번 배정: 2/3(화) 오픈조 -> 이알바 (대타 요청할 근무)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-03', '2026-02-03 09:00:00', '2026-02-03 13:00:00', NOW(), NOW());
-- 이알바에게 마감조 배정
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 3, '2026-02-20', '2026-02-20 18:00:00', '2026-02-20 22:00:00', NOW(), NOW());

-- [2월 2주차]
-- 2월 9일(월): 이알바(오픈), 박대타(미들)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-09', '2026-02-09 09:00:00', '2026-02-09 13:00:00', NOW(), NOW());
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (3, 2, '2026-02-09', '2026-02-09 13:00:00', '2026-02-09 18:00:00', NOW(), NOW());

-- 2월 10일(화): 이알바(미들), 박대타(마감)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 2, '2026-02-10', '2026-02-10 13:00:00', '2026-02-10 18:00:00', NOW(), NOW());
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (3, 3, '2026-02-10', '2026-02-10 18:00:00', '2026-02-10 22:00:00', NOW(), NOW());

-- 2월 11일(수): 박대타(오픈), 이알바(마감)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (3, 1, '2026-02-11', '2026-02-11 09:00:00', '2026-02-11 13:00:00', NOW(), NOW());
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 3, '2026-02-11', '2026-02-11 18:00:00', '2026-02-11 22:00:00', NOW(), NOW());

-- 2월 12일(목): 이알바(오픈) - 박대타 휴무 가정
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-12', '2026-02-12 09:00:00', '2026-02-12 13:00:00', NOW(), NOW());

-- 2월 13일(금): 박대타(미들), 이알바(마감)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (3, 2, '2026-02-13', '2026-02-13 13:00:00', '2026-02-13 18:00:00', NOW(), NOW());
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 3, '2026-02-13', '2026-02-13 18:00:00', '2026-02-13 22:00:00', NOW(), NOW());

-- [2월 3주차]
-- 2월 16일(월): 이알바(오픈), 박대타(오픈) - 둘 다 오픈인 경우
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-02-16', '2026-02-16 09:00:00', '2026-02-16 13:00:00', NOW(), NOW());
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (3, 1, '2026-02-16', '2026-02-16 09:00:00', '2026-02-16 13:00:00', NOW(), NOW());

-- 2월 18일(수): 이알바(미들) - 대타 요청 테스트용으로 적합
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 2, '2026-02-18', '2026-02-18 13:00:00', '2026-02-18 18:00:00', NOW(), NOW());

-- [3월 - 먼 미래 일정]
-- 3월 1일(일): 이알바(오픈)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (2, 1, '2026-03-01', '2026-03-01 09:00:00', '2026-03-01 13:00:00', NOW(), NOW());

-- 3월 2일(월): 박알바(마감)
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (4, 3, '2026-03-02', '2026-03-02 18:00:00', '2026-03-02 22:00:00', NOW(), NOW());

-- 7. Attendance (출퇴근 기록)
-- 1번 배정(2/2)에 대해서는 정상 출근 기록 생성
-- INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, created_at, updated_at)
-- VALUES (1, '2026-02-02 08:55:00', '2026-02-02 13:05:00', 'NORMAL', NOW(), NOW());


-- 8. SubstituteRequests (대타 요청)
-- 상황: 이알바(2번 멤버)가 *2번 배정(2/3)* 근무에 대해 대타 요청
-- INSERT INTO substitute_requests (shiftassignment_id, requester_id, status, reason, created_at, updated_at)
-- VALUES (2, 3, 'PENDING', '화요일에 개인 사정이 생겼습니다.', NOW(), NOW());


-- 9. SubstituteApplications (대타 지원)
-- 박대타(3번 멤버)가 위의 대타 요청(request_id = 1)에 지원
-- INSERT INTO substitute_applications (request_id, applicant_id, status, created_at, updated_at)
-- VALUES (1, 3, 'WAITING', NOW(), NOW());


-- [추가 요청] 선호도 테스트용 데이터
-- 유저: PreferenceTestUser (4번 유저)
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('preftest@test.com', '선호테스터', '$2a$10$bjbizcdFOx7jBwBm00m5rOHawFLs3qtSZV0QJfc663m14LmnLZTWy', '01000000005', NOW());

-- 스토어 멤버: 2번 매장(홍대점)에 4번 유저를 직원으로 추가 (Member ID: 4)
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (2, 4, 'STAFF', 'STAFF', 'HALL', 10000, 30, 'ACTIVE', 4444, NOW(), NOW());


-- ============================================================
-- [ShiftAssignment 자동 스케줄 생성 테스트용 데이터]
-- ============================================================

-- 10. 테스트용 새로운 매장 생성 (Store ID: 3)
INSERT INTO stores (name, location, open_time, close_time, n_shifts, brn, user_id, alias, created_at, updated_at, template_type)
VALUES ('시프트메이트 테스트점', '서울시 강서구', '08:00:00', '21:00:00', 3, '111-22-33444', 1, '테스트점', NOW(), NOW(), 'HIGHSERVICE');

-- 11. 테스트 매장 직원들 추가 (User ID: 5~9, StoreMember ID: 5~9)
-- 5번 유저: 테스트 매니저
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('test.manager@test.com', '테스트매니저', '$2a$10$bjbizcdFOx7jBwBm00m5rOHawFLs3qtSZV0QJfc663m14LmnLZTWy', '01000000006', NOW());

INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (3, 5, 'MANAGER', 'MANAGER', 'HALL', 0, 40, 'ACTIVE', 5555, NOW(), NOW());

-- 6번 유저: 홀 직원 1
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('hall1@test.com', '홀직원1', '1234', '01000000007', NOW());

INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (3, 6, 'STAFF', 'STAFF', 'HALL', 13000, 25, 'ACTIVE', 6666, NOW(), NOW());

-- 7번 유저: 홀 직원 2
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('hall2@test.com', '홀직원2', '1234', '01000000008', NOW());

INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (3, 7, 'STAFF', 'PART_TIME', 'HALL', 11500, 15, 'ACTIVE', 7777, NOW(), NOW());

-- 8번 유저: 주방 직원 1
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('kitchen1@test.com', '주방직원1', '1234', '01000000009', NOW());

INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (3, 8, 'STAFF', 'STAFF', 'KITCHEN', 14000, 30, 'ACTIVE', 8888, NOW(), NOW());

-- 9번 유저: 주방 직원 2
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('kitchen2@test.com', '주방직원2', '1234', '01000000010', NOW());

INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (3, 9, 'STAFF', 'PART_TIME', 'KITCHEN', 12500, 20, 'ACTIVE', 9999, NOW(), NOW());


-- 12. 테스트 매장의 Shift Templates (Template ID: 4~9)
-- 평일 템플릿
INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (3, '평일 오픈', '08:00:00', '12:00:00', 2, 'NORMAL', 'WEEKDAY');

INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (3, '평일 점심피크', '12:00:00', '15:00:00', 3, 'PEAK', 'WEEKDAY');

INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (3, '평일 저녁', '15:00:00', '18:00:00', 2, 'NORMAL', 'WEEKDAY');

INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (3, '평일 마감', '18:00:00', '21:00:00', 2, 'NORMAL', 'WEEKDAY');

-- 휴일 템플릿
INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (3, '주말 오픈', '08:00:00', '13:00:00', 3, 'NORMAL', 'HOLIDAY');

INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (3, '주말 피크', '13:00:00', '19:00:00', 4, 'PEAK', 'HOLIDAY');

INSERT INTO shift_templates (store_id, name, start_time, end_time, required_staff, shift_type, day_type)
VALUES (3, '주말 마감', '19:00:00', '21:00:00', 2, 'NORMAL', 'HOLIDAY');


-- 13. 직원 선호도 데이터 (EmployeePreference ID: 3~20)
-- 홀직원1 (Member ID: 6)의 선호도
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (6, 4, 'MONDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (6, 5, 'MONDAY', 'NATURAL', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (6, 6, 'WEDNESDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (6, 7, 'FRIDAY', 'UNAVAILABLE', NOW(), NOW());

-- 홀직원2 (Member ID: 7)의 선호도
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (7, 5, 'TUESDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (7, 6, 'THURSDAY', 'NATURAL', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (7, 8, 'SATURDAY', 'UNAVAILABLE', NOW(), NOW());

-- 주방직원1 (Member ID: 8)의 선호도
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (8, 4, 'MONDAY', 'NATURAL', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (8, 5, 'TUESDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (8, 6, 'WEDNESDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (8, 9, 'SUNDAY', 'UNAVAILABLE', NOW(), NOW());

-- 주방직원2 (Member ID: 9)의 선호도
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (9, 4, 'THURSDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (9, 7, 'FRIDAY', 'NATURAL', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (9, 8, 'SATURDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (9, 9, 'SUNDAY', 'PREFERRED', NOW(), NOW());

-- 테스트 매니저 (Member ID: 5)의 선호도
INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (5, 4, 'MONDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (5, 5, 'TUESDAY', 'PREFERRED', NOW(), NOW());

INSERT INTO employee_preferences (member_id, shift_template_id, day_of_week, type, created_at, updated_at)
VALUES (5, 8, 'SATURDAY', 'NATURAL', NOW(), NOW());

-- ============================================================
-- [프로필 월급 테스트용] 알바 1명 + 2개 스토어 + 3개월 근무/출퇴근 데이터
-- ============================================================

-- 14. 테스트용 알바 유저 1명 추가 (예상 user_id: 11)
INSERT INTO users (email, name, password, phone_number, created_at)
VALUES ('salary.parttime@test.com', '급여알바', '$2a$12$6FOiv9dZY05vhTR2a9x4zO6IMFsFhWLG085AxYZSExuYHGMsAEHJe', '01000000011', NOW());

-- 15. 같은 유저를 1번/2번 스토어에 모두 STAFF(PART_TIME)로 등록
INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (1, 11, 'STAFF', 'PART_TIME', 'HALL', 11000, 12, 'ACTIVE', 1112, NOW(), NOW());

INSERT INTO store_members (store_id, user_id, role, member_rank, department, hourly_wage, min_hours_per_week, status, pin_code, created_at, updated_at)
VALUES (2, 11, 'STAFF', 'PART_TIME', 'HALL', 13000, 10, 'ACTIVE', 2112, NOW(), NOW());

-- 16. 월별 1건씩 근무 배정 (2025-11, 2025-12, 2026-01, 2026-02)
-- 공통 배정시간: 11:00 ~ 17:00
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 1 AND user_id = 11 LIMIT 1),
    1,
    '2025-11-12',
    '2025-11-12 11:00:00',
    '2025-11-12 17:00:00',
    NOW(),
    NOW()
);

INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 2 AND user_id = 11 LIMIT 1),
    1,
    '2025-12-09',
    '2025-12-09 11:00:00',
    '2025-12-09 17:00:00',
    NOW(),
    NOW()
);

INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 1 AND user_id = 11 LIMIT 1),
    1,
    '2026-01-14',
    '2026-01-14 11:00:00',
    '2026-01-14 17:00:00',
    NOW(),
    NOW()
);

INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 2 AND user_id = 11 LIMIT 1),
    1,
    '2026-02-18',
    '2026-02-18 11:00:00',
    '2026-02-18 17:00:00',
    NOW(),
    NOW()
);

-- 16-1. 각 월에 반대 스토어 근무 1건씩 추가 (시간은 소폭 변동)
-- 2025-11: 기존 강남점 -> 홍대점 추가
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 2 AND user_id = 11 LIMIT 1),
    1,
    '2025-11-25',
    '2025-11-25 11:00:00',
    '2025-11-25 17:00:00',
    NOW(),
    NOW()
);

-- 2025-12: 기존 홍대점 -> 강남점 추가
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 1 AND user_id = 11 LIMIT 1),
    1,
    '2025-12-22',
    '2025-12-22 11:00:00',
    '2025-12-22 17:00:00',
    NOW(),
    NOW()
);

-- 2026-01: 기존 강남점 -> 홍대점 추가
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 2 AND user_id = 11 LIMIT 1),
    1,
    '2026-01-23',
    '2026-01-23 11:00:00',
    '2026-01-23 17:00:00',
    NOW(),
    NOW()
);

-- 2026-02: 기존 홍대점 -> 강남점 추가
INSERT INTO shift_assignments (member_id, shift_template_id, work_date, updated_start_time, updated_end_time, created_at, updated_at)
VALUES (
    (SELECT id FROM store_members WHERE store_id = 1 AND user_id = 11 LIMIT 1),
    1,
    '2026-02-24',
    '2026-02-24 11:00:00',
    '2026-02-24 17:00:00',
    NOW(),
    NOW()
);

-- 17. 위 배정에 대한 출퇴근(완료) 기록 생성
-- 2025-11: 11:00 출근, 17:40 퇴근 (NORMAL)
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2025-11-12 11:00:00', '2025-11-12 17:40:00', 'NORMAL', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sa.work_date = '2025-11-12'
LIMIT 1;

-- 2025-12: 10:55 출근, 17:13 퇴근 (NORMAL)
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2025-12-09 10:55:00', '2025-12-09 17:13:00', 'NORMAL', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sa.work_date = '2025-12-09'
LIMIT 1;

-- 2026-01: 11:20 출근, 17:00 퇴근 (LATE)
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2026-01-14 11:20:00', '2026-01-14 17:00:00', 'LATE', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sa.work_date = '2026-01-14'
LIMIT 1;

-- 2026-02: 11:03 출근, 17:50 퇴근 (NORMAL)
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2026-02-18 11:03:00', '2026-02-18 17:50:00', 'NORMAL', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sa.work_date = '2026-02-18'
LIMIT 1;

-- 17-1. 반대 스토어로 추가한 근무에 대한 출퇴근(완료) 기록
-- 2025-11 홍대점 추가분
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2025-11-25 11:04:00', '2025-11-25 17:36:00', 'NORMAL', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sm.store_id = 2 AND sa.work_date = '2025-11-25'
LIMIT 1;

-- 2025-12 강남점 추가분
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2025-12-22 10:58:00', '2025-12-22 17:18:00', 'NORMAL', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sm.store_id = 1 AND sa.work_date = '2025-12-22'
LIMIT 1;

-- 2026-01 홍대점 추가분
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2026-01-23 11:16:00', '2026-01-23 17:07:00', 'LATE', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sm.store_id = 2 AND sa.work_date = '2026-01-23'
LIMIT 1;

-- 2026-02 강남점 추가분
INSERT INTO attendance (assignment_id, clock_in_at, clock_out_at, status, work_status, created_at, updated_at)
SELECT sa.id, '2026-02-24 11:02:00', '2026-02-24 17:44:00', 'NORMAL', 'OFFWORK', NOW(), NOW()
FROM shift_assignments sa
JOIN store_members sm ON sa.member_id = sm.id
WHERE sm.user_id = 11 AND sm.store_id = 1 AND sa.work_date = '2026-02-24'
LIMIT 1;

