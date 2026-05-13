# 3팀 컨텍스트 (BBip)

> **사용 방법**: 매일 점검 시 [meeting_prep_template.md](../../.shared/meeting_prep_template.md) 작성 전, 본 문서를 환기
> **갱신 시점**: 단계 전환 (요구분석 → 설계 → 구현 → 검증), 팀 구성 변화 시
> **⚠️ 방법론**: **워터폴** — 애자일 권고 (Sprint, MVP 다이어트, mock-first) 금지

---

## 1. 프로젝트 정체

| 항목 | 값 |
|---|---|
| 팀명 | **BBipit** |
| 앱명 | **BBip** |
| 도메인 | **친구 그룹 음성 무전(워키토키) + 위치 공유 + 워치 연동** |
| 기간 | 2026-05-06 ~ 2026-06-16 (6주) |
| 방법론 | **워터폴** (Phase-gate) |
| 핵심 기능 | 음성 무전(송수신), 위치 공유, DM, 알림, Watch UI |
| 컬러 톤 | 보라색 계열 (#7900CE) |

---

## 2. 팀 구성 (4명)

| 역할 | 이름 | GitHub | 비고 |
|---|---|---|---|
| **팀장** | 장지은 | `zzingenius` (커밋엔 `zzing`/`장지은`) | 현재 단독 커밋자 (13건) |
| **부팀장** | 정우석 | `useok2481-stack` | Issue 작성 활동 (커밋 0) |
| 팀원 | 서은신 | `Theo-Brie` | Issue 작성 활동 (커밋 0) |
| 팀원 | 이유빈 | `yyukong` | Issue 작성 활동 (커밋 0) |

상세: [members](../members)

### 2-1. 현재 커밋 분포 (2026-05-13 기준)

| 멤버 | 커밋 | 비율 | 평가 (워터폴 컨텍스트) |
|---|---|---|---|
| 장지은 (팀장) | 15 (zzing 13 + 장지은 2) | ~100% | 🟢 워터폴 초기 — 정상 |
| 정우석 | 0 | 0% | 🟡 Issue 활동만 — 설계 단계엔 정상 |
| 서은신 | 0 | 0% | 🟡 동일 |
| 이유빈 | 0 | 0% | 🟡 동일 |

→ **1팀/2팀(애자일)과 평가 기준이 다름**. 워터폴 설계 단계에선 1인 코드 시작 + 나머지 명세·디자인 기여 정상 패턴. **단, Week 3 이후 구현 단계 진입 시점에 분담 패턴 봐야 함**.

---

## 3. 외부 자료 위치

| 자료 | 위치 |
|---|---|
| GitHub | https://github.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip |
| 로컬 클론 | `/tmp/bbip-analysis/BBip` (없으면 새로 clone) |
| Figma 브레인스토밍 (FigJam) | https://www.figma.com/board/X1hPf6U7hRuUGadqBoAr77 (fileKey: `X1hPf6U7hRuUGadqBoAr77`) |
| Figma UI (Design) | https://www.figma.com/design/gjhxpOboz1famP1hlLVbjq (fileKey: `gjhxpOboz1famP1hlLVbjq`, **editor 권한**) |
| 3팀 figma 자료 | [../figma/](../figma/) |
| 3팀 prd | [../prd/](../prd/) |
| 3팀 회의록 | [../mom/](../mom/) |
| 3팀 docs | [../docs/](../docs/) |
| 기존 보조강사 메모 | [../review_260508.md](../review_260508.md), [../review_background_location.md](../review_background_location.md), [../feedback_background_processing.md](../feedback_background_processing.md) |

---

## 4. 워터폴 단계 현황

| 단계 | 산출물 | 상태 (2026-05-13) | 위치 |
|---|---|---|---|
| **요구분석/탐색** | 레퍼런스 마인드맵, 뼈대 잡기 | 🟢 진행 (활성도 낮아짐) | Figma 브레인스토밍 |
| **설계** | UI 화면 + DB 스키마 | 🟢 진행 (오늘도 갱신됨) | Figma UI (editor 권한) |
| **구현** | 안드로이드 코드 | 🟢 초기 진입 (15 commits) | GitHub `FinalProject-BBipit-BBip` |
| **검증** | 테스트 | 🔴 미시작 | — |

→ **요구분석/탐색 → 설계 전환은 사실상 완료**. 현재 **설계 ↔ 구현 병행 상태**. Week 3 이후 검증 단계 진입 시점 점검.

---

## 5. 발견된 앱 기능 (Issue 제목 기반)

| 기능 | Issue | 상태 | 우선순위 |
|---|---|---|---|
| 무전 송신 | #8 | open | Must (핵심 인터랙션) |
| 무전 수신 | #9 | open | Must (핵심 인터랙션) |
| 이메일 인증 회원가입 | #6 | open | Must |
| DM 목록 | #5 | open | Must |
| 알림 목록 | #4 | open | Must |
| 로그인 화면 UI | #10 | open | Must |

→ 6개 미닫힘 Issue + 4개 닫힘 Issue (#1, #2, #3, #7 — 테스트/인증 관련). **Issue 닫힘률 40%로 1팀(0%) 대비 우수**.

---

## 6. 코드 구조 (멀티 모듈)

```
FinalProject-BBipit-BBip/
├── app/               ← 메인 안드로이드 앱
├── bbipit/            ← 별도 모듈 (워치 또는 라이브러리 — 정체 확인 필요)
├── functions/         ← Firebase Cloud Functions ⭐ (1팀/2팀에 없음)
├── .firebaserc        ← Firebase 프로젝트 설정 (안전, secret 아님)
└── firebase.json
```

→ **1팀(app + wear) / 2팀(app) 보다 복잡한 구조**. Firebase Functions 직접 운영.

### 6-1. `bbipit/` 모듈 정체 (확인 필요)

- WearOS 모듈인가? (1팀과 비슷한 패턴)
- 공통 라이브러리 모듈인가?
- → Week 3 진입 전 명확화 권장

---

## 7. 1팀/2팀과의 비교

| 항목 | 1팀 (Scoffee) | 2팀 (Umma) | 3팀 (BBip) |
|---|---|---|---|
| 방법론 | 애자일 | 애자일 | **워터폴** |
| 인원 | 4명 | 5명 | 4명 |
| 도메인 | 카페인-수면 | 영어 학습 | **음성 무전 + 위치 공유** |
| 핵심 기술 | Gemini API + WearOS | Gemini Live + Push-to-Talk | Firebase Functions + Watch + 음성 |
| 커밋 수 | 59 | ~100 | 15 |
| PR 수 | 13 | 48 | 1 |
| Issue 닫힘률 | 0% 🚨 | (미확인) | **40% ⭐** |
| 보안 (R13) | 미확인 | 🚨 노출 | ✅ **깨끗** |
| 위생 (R14) | 미확인 | 🟡 IDE 아티팩트 | ✅ **깨끗** |
| 모듈 구조 | app + wear | app | app + bbipit + functions |
| Figma | 1개 (FigJam) | 1개 (Design) | **2개** (FigJam + Design) |

→ 3팀은 **양적으로 늦지만 질적으로 정돈됨**. 워터폴이라 정상 패턴.

---

## 8. 3팀 특이 사항 (보조강사 환기)

1. **워터폴이라 일일 점검은 가볍게** — 변화 빈도 낮음, **단계 전환 시 집중 점검**
2. **명세 완성도가 핵심 지표** — 코드 진척보다 설계 산출물 정합성 우선
3. **장지은 단독 커밋이 정상** (애자일 R8 적용 ❌) — Week 3 이후 실제 분담 시작 시점 추적
4. **Issue 닫힘률 40%로 모범** — 다른 팀에 모범 사례로 제시 가능
5. **bbipit/ 모듈 정체 확인 필요** — 무엇을 담는 모듈인지
6. **Figma 2개 파일 운영** — 두 파일 모두 lastModified 점검
7. **장지은 GitHub 계정 = `zzingenius`** (Issue) = `zzing` (커밋) — 이메일 다른 게 정상 (학교/개인)

---

## 9. 일정 (워터폴 단계별)

| 단계 | 추정 기간 | 산출물 종료 기준 |
|---|---|---|
| 요구분석 | 05-06 ~ 05-12 | Figma 브레인스토밍 정리 완료 |
| 설계 | 05-13 ~ 05-19 | UI 화면 + DB 스키마 + 명세서 완료 |
| **구현 (현재)** | 05-20 ~ 06-09 | 모든 Must Issue 머지 |
| 검증 | 06-10 ~ 06-16 | 데모 시나리오 무중단 + 통합 테스트 |

> ⚠️ 본 일정은 추정. [prd/](../prd/) 입수 후 확정.

---

## 10. 회의 톤 가이드 (3팀 전용)

- **워터폴 톤 유지** — Sprint, MVP 다이어트, mock-first 권고 ❌
- **명세 완성도 / 단계 종료 명확성** 강조 ✅
- 장지은 팀장에게 코드 분담 압박 ❌ (워터폴 초기엔 정상)
- 정우석/서은신/이유빈에게 Issue 활동 ≥ 1건/주 격려
- 단계 전환 시 [team_specific_checks.md § 2 단계 종료 체크리스트](./team_specific_checks.md) 활용

---

## 11. 메모리 참조

3팀 워터폴 메모: `~/.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md`

핵심 내용:
- 3팀은 워터폴 방법론 확정
- MVP 다이어트·스프린트 권고 금지
- 명세 완성도 강조로 조언

---

## 12. 참조

- [.shared/daily_check_method.md](../../.shared/daily_check_method.md)
- [.shared/risk_taxonomy.md](../../.shared/risk_taxonomy.md)
- [.shared/meeting_prep_template.md](../../.shared/meeting_prep_template.md)
- [./team_specific_checks.md](./team_specific_checks.md) — 3팀 고유 점검 (워터폴 phase-gate 포함)
- [../members](../members)
- [../prd/](../prd/)
- [../figma/README.md](../figma/README.md)
- [../review_260508.md](../review_260508.md), [../review_background_location.md](../review_background_location.md) — 기존 보조강사 메모
- **GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip
