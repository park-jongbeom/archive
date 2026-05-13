# 1팀 컨텍스트 (Scoffee)

> **사용 방법**: 매일 점검 시 [meeting_prep_template.md](../../.shared/meeting_prep_template.md) 작성 전, 본 문서를 환기하여 팀 고유 정보를 잊지 않도록 한다.
> **갱신 시점**: 단계 전환, 팀 구성 변화, 방법론 변경 시

---

## 1. 프로젝트 정체

| 항목 | 값 |
|---|---|
| 앱명 | **Scoffee** (☕ + Sleep) |
| 도메인 | 카페인 추적 + 수면 영향 + AI 컷오프 산출 |
| 기간 | 2026-05-06 ~ 2026-06-16 (6주) |
| 방법론 | **애자일** (Scrum-lite) |
| 핵심 차별점 | "AI(Gemini)가 나만의 카페인 컷오프 시간 알려주는 앱" |
| 데모 기준 | FLOW-A (입력 → 잔류량 → 워치 알림) 무중단 + FLOW-B (수면 → 리포트) 무중단 |

상세: [product_brief/Product Brief_Scoffee_ver 1.0](../product_brief/Product%20Brief_Scoffee_ver%201.0)

---

## 2. 팀 구성

> 🚨 **2026-05-12 인원 변경**: 팀장/PO **송성호(비전공자) 중도포기** → **손지희(전공자)** 신임 팀장 승계. 부팀장 공백. 인원 4명 → **3명**.

| 역할 | 이름 | 배경 | GitHub | 비고 |
|---|---|---|---|---|
| **팀장** (5/12부 승계) | 손지희 | 전공자 | `starlightfjh` (`son@...`, `starlightfjh@...`) | 테크 리드 + 신임 팀장 → **R-burn-1 점검 대상** |
| (부팀장) | — | — | — | **공백** (5/12부) — 강사 합의로 승계/유지/폐지 결정 필요 |
| 팀원 | 이제이 | 전공자 | `juu124` | UI/디자인 시스템 |
| 팀원 | 임정섭 | 전공자 | `ljs990326-cloud` (`정섭`/`JungSub` 혼용) | Hilt/잔류량 계산 |
| ~~팀장/PO~~ | ~~송성호~~ | ~~비전공자~~ | ~~`sdfg7979-glitch`~~ | **2026-05-12 중도포기** |

**PO 역할**: 송성호 부재로 **공백 (R-PO)**. 손지희 단독 / 이제이·임정섭 분담 — 결정 필요.

상세: [members](../members), [brief_review_w1.md](./brief_review_w1.md)

---

## 3. 외부 자료 위치

| 자료 | 위치 |
|---|---|
| GitHub | https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee |
| 로컬 클론 | `/tmp/snoffee-analysis/Snoffee` (없으면 새로 clone) |
| Figma 보드 | https://www.figma.com/board/NKBVG1F8BcFXzjpnJxsC4u (FigJam, fileKey: `NKBVG1F8BcFXzjpnJxsC4u`) |
| 1팀 figma 자료 | [../figma/](../figma/) |
| 일일 회의록 | [../mom/](../mom/) |
| 베이스라인 스냅샷 | [../snapshots/260513.md](../snapshots/260513.md) |

---

## 4. Brief 핵심 요약 (PRD)

### Must 기능 (필수) 7개

1. 온보딩 (수면/민감도/기상 시간 설정)
2. 카페인 입력 (음료 선택 + 섭취 시간)
3. 체내 카페인 잔류량 실시간 계산
4. 개인화 카페인 컷오프 산출 (Gemini API)
5. WearOS 워치 알림 3종 (섭취 직후 / 컷오프 도달 / 취침 전)
6. 수면 데이터 연동 (Health Services)
7. 주간 카페인-수면 상관관계 리포트

상세 진척 매트릭스: [issue_pr_matrix.md § 2](./issue_pr_matrix.md#2-prd-must-항목--issue--figjam-3중-정합성)

### Won't 기능 (스코프 외, R9 점검 대상)

- 회원가입/로그인 (온보딩 대체)
- 소셜 (친구 공유 등)
- 복잡한 통계 대시보드

---

## 5. Week 1 검토 핵심 (강팀 평가 — 인원 변경 후 재평가 필요)

[brief_review_w1.md](./brief_review_w1.md) 종합 신호등은 **4명 기준 🟢**이었음. 5/12 송성호 중도포기로 **3명 체제 재평가 필요**:

- 3명 모두 전공자 + 프로젝트 경험 — 기술 강도는 여전히 상위
- **Must 7개 도전 가능성 ↓** — 5~6개로 압축 검토 권장 (취침 전 알림 또는 주간 리포트가 Won't 후보)
- mock-first 3건은 그대로 유효:
  1. WearOS 알림 단계 전개 (섭취 직후 → 컷오프 → 취침 전)
  2. Health Services mock-first (Week 3 실연동)
  3. Gemini API mock-first (Week 3 실연동)
- ~~비전공자 팀장 의사결정 부담 완화~~ → **손지희 신임 팀장 부담 분산 + PO 역할 재배분** 으로 톤 전환

→ 일일 점검 시 mock-first 준수 + **R-burn-1 (손지희 부담) + R-PO (PO 공백)** 이 신규 기본 가중치.

---

## 6. 코드 아키텍처 (현재 상태)

```
Clean Architecture (Lite) — MVVM
├── app/             ← 메인 안드로이드
│   ├── core/        ← 공용 UI/네비게이션 (component/, theme/)
│   ├── data/        ← DataSource/Mapper/Repository/DI
│   ├── domain/      ← Model/Repository(I)/UseCase/Util
│   └── presentation/ ← Screen/ViewModel/UiState (onboarding/home/caffeine/report/notification)
└── wear/            ← WearOS 모듈
    └── (alert/service/data/theme)
```

상세: 1팀 종합 진단의 § 4 ([초기 분석 보고](./brief_review_w1.md)의 동일 부분)

### 핵심 파일

- `app/src/main/java/com/snoffee/app/domain/util/CaffeineCalculator.kt` — 반감기 공식, 유일한 단위 테스트 대상
- `wear/` namespace: `com.example.wear` (템플릿 기본값, 변경 권장)

---

## 7. 일일 점검 시 환기할 1팀 고유 사실

매일 회의 30분 전 다음을 머릿속에 다시 올린다:

1. **인원 변경 (5/12)** — 송성호 중도포기 / 손지희 신임 팀장 / 부팀장 공백 / 3명 체제
2. **R-burn-1 손지희 부담** — 테크 리드 + 신임 팀장 + (PO 후보) 누적 시 1.3배 초과 점검
3. **R-PO 공백** — PO 역할 누가 맡는지, 우선순위 결정이 회의에서 누락되지 않는지
4. **Issue 닫힘률 0%** — PR Description에 `Closes #N` 컨벤션 없음 (R2 지속)
5. **Mock-first 합의 3건** — Gemini/Health Services/WearOS 단계 전개 → Week 3 진입 시 실연동 전환 시점
6. **Sprint 1 비어있음** — FigJam Sprint 1 섹션 (id=439:631) 자식 0개 → R11
7. **Must-4, Must-5 3중 빈틈** — Issue 없음 + FigJam 없음 + 코드 스텁만 — 점검 1순위
8. **`sensitivity` 타입 불일치** — Domain enum 있는데 UseCase에서 Int 분기 (코드 리뷰 거리)
9. **wear namespace** — `com.example.wear` (템플릿) → 통일 권장

---

## 8. 회의 톤 가이드 (1팀 전용 — 5/12부 갱신)

- **3명 체제 — 도전적 톤 유지, 단 Must 압축 가능성 열어두기**
- 손지희 신임 팀장에게는 **부담 분산 + 결정 권한 명확화** 톤 (테크 리드 → 팀장 전환)
- 이제이·임정섭에게는 **PO 분담 또는 작업 책임 격상 권유** 톤
- 인원 변경 자체를 부정적으로 다루지 않기 — "남은 3명으로 어떻게 강하게 마무리할지" 프레임
- 코드 리뷰 시: 좋은 점(Clean Architecture, UI 상태 4분기) 먼저 짚고 미시 품질 보완

---

## 9. 일정 (대략)

| Week | 기간 | 목표 (계획) |
|---|---|---|
| Week 1 | 05-06 ~ 05-12 | Brief 합의, 골격 설계, 초기 mock-first 결정 |
| **Week 2 (현재)** | 05-13 ~ 05-19 | FLOW-A 데모, Sprint 1 계획, Issue Closes 정착 |
| Week 3 | 05-20 ~ 05-26 | Gemini/Health 실연동, WearOS 컷오프 알림 |
| Week 4 | 05-27 ~ 06-02 | 취침 전 알림, FLOW-B 시작 |
| Week 5 | 06-03 ~ 06-09 | 주간 리포트 + Gemini 인사이트, 통합 테스트 |
| Week 6 | 06-10 ~ 06-16 | 데모 리허설, main 머지, 회고 |

---

## 10. 참조

- [.shared/daily_check_method.md](../../.shared/daily_check_method.md) — 점검 방법
- [.shared/risk_taxonomy.md](../../.shared/risk_taxonomy.md) — 위험 신호
- [.shared/meeting_prep_template.md](../../.shared/meeting_prep_template.md) — 회의 양식
- [./team_specific_checks.md](./team_specific_checks.md) — 1팀 고유 점검 항목
- [./brief_review_w1.md](./brief_review_w1.md) — Week 1 검토
- [./issue_pr_matrix.md](./issue_pr_matrix.md) — Issue↔PR↔Figma 매트릭스
- [../figma/README.md](../figma/README.md) — Figma 보드 가이드
