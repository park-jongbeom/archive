# 1팀 회의록 — 2026-05-13 PM

> **일시**: 2026-05-13 16:00 ~ 16:05 (4분 43초)
> **참여**: 손지희 (신임 팀장), 임정섭 (정섭/JungSub), 강사진
> **원본**: [meeting/2026-05-13_pm_transcript.txt](../meeting/2026-05-13_pm_transcript.txt)
> **작성**: 보조강사 (transcript 기반 정리)

---

## 1. 진척 보고 (오후 작업)

- **손지희 + 정섭**: UI 작업 (앱바, 탭바 추가)
- **정섭**: **FlowB (수면 → 리포트) 진입** — Week 3 진입 신호
- **이제이 (juu124)**: 회의 미언급 — 별도 확인 필요

## 2. 강사 지적 사항

### (a) Issue 백로그 우선 등록
- **지적**: 포인트 관리를 GitHub 외에서만 하면 보조강사가 백로그로 진척 추적 불가
- **합의**: Issue 작성 → 작업 진행 순서 유지
- **담당**: 전원

### (b) 🚨 `google-services.json` 노출 발견
- **지적**: 강사가 회의 중 직접 발견 → 즉시 제거 지시
- **현재 상태**: `app/google-services.json` 추적 중, 노출 API 키 `AIzaSyB9***p_Zbc` (project_id: drink-information)
- **합의**: 손지희 담당, 오늘 중 처리
- **추가 권장 (보조강사 별도 통지)**:
  1. Firebase 콘솔에서 노출된 키 제한/재발급
  2. `.gitignore`에 `app/google-services.json` 패턴 추가
  3. git history 정화 검토

## 3. 운영 체제 합의

- **손지희 브리핑 체제 유지** — "당분간 이 체제로" (강사 발화)
- 부팀장 자리 / PO 역할 분담 / Must 7개 압축 검토 등 인원 변경 후속은 **명시 합의 없음** → 내일 AM 이월

## 4. 결정 사항 (Decision Log)

| # | 결정 | 일자 | 근거 |
|---|---|---|---|
| 1 | 송성호 PO 중도포기 후 손지희 신임 팀장 승계, 브리핑 책임 | 2026-05-12 | [members](../members) + 본 회의 |
| 2 | Issue 등록 → 작업 진행 컨벤션 (R2 해소 시도) | 2026-05-13 PM | 강사 직접 지시 |
| 3 | `app/google-services.json` git 추적 제거 + 노출 키 재발급 검토 (R13) | 2026-05-13 PM | 강사 직접 지시 |
| 4 | 정섭 FlowB 작업 영역 진입 (Week 3 진입 신호) | 2026-05-13 PM | 본인 보고 |

## 5. 액션 아이템

| 액션 | 담당 | 기한 |
|---|---|---|
| `app/google-services.json` 추적 제거 + `.gitignore` 추가 | 손지희 | 오늘 중 |
| 노출 API 키 Firebase 콘솔에서 제한/재발급 | 손지희 | 오늘 중 |
| 신규 작업 PR description에 `Closes #N` 명시 시작 | 전원 | 다음 PR부터 |
| 본 Decision Log 검토 + 다음 회의 합의 추가 | 손지희 | 내일 AM 전 |

## 6. 이월 항목 (내일 AM)

- PO 역할 명시 분담 합의 (손지희 단독 / 이제이·임정섭 분담)
- 부팀장 자리 처리 (공백 / 승계 / 폐지)
- Must 7개 → 5~6개 압축 검토 (3명 체제)
- 이제이 활동 / R-quiet 확인
- google-services.json 제거 검증

## 7. 참조

- 사전 체크리스트 + 회의 결과: [snapshots/260513_pm.md](../snapshots/260513_pm.md)
- 컨텍스트: [review/context.md](../review/context.md)
- 점검 항목: [review/team_specific_checks.md](../review/team_specific_checks.md)
- 5/12 인원 변경 메모리: `~/.claude/projects/.../memory/team1_personnel_change_260512.md`
