# 위험 신호 분류 (Risk Taxonomy) — R1 ~ R14

> **공통 자료** — 1팀(Scoffee, 애자일) / 2팀(Umma, 애자일) / 3팀(BBip, 워터폴) 모두 적용
> **출처**: [.shared/daily_check_method.md](./daily_check_method.md) § 3 + 1팀 [daily_check_guide.md](../1team/review/daily_check_guide.md) 기존 R-list 통합
> **정량 기준이 있어야 같은 진단**이 나오므로, 모호한 표현 피하고 **숫자·일수**로 명시

---

## 1. 카테고리별 위험 신호

### A. 사람 (People) — 4건

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R1** | PO/팀장 무활동 (특히 비전공자 PO) | 7일 누적 commit + Issue + PR 댓글 = 0 | 1:1 의제 1순위 격상 |
| **R8** | 단일 인물 과부하 | 1인이 주간 develop 커밋의 60% 이상 | 작업 재분배 권고, 페어 프로그래밍 권장 |
| **R-out** | 팀원 이탈 신호 | 1인 14일 누적 활동 없음 | 즉시 강사 보고 (생활지도 차원) |
| **R-quiet** | 조용한 멤버 (보조강사가 강사 대비 잘 잡는 항목) | Issue 댓글·PR 리뷰·커밋 모두 주간 1건 이하 | 회의에서 의도적으로 발언 기회 제공 |

### B. 협업 / 워크플로우 (Process) — 4건

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R2** | Issue 미닫힘 누적 | open issue ≥ 10 + closed = 0 지속 | `Closes #N` 컨벤션 교육 |
| **R3** | 셀프 머지 우세 | 최근 5 PR 중 4개 이상 작성자=머지자 | 페어 리뷰 의무화 권고 |
| **R6** | 머지 충돌 폭증 | 1일 내 `Merge branch 'develop' into ...` 3회 이상 | 작업 분리 / 동기화 빈도 협의 |
| **R-cycle** | PR 사이클 정체 | open → merge 평균 ≥ 3일 | 리뷰 정체 원인 1:1 점검 |

### C. 정렬 / 진척 (Alignment) — 3건

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R4** | mock-first 위반 (애자일 1·2팀) | Week 3 이전 Gemini API 키 노출 또는 실호출 commit | 즉시 revert 또는 mock 복원 |
| **R9** | Won't 기능 작업 (스코프 드리프트) | "회원가입", "소셜", "친구 공유" 등 Brief Won't 항목 키워드 commit/PR | Brief § 7 환기 |
| **R12** | 디자인 ↔ 코드 비동기 | Figma에 있는 화면이 코드엔 없거나 그 반대 7일 지속 | 동기화 의제 격상 |

### D. 코드 품질 (Quality) — 2건

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R5** | 테스트 정체 | Week 3 진입 시 단위 테스트 ≤ 2개 | TDD 1회 시연 + Mapper 테스트 권장 |
| **R7** | 아키텍처 위반 | `domain/` 패키지가 `data/` 또는 `presentation/` import (grep 결과 있음) | 즉시 리팩토링 요청 |

### E. 보안 / 위생 (Hygiene) — 2건

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R13** 🚨 | **시크릿/설정 파일 커밋** | `google-services.json`, `*.keystore`, `.env`, 서비스 계정 JSON, API 키 평문(`AIza...`, `sk_live_...`, `figd_...`) 등이 git에 추적됨 | **즉시 보고** + 재발급(rotation) 권고 + `.gitignore` 보강 + git history 정화 검토 |
| **R14** | IDE 아티팩트 누적 | `.idea/`, `.vscode/`, `.idea/shelf/*.patch` 등 3개 이상 발견 | `.gitignore` 표준 패턴 추가 권고 |

### F. 운영 / 데모 (Operations) — 3건

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R10** | Demo Scenario 부재 | Week 2 종료 시 Demo Scenario v0 미작성 | 다음 회의 의제 강제 |
| **R11** | Sprint 1 미계획 (애자일 1·2팀) | Week 3 진입(05-13) 이후 Sprint 1 섹션 비어있음 지속 | 1:1로 Sprint 1 백로그 등록 요청 |
| **R-demo** | 데모 1주 전 FLOW 미동작 | Week 5 진입 시 핵심 FLOW 1개라도 끊김 | 즉시 강사 보고, 다른 작업 중단 권고 |

---

## 2. 워터폴 전용 위험 신호 (3팀 — Phase-gate)

[Smartsheet phase-gate](https://www.smartsheet.com/phase-gate-process) / [Wikipedia](https://en.wikipedia.org/wiki/Phase-gate_process) 기반:

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R-W1** | Must Meet 미충족 단계 종료 시도 | 단계 종료 선언 시 [Must Meet 체크리스트](../3team/review/team_specific_checks.md) 1개 이상 ❌ | 단계 종료 거부, 보완 후 재검토 |
| **R-W2** | 단계 정체 (탐색 → 설계 등) | 한 단계가 일정 대비 50% 초과 지연 | 일정 조정 또는 범위 축소 협의 |
| **R-W3** | 명세-구현 비동기 | 설계 문서엔 있는 화면이 구현엔 없거나 그 반대 7일 지속 | R12와 유사하나 워터폴 톤으로 |
| **R-W4** | 핸드오프 문서 부재 | 단계 전환 시 handoff checklist 미작성 | 다음 단계 진입 차단 |

---

## 3. 즉시 에스컬레이션 트리거

다음 중 **하나라도 발견 시 회의 보고 상단에 🚨 박스로 강조 + 사용자에게 별도 알림**:

- [ ] R1: PO 7일 누적 무활동
- [ ] R-out: 팀원 1인 14일 무활동
- [ ] **R13 🚨: 시크릿 노출** — 즉시 토큰/키 재발급 + history 정화
- [ ] main 브랜치로 직접 push (Git Flow 위반)
- [ ] force push 흔적 (`git reflog show origin/develop` 비정상)
- [ ] 라이선스 위반 라이브러리 추가
- [ ] R-demo: 데모 1주 전 핵심 FLOW 미동작

---

## 4. 위험 신호 점검 명령 모음

각 위험 신호를 1줄 명령으로 점검:

```bash
# R1, R8, R-out, R-quiet — 인물별 활동
git shortlog -sne --since="7 days ago" origin/develop

# R2 — Issue 미닫힘 (WebFetch로 GitHub Issue 페이지 조회)

# R3 — 셀프 머지 (PR 작성자 == 머지자)
git log origin/develop --merges -20 --format="%H %an %s"

# R6 — 머지 충돌 빈도
git log origin/develop --since="24 hours ago" --grep="Merge branch 'develop'"

# R7 — domain → data import 위반 (1팀 Snoffee 기준)
grep -rn "import com.snoffee.app.data" app/src/main/java/com/snoffee/app/domain

# R13 — 시크릿 파일
git ls-files | grep -iE "google-services|\\.env$|\\.keystore$|\\.jks$|\\.p12$|\\.pem$|-key\\.json$"

# R13 — API 키 평문 (전체 파일 grep)
git ls-files | xargs grep -lE "AIza[0-9A-Za-z_-]{30,}|sk_live_|figd_" 2>/dev/null

# R14 — IDE 아티팩트
git ls-files | grep -E "^\\.idea/|^\\.vscode/|\\.DS_Store|Thumbs\\.db"
```

---

## 5. 점검 결과 기록 방식

회의 사전 준비 시 [meeting_prep_template.md](./meeting_prep_template.md) 의 "🚨 위험 신호" 섹션에 발견된 R-ID 목록 + 정량 근거 기재.

예시 (가상 — 1팀은 2026-05-12 인원 변경으로 R1·R-quiet 항목 모두 갱신됨):
```
🚨 위험 신호 발견:
- R-PO (1팀 PO 공백): 5/12부 PO 미배정, PM 회의 후 결정사항 0건 — 다음 회의 의제 강제
- R13 (2팀 시크릿 노출): app/google-services.json 추적 중 — 재발급 권고
- R-burn-1 (1팀 손지희 부담): 신임 팀장 + 테크 리드 + (PO 후보) — open assign 4건 — 작업 분산
```

---

## 6. 갱신 정책

- 매주 일요일: 정량 기준 재검토 (너무 빡빡/느슨한지)
- 신규 위험 패턴 발견 시: 본 문서에 R-항목 추가 후 [.shared/daily_check_method.md](./daily_check_method.md) 와 [meeting_prep_template.md](./meeting_prep_template.md) 도 갱신
- 단계 전환 시 (예: 3팀 설계 → 구현): R-W 항목 가중치 조정

---

## 7. 참조

- [.shared/daily_check_method.md](./daily_check_method.md) — 본 분류의 사용 방법
- [.shared/meeting_prep_template.md](./meeting_prep_template.md) — 점검 결과 기록
- [Atlassian — Daily Standups](https://www.atlassian.com/agile/scrum/standups)
- [PMI — Early Warning Signs](https://www.pmi.org/learning/library/identifying-warning-signs-complex-projects-6259)
- [Smartsheet — Phase-Gate Process](https://www.smartsheet.com/phase-gate-process)
