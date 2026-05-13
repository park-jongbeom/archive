# 1팀 고유 점검 항목 (Scoffee)

> **사용 방법**: 매일 점검 시 [risk_taxonomy.md](../../.shared/risk_taxonomy.md) 공통 R1~R14 항목 + 본 문서의 1팀 특화 항목을 동시에 점검
> **갱신 시점**: 매주 일요일, Pre-mortem 결과 반영 시
> **현재 가중치**: Week 2 (2026-05-13 ~ 05-19) 진입 시점 기준

---

## 1. 1팀 절대 빠뜨리지 말 것 (Top 5 — 2026-05-12 인원 변경 반영)

> 🚨 **5/12부 변경**: ~~송성호 PO 24h 활동~~ R1 점검 종료 → **손지희 부담 + PO 공백** 신규 항목으로 대체.

매 미팅 전 무조건 점검:

| # | 점검 항목 | 명령 / 방법 | 정상 상태 |
|---|---|---|---|
| 1 | **손지희 신임 팀장 부담 (R-burn-1)** | 손지희 24h 활동 (commit + Issue + PR + 댓글) + open assign 수 | open assign ≤ 3, 일일 활동 = 다른 멤버의 1.3배 이내 |
| 2 | **PO 공백 신호 (R-PO)** | 회의 후 우선순위/스코프 결정 흔적 (Issue 등록 / Decision Log) | 매일 결정 사항 ≥ 1건 명시 |
| 3 | **Issue Closes 컨벤션** | 신규 PR description에 `Closes #N` 또는 `Fixes #N` 포함 여부 | 신규 PR 모두 포함 |
| 4 | **Sprint 1 FigJam 상태** | Figma `/v1/files/.../nodes?ids=439:631` 응답의 children.length | Week 3 진입 후 > 0 |
| 5 | **Mock-first 합의 준수 + Must-4·5 진척** | grep `AIza...` / Samsung Health 실호출 패턴 / Issue·commit 활동 | Week 3 전엔 mock 유지, Week 3 시작 시 Issue 등록 |

---

## 2. PRD Must 매트릭스 (매일 갱신)

[issue_pr_matrix.md § 2](./issue_pr_matrix.md#2-prd-must-항목--issue--figjam-3중-정합성) 의 상태를 매일 점검 시 환기:

| Must # | 기능 | 코드 | Issue | FigJam | 점검 가중치 |
|---|---|---|---|---|---|
| 1 | 온보딩 | 🟢 | ❌ | ✅ | 낮음 (Issue 등록만 권장) |
| 2 | 카페인 입력 | 🟢 | ✅ | ✅ | 낮음 |
| 3 | 잔류량 계산 | 🟢 | ✅ | ✅ | **완성** — 단위 테스트 확장만 |
| 4 | Gemini 컷오프 | 🟡 | ❌ | ❌ | 🚨 **3중 빈틈 — Week 3 핵심** |
| 5 | WearOS 알림 | 🟡 | ❌ | ❌ | 🚨 **3중 빈틈 — Week 3 섭취 직후만이라도** |
| 6 | Health Services | 🟡 | ❌ | ✅(수면 분기) | Week 3 시작 |
| 7 | 주간 리포트 | 🟡 | ❌ | ✅(Flow-B) | Week 4 시작 |

---

## 3. 1팀 코드 미시 품질 점검 (보조강사 특화)

강사가 잘 안 보는 항목:

### 3-1. 아키텍처 위반

```bash
# domain이 data 또는 presentation을 import하면 위반
grep -rn "import com.snoffee.app.data\\|import com.snoffee.app.presentation" \
  /tmp/snoffee-analysis/Snoffee/app/src/main/java/com/snoffee/app/domain
```

→ 결과 있으면 즉시 짚어야 함.

### 3-2. 도메인 enum 무력화

- `app/src/main/java/com/snoffee/app/domain/usecase/caffeine/CalculateResidualUseCase.kt`
- `CaffeineSensitivity` enum 있는데 sensitivity를 Int로 받아 when 분기 중 — 코드 리뷰 거리

### 3-3. wear 모듈 namespace

- `wear/build.gradle.kts` namespace = `com.example.wear` (템플릿 기본값)
- → `com.snoffee.wear` 통일 권장 (Play Console 충돌 방지)

### 3-4. 단위 테스트 정체

- 현재 `CalculateResidualUseCaseTest.kt` 1건만
- Week 3 진입 전 +4건 목표 (Mapper 우선)

### 3-5. 신규 TODO 누적

```bash
grep -rn "TODO\\|TODO(\"Not yet implemented\")" \
  /tmp/snoffee-analysis/Snoffee/app/src/main/java | wc -l
```

→ Week 2 종료 시점 ~20건. Week 3 진입 후 줄어야 정상.

---

## 4. Figma 보드 1팀 특화 점검

| 항목 | 방법 |
|---|---|
| Sprint 1 (id=439:631) 자식 수 | 0개 지속 시 R11 |
| 와이어프레임 (id=207:144) 갱신 여부 | 변경 시 코드 동기화 대기 점검 (R12) |
| 떠다니는 피드백 메모 (페이지 최상위) | 미반영 누적 시 디자인 의사결정 정체 신호 |

[1팀 figma/README § 5](../figma/README.md#5-일일-체크-통합-항목) 의 체크리스트도 동시 참조.

---

## 5. Decision Log 점검

Week 1 검토에서 권장한 Decision Log 6건이 [teams-docs/1team/](../) 어디에도 없음. 매 PM 회의 시:

- [ ] 오늘 결정사항이 발생했나
- [ ] 발생했다면 어디에 기록됐나
- [ ] 누락된 결정사항은?

→ **PM 회의 후 결정사항 1줄이라도 [mom/YYMMDD](../mom/) 에 기록 유도**.

---

## 6. 1팀 특화 R-항목 (공통 R1~R14 외 추가) — 2026-05-12 갱신

본 팀에만 적용되는 위험 신호:

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R-PO** ⭐ | PO 공백 — 우선순위/스코프 결정 부재 (5/12부 신규) | 회의 후 결정사항 0건 기록 또는 Issue 등록 없음 지속 ≥ 2회 | 다음 회의에서 PO 역할 분담 합의 강제 |
| **R-burn-1** ⭐ | 손지희 신임 팀장 부담 1.3배 초과 (5/12부 신규) | 손지희 일일 활동 (commit+Issue+PR+댓글) 합산 ≥ 다른 멤버 평균 × 1.3 / open assign ≥ 4 | 작업 분산 권장, PO 역할은 이제이/임정섭 검토 |
| ~~R1-1~~ | ~~송성호 PO GitHub 계정 미매핑~~ | **5/12 송성호 포기로 무효화** | — |
| **R2-1** | `[Issue A-N]` 컨벤션 깨짐 | 새 Issue가 PRD Must 번호와 매핑 안 되는 제목 | 다음 회의 짧게 환기 |
| **R-MN** | Must-N 동시 빈틈 | Issue + FigJam + 코드 3중 미시작 항목 (Must-4, Must-5 현재 해당) | 다음 회의 의제 |
| **R-DL** | Decision Log 미작성 | PM 회의 후 mom/ 에 결정사항 0건 기록 | 다음 AM 회의 첫 안건 — **5/12 인원 변경 기록부터 시작** |
| **R-Must7** | Must 7개 도전이 3명 체제에서 과부하 | Week 3 중반에 Must-4·5·6·7 중 2개 이상 진척 0 | Must 압축 (Won't 후보: Must-7 또는 Must-5의 취침전) |

---

## 7. Pre-mortem 결과 반영 (주1회 갱신)

[premortem_template.md](../../.shared/premortem_template.md) 에서 도출된 시나리오의 leading indicator를 본 섹션에 누적:

### 2026-05-13 베이스라인 (참고)

(첫 pre-mortem은 다음 금요일 — 본 섹션은 그때 채움)

---

## 8. Week N 가중치 변동 (단계별)

같은 항목이라도 주차마다 가중치 다름:

### Week 2 (현재)
- 🎯 FLOW-A 데모 가능성
- 🎯 Issue Closes 컨벤션 정착
- 🎯 **인원 변경 영향 정리** (5/12 송성호 포기 / 손지희 승계 / PO 역할 재배분)
- 🎯 **손지희 부담 분산** (R-burn-1)
- 🎯 Sprint 1 계획 + Must 압축 여부 결정

### Week 3 (다음 주)
- 🎯 Gemini/Health Services 실연동 진입
- 🎯 WearOS 섭취 직후 알림 실 동작
- 🎯 단위 테스트 +4건
- 🎯 Demo Scenario v1

### Week 4
- 🎯 WearOS 알림 3종 모두
- 🎯 Health 권한 다이얼로그
- 🎯 FLOW-B 시작

### Week 5
- 🎯 주간 리포트 + Gemini 인사이트
- 🎯 통합 테스트

### Week 6
- 🎯 데모 리허설
- 🎯 main 머지 + 회고

→ 본 가중치는 [.shared/daily_check_method.md § 8](../../.shared/daily_check_method.md) 의 갱신 정책에 따라 주 1회 검토.

---

## 9. 참조

- [.shared/daily_check_method.md](../../.shared/daily_check_method.md)
- [.shared/risk_taxonomy.md](../../.shared/risk_taxonomy.md)
- [.shared/meeting_prep_template.md](../../.shared/meeting_prep_template.md)
- [./context.md](./context.md) — 1팀 컨텍스트
- [./brief_review_w1.md](./brief_review_w1.md)
- [./issue_pr_matrix.md](./issue_pr_matrix.md)
- [./daily_check_guide.md](./daily_check_guide.md) — 인덱스 (구조 개편 후 단순화됨)
