# 3팀 고유 점검 항목 (BBip) — 워터폴 변형

> **⚠️ 워터폴 변형**: 1팀/2팀(애자일)과 점검 방법 다름
> **사용 방법**: 공통 [risk_taxonomy.md](../../.shared/risk_taxonomy.md) **R-W1~R-W4 워터폴 항목** + 본 문서의 3팀 특화 항목
> **갱신 시점**: 매주 일요일, 단계 전환 시
> **현재 단계**: 설계 ↔ 구현 병행 (Week 2 종료 시점)

---

## 1. 3팀 절대 빠뜨리지 말 것 (Top 5)

매 미팅 전 무조건 점검:

| # | 점검 항목 | 명령 / 방법 | 정상 상태 |
|---|---|---|---|
| 1 | **현재 단계 종료 기준 진척** | § 2 Phase-gate 체크리스트 | 종료 임박 시 90% 이상 충족 |
| 2 | **Figma UI 파일 갱신** | `lastModified` 24h 이내 | 활성 단계엔 ≥ 1회/주 |
| 3 | **장지은 외 멤버 활동** | Issue/PR/Figma 댓글 — 주간 ≥ 1건/인 | (워터폴 초기 정상 패턴) |
| 4 | **bbipit/ 모듈 정체** | 모듈명 의도 확인 (워치? 라이브러리?) | Week 3 전 명확화 |
| 5 | **명세서 완성도** | [../prd/](../prd/) 의 핵심 명세 분량·구조 | 단계 종료 전 완성 |

---

## 2. Phase-gate 체크리스트 (워터폴 핵심) ⭐

각 단계 종료 시 보조강사가 단계 전환 권유 전에 점검:

### 2-1. 요구분석/탐색 단계 종료 (Must Meet)

[Smartsheet Phase-gate](https://www.smartsheet.com/phase-gate-process) 기반:

- [ ] Figma 브레인스토밍 보드 "뼈대 잡기" 정리 완료
- [ ] 핵심 기능 목록 5~7개 도출 (Must)
- [ ] 사용자 시나리오 1~2개 정리
- [ ] 기술 제약사항 명시 (워치 SDK, Firebase Functions 등)
- [ ] 차별점 한 줄 명확화
- [ ] **모든 항목 ✅ 후에만 설계 단계 진입 권유**

→ 2026-05-13 현재 추정: ✅ 대부분 완료 (Figma 브레인스토밍 lastModified 05-12부터 정체)

### 2-2. 설계 단계 종료 (Must Meet)

- [ ] Figma UI 파일에 모든 Must 화면 와이어프레임 존재
- [ ] DB 스키마 다이어그램 완성 (모든 entity + 관계)
- [ ] 음성 무전 시퀀스 다이어그램 (신호 흐름 - 클라이언트 → Functions → 다른 클라이언트)
- [ ] 위치 공유 데이터 흐름 다이어그램
- [ ] 워치 UI 5종 명세 (어떤 정보 표시, 어떤 입력 가능)
- [ ] 권한 요청 시점 명시 (마이크, 위치)
- [ ] **모든 항목 ✅ 후에만 구현 본격화 권유**

→ 현재 진행 중. UI 파일은 갱신 중, DB는 시각화 완성.

### 2-3. 구현 단계 종료 (Must Meet)

- [ ] 모든 Must Issue 머지 완료
- [ ] 음성 무전 송수신 동작 (다른 디바이스 간)
- [ ] 위치 공유 실시간 동기화
- [ ] Watch UI 동작
- [ ] FCM 알림 동작
- [ ] 데모 시나리오 1회 무중단 시연 가능
- [ ] **모든 항목 ✅ 후에만 검증 단계 진입 권유**

### 2-4. 검증 단계 종료 (Must Meet)

- [ ] 데모 시나리오 3회 연속 무중단
- [ ] 통합 테스트 작성 + 통과
- [ ] 핵심 시나리오의 로딩/에러/빈 화면 1회 이상
- [ ] README + 발표 자료 완성
- [ ] **모든 항목 ✅ 후 데모 진입**

---

## 3. 3팀 코드 미시 품질 점검

### 3-1. 보안 / 위생 (현재 깨끗)

```bash
cd /tmp/bbip-analysis/BBip

# R13
git ls-files | grep -iE "google-services|\\.env$|\\.keystore$|\\.jks$|\\.p12$|\\.pem$"

# R14
git ls-files | grep -E "^\\.idea/|^\\.vscode/|\\.DS_Store" | wc -l

# API 키 평문
git ls-files | xargs grep -lE "AIza[0-9A-Za-z_-]{30,}" 2>/dev/null
```

→ 2026-05-13 현재 모두 ✅ 깨끗. **이 상태 유지 점검**.

### 3-2. functions/ 모듈 보안 (Firebase Functions 특화)

- 환경 변수 사용 (`functions.config()`)
- 비밀번호/API 키 코드에 평문 ❌
- CORS 설정 명시
- 인증 미들웨어 적용

### 3-3. 멀티 모듈 의존성

- `app/` 이 `bbipit/` 모듈에 의존하는 방향이 올바른지
- `functions/` 가 클라이언트 모듈과 분리되어 있는지

### 3-4. 패키지 구조 변화 추이

최근 커밋 보면 "패키지 구조 변경" 패턴이 잦음 (4건). Week 3 진입 전 안정화 권장:
```bash
git log origin/develop --grep="패키지 구조" --since="7 days ago"
```

---

## 4. 인물별 활동표 양식 (워터폴 변형)

워터폴이라 **commit 외 활동도 동등 가중치**로 봄:

| 멤버 | 24h commit | 24h Issue | 24h Figma | 24h 댓글/리뷰 | 신호 |
|---|---|---|---|---|---|
| 장지은 | N | N | N | N | 🟢/🟡 |
| 정우석 | 0 | N | N | N | 🟢/🟡 |
| 서은신 | 0 | N | N | N | 🟢/🟡 |
| 이유빈 | 0 | N | N | N | 🟢/🟡 |

→ **commit 0이라도 다른 활동이 있으면 🟢**. 모든 활동이 0이면 🟡.

---

## 5. Figma 2개 파일 동시 점검

### 5-1. 브레인스토밍 (FigJam)

- 현재 단계 종료 임박 — **변화량 적음이 정상**
- 단, 미반영 메모/스티키 누적되면 의사결정 정체 신호

### 5-2. UI (Design, editor 권한)

- **현재 단계 활성 산출물** — 일일 갱신 기대
- "화면" 섹션 자식 수 변화 추적
- "DB" 섹션 스키마 변경 추적

[3팀 figma/README § 7](../figma/README.md#7-일일-체크-통합-항목-3팀-특화) 도 동시 참조.

---

## 6. 3팀 특화 R-항목 (워터폴 변형)

본 팀에만 적용되는 위험 신호:

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R-W1** | Must Meet 미충족 단계 종료 시도 | § 2 체크리스트 1개 이상 ❌ 인데 다음 단계 진입 시도 | 단계 종료 거부, 보완 후 재검토 |
| **R-W2** | 단계 정체 | 한 단계가 일정 대비 50% 초과 지연 | 일정 조정 또는 범위 축소 협의 |
| **R-W3** | 명세-구현 비동기 | 설계엔 있는데 구현 미시작 화면 7일 지속 | R12와 유사하나 워터폴 톤 |
| **R-W4** | 핸드오프 문서 부재 | 단계 전환 시 handoff 미작성 | 다음 단계 진입 차단 |
| **R-W5** | bbipit 모듈 정체 미명확 | Week 3 진입 시점 모듈 의도 미문서화 | 즉시 [docs/](../docs/) 에 1줄 기록 |
| **R-W6** | 패키지 리팩토링 반복 | "패키지 구조 변경" 커밋 주간 ≥ 3건 | 아키텍처 확정 후 진행 권유 |

---

## 7. Pre-mortem 결과 반영 (주1회 갱신)

[premortem_template.md](../../.shared/premortem_template.md) 결과 누적.

### 2026-05-13 베이스라인 (참고)

(첫 pre-mortem은 다음 금요일 — 본 섹션은 그때 채움)

---

## 8. Week N 가중치 변동 (워터폴 단계 기준)

워터폴이라 Week 단위가 아닌 **단계 단위**가 우선:

### 설계 단계 종료 임박 (현재 ~ Week 2 종료)
- 🎯 § 2-2 Must Meet 체크리스트 완성
- 🎯 bbipit/ 모듈 정체 명확화
- 🎯 음성 무전 시퀀스 다이어그램 작성

### 구현 단계 (Week 3 ~ Week 5)
- 🎯 무전 송수신 (#8, #9) 우선 진행 (핵심 인터랙션)
- 🎯 정우석/서은신/이유빈 커밋 진입 (분담 시작)
- 🎯 functions/ 모듈 1차 배포

### 검증 단계 (Week 6)
- 🎯 데모 시나리오 무중단 3회
- 🎯 README + 발표 자료
- 🎯 회고

---

## 9. 정보 미수집 항목 (입수 후 본 문서 갱신)

- [ ] 3팀 PRD/명세서 ([../prd/](../prd/) 확인)
- [ ] 3팀 워터폴 단계 일정표 (단계별 종료 목표 날짜)
- [ ] bbipit/ 모듈 의도 (워치? 공통? 라이브러리?)
- [ ] 음성 무전 시퀀스 다이어그램 위치 (Figma? 별도 문서?)

---

## 10. 참조

- [.shared/daily_check_method.md](../../.shared/daily_check_method.md)
- [.shared/risk_taxonomy.md](../../.shared/risk_taxonomy.md) — R-W1~R-W4 워터폴 항목 포함
- [.shared/meeting_prep_template.md](../../.shared/meeting_prep_template.md)
- [./context.md](./context.md) — 3팀 컨텍스트
- [../members](../members)
- [../prd/](../prd/)
- [../figma/README.md](../figma/README.md)
- [../review_260508.md](../review_260508.md), [../review_background_location.md](../review_background_location.md) — 기존 보조강사 메모
- 3팀 메모리: `~/.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md`
- 외부 워터폴 참조:
  - [Smartsheet — Phase-Gate Process](https://www.smartsheet.com/phase-gate-process)
  - [Wikipedia — Phase-Gate Process](https://en.wikipedia.org/wiki/Phase-gate_process)
- **GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip
