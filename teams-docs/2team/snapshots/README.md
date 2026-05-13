# 2팀 일일 스냅샷 (Umma)

> 매일 일일 진척 체크 ([daily_check_method.md](../../.shared/daily_check_method.md)) 수행 후 결과를 이곳에 저장합니다.

## 파일명 규칙

`YYMMDD_(am|pm).md` — 예:
- `260514_am.md` = 2026년 5월 14일 오전 10시 미팅용
- `260514_pm.md` = 2026년 5월 14일 오후 4시 미팅용

## 형식

[`../../.shared/meeting_prep_template.md`](../../.shared/meeting_prep_template.md) 의 AM 또는 PM 양식 그대로 사용.

## 활용

다음 체크 시 직전 스냅샷을 읽어 변화량 산출:

- AM 점검: 어제 PM 스냅샷과 비교 (18시간 윈도우)
- PM 점검: 오늘 AM 스냅샷과 비교 (6시간 윈도우)

주말 미팅이 없으면 월요일 AM에 "지난 금요일 PM 대비"로 비교.

## 보관 정책

- 모든 스냅샷 영구 보관 (6주 프로젝트 종료 후 회고용)
- 주차 종료 시 [`../review/weekN_summary.md`](../review/) 로 주간 요약본 별도 생성 권장
- 주 1회 Pre-mortem 결과는 [`YYMMDD_premortem.md`](./) 형식으로 별도 저장

## 베이스라인

⬜ 정식 일일 점검 시작 시점에 베이스라인 스냅샷 작성. 현재는 [../review/context.md](../review/context.md) 가 베이스라인 역할.
