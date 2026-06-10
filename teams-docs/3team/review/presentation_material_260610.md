# 3팀 BBip — 발표 슬라이드 소재집

> **작성**: 2026-06-10 / 공통 가이드: [.shared/final_presentation_guide.md](../../.shared/final_presentation_guide.md)
> 슬라이드 5(트러블슈팅)·6(협업규칙)·7(회고/개선)에 비중. **워터폴 팀** — 규칙은 단계 종료 기준·명세 완성도 프레임으로 서술.

---

## 슬라이드 매핑 한눈에

| 슬라이드 | 이 팀의 추천 소재 |
|---|---|
| S1-2 개요 | BBip = 젠리+워키토키, 위치공유 + PTT 무전 + 워치 |
| S4 기술 | Kotlin·Compose·Firebase·Maps·Kakao·Cloud Functions·Wear OS DataLayer·FGS |
| **S5 트러블슈팅** | ① 위치공유 "스냅샷vs주기갱신" → READ/WRITE 분리 ② PTT 무전 실패 ③ AOS14+ FGS |
| **S6 협업규칙** | Must Meet 6항목 / 범위잠금 / Mermaid / 워터폴+칸반 / 보안 CLEAN |
| **S7 회고·개선** | ⭐ 정우석 14일 침묵 → 65h 폭발 → 전원 활성화 |
| S8 성과 | Decision Log 18건(1위), Issue닫힘 40%(1위), 보안 전기간 CLEAN(유일) |

---

## S1-2. 프로젝트 개요

- **한 줄 정의**: "젠리(Zenly) + 워키토키" — 실시간 위치 공유(지도 친구 마커) + 음성 무전(PTT) + DM + Wear OS 워치 연동. 서비스 종료된 젠리의 빈자리를 워치 PTT로 차별화
- **팀**: 4명 — 장지은(팀장) / 정우석(부팀장) / 서은신 / 이유빈
- **방법론**: **워터폴(Phase-gate)** — 역기획(젠리 레퍼런스) → PRD 11종 → Figma 명세 → DB 스키마 → 구현 일괄 진입. Sprint/MVP 다이어트 없음
- 출처: `review_260508.md`, `review/context.md`, `memory/team3_methodology.md`

## S4. 아키텍처 / 기술스택 (왜 골랐나)

| 영역 | 스택 | "왜" |
|---|---|---|
| 클라이언트 | Kotlin + Compose | 선언형 UI |
| Backend | Firebase(Firestore/Auth/RTDB/Storage) + **Cloud Functions** | 실시간 + 서버 로직 |
| 지도 | Google Maps SDK + Kakao SDK | 위치 표시·인증 |
| 워치 | **Wear OS DataLayer**(DataClient/MessageClient/ChannelClient) | 폰↔워치 통신 |
| 위치 | **Foreground Service**(FOREGROUND_SERVICE_LOCATION) + FusedLocationProvider | 백그라운드 위치 |
| 구조 | 멀티모듈(app / bbipit / functions) | 폰·워치·서버 분리 |

---

## S5. 트러블슈팅 (대표 2~3건 — 워치/위치 기술 드라마)

### TS-1. "스냅샷 vs 주기 갱신" 논쟁 → READ/WRITE 레이어 분리 (설계 의사결정 ⭐)
- **문제**: 위치 공유를 Firestore Snapshot 구독 vs 주기적 갱신 중 무엇으로? 강사와 부팀장 답변이 충돌하는 듯 보임
- **가설·검증**: "스냅샷"이 두 기술을 동시에 지칭하고 있었음 — (A) `Firestore addSnapshotListener`(READ 레이어) (B) `FusedLocationProvider.getLastLocation()`(WRITE 입력). 강사는 A, 정우석은 B를 말한 것 → **둘 다 옳음**
- **해결(결합)**: WRITE = Foreground Service + `requestLocationUpdates` 콜백 / READ = 포그라운드에서만 `addSnapshotListener` / 백그라운드 SnapshotListener 금지(OS가 소켓 종료) / Activity Recognition 단독 트리거 금지
- **결과·학습**: 설계 단계에서 5줄로 동결. "워터폴에서 명세 품질이 곧 결과 품질". 3차 회의록 기술 리서치 깊이 = 부트캠프 평균 상회
- 출처: `review_background_location.md`, `feedback_background_processing.md`, `review_260508.md §5`

### TS-2. PTT 무전 100% 실패 (시연 직전 회귀)
- **문제**: 5/27 시점 무전 송신 100% 실패 — 핵심 차별화 기능
- **원인(복합)**: ① `functions/index.js`가 빈 boilerplate인데 클라이언트는 26개 함수 호출(로컬 repo↔콘솔 배포본 불일치) ② PR #194(5/26)에서 `sendVoiceMessageDirect`→`sendVoiceMessage` 일괄 rename 중 시그니처 불일치
- **추가 발견**: 수신자 UID가 1명으로 하드코딩(`Wy102dz…`) → "무전이 한 명한테만 가는 앱"
- **학습**: ① 테스트용 하드코딩값을 production에 잔존시키지 않기 ② 함수명 변경 시 서버 시그니처 동시 확인 ③ **시연 5일 전 fix 외 PR 동결**
- 출처: `memory/code_review_260522_cohort.md`, `memory/runtime_critical_review_260527.md`

### TS-3. Android 14+ / 12+ Foreground Service 정책 대응
- **AOS14+**: 서비스 타입과 권한 불일치 시 앱 시작 불가 → `foregroundServiceType` 4종 OR 정확 매칭(코호트 내 최견고)
- **AOS12+**: 워치가 폰을 깨우는 백그라운드 `startForegroundService`가 `ForegroundServiceStartNotAllowedException` → FCM push로 깨운 뒤 FGS 시작하는 패턴 필요
- **학습**: OS 버전별 백그라운드 정책 강화에 대응하는 게 위치/무전 앱의 핵심 난제
- 출처: `memory/code_review_260522_cohort.md`, `test/TESTER_PARK_BETA_REVIEW_260608.md §B-04`

> **보너스(개선 완료)**: 지도 마커 친구 불일치 → `key(friend.uid)` 안정키 / DM 방 중복 → 서버 `ALREADY_EXISTS` 처리 / 알림 순서 튐 → LazyColumn `key` (모두 6/8 베타에서 수정 확인).

---

## S6. 협업 Ground Rules + 준수율 (워터폴 프레임)

| 규칙 | 동기(왜) | 준수 증거 | 출처 |
|---|---|---|---|
| **Must Meet 6항목** (설계→구현 단계 종료 기준) | 부족한 명세로 구현 진입 시 코드 폐기 비용 | ⚠️ 9회 이월(회고 소재) | `review/team_specific_checks.md §2-2` |
| **In/Out-of-Scope 범위 잠금** | 구현 중 "이거 해야해?" 논쟁 방지 | ✅ 구현 전 분류 | `review_260508.md §4` |
| **이슈/PR 템플릿** (PR에 **트러블슈팅 섹션** 포함) | 추적·회고 | ✅ 5/18 장지은 직접 업데이트 | `repo/.github/` |
| **Mermaid 다이어그램** | Git diff 추적 + PR 리뷰 가능 | ✅ 13건 전체 유효(오진 0) | `memory/diagram_format_signal_260519.md` |
| **워터폴 + 칸반 병행** | 일정 관리 경험 | ✅ 강사 공식 인정 | `memory/team3_pm_260515_signals.md` |
| **보안·위생(R13/R14)** | 시크릿 보호 | ✅ **전 기간 CLEAN(코호트 유일)** | `review/context.md §7` |

**정량 하이라이트**: Decision Log 18건(코호트 1위) / Issue 닫힘률 40%(1위) / 보안 노출 0건(유일)

> 발표 포인트: PR 템플릿에 **트러블슈팅 섹션을 의무화**한 것 자체가 "문제 기록을 규칙으로 만든" 좋은 사례 — 발표 S5와 자연 연결.

---

## S7. 회고 · 개선 — ⭐ 단일 내러티브

### A. 정우석 14일 침묵 → 주말 65시간 폭발 → 전원 활성화

| KPT | 내용 |
|---|---|
| **Problem** | 5/5~5/14 정우석 commit 0(14일). 장지은 단독 ~100% 커밋, 회의도 팀장 단독 브리핑 |
| **원인(blameless)** | 게으름이 아니라 **워터폴 설계 단계 특성** — 명세 완성 전엔 코드 진입이 이른 상태 |
| **Try → 액션** | 5/15 정우석 mom 컨벤션 자생 발화 → 04:48 첫 server-api commit |
| **Keep → 결과** | 5/15~17 **65시간 29 commit** (무전 송수신 Must 핵심 + 7 PR). **5/18 4명 전원 활성화**(50 commit/65h) |

**핵심 메시지**: 워터폴은 명세가 완성되는 순간 구현 에너지가 폭발한다. 단 새벽 commit + 셀프머지 87.5%는 **번아웃 신호**이기도 — "영웅 1명이 아니라 팀이 함께 완주"가 다음 과제.
- 출처: `memory/team3_may26_deadline_signal_260518.md`, `snapshots/260518_am.md`

### B. Must Meet 9회 이월의 솔직한 회고 (워터폴 자기비판)
- **Problem**: 설계 단계 종료 기준 6항목이 공식 점검 없이 구현 진입(5/12~5/19 9회 이월). 5/13 팀 스스로 "선행 작업 부실" 자인
- **Try**: 5/18 "5/26 개발 완료" 일정 명시로 일부 보강
- **학습**: 워터폴에서 단계 종료 기준을 형식적으로 통과시키면 후공정 비용이 커진다 — 명세 게이트를 실제로 닫는 규율이 필요
- 출처: `memory/team3_pm_260515_signals.md`, `memory/team3_may26_deadline_signal_260518.md`

---

## S8. 성과 지표

| 지표 | 값 |
|---|---|
| Decision Log | 18건 (코호트 1위) |
| Issue 닫힘률 | 40% (코호트 1위) |
| 보안 R13/R14 | 전 기간 CLEAN (코호트 유일) |
| Mermaid 검증 | 13건 전체 유효(오진 0) |
| 설계 깊이 | Snapshot 비용·Doze·WakeLock·WorkManager vs FGS 실무 검토 |

---

## 오프닝/클로징용 "한 장면" 스토리

1. **"두 분 다 맞는 말씀입니다"** — 위치 아키텍처 논쟁이 사실은 용어("스냅샷")의 중의성 때문이었고, READ/WRITE 분리로 풀린 이야기. 기술 깊이 + 커뮤니케이션 교훈.
2. **"14일 침묵, 그리고 주말 65시간"** — 정우석 R-quiet 해소 (S7 내러티브 스토리 버전). 워터폴 단계 전환의 에너지.
3. **"무전이 한 명한테만 간다"** — PTT UID 하드코딩 + 빈 functions 발견기. 솔직한 결함 회고 + 재발 방지 규칙(시연 전 동결).

> 클로징 권고: 워터폴 팀답게 "명세 게이트를 더 엄격히 닫았다면"이라는 회고 + "보안·문서·의사결정 기록은 코호트 1위였다"는 강점을 함께 제시하면 균형 잡힌 인상.

---

## 출처 인덱스
`memory/`: team3_methodology, team3_pm_260515_signals, team3_may26_deadline_signal_260518, diagram_format_signal_260519, cohort_mic_issue_260518_pm, cohort_pm_mic_consolidated_260519, code_review_260522_cohort, runtime_critical_review_260527, cohort_ui_stability_260601
`teams-docs/3team/`: review/{context,team_specific_checks}, review_260508, review_background_location, feedback_background_processing, mom/260518_am_minutes, snapshots/{260518_am,260519_pm}, repo/.github/{pull_request_template,ISSUE_TEMPLATE}, prd/, systemflow/, test/{TESTER_PARK_GUIDE_260601,TESTER_PARK_BETA_REVIEW_260608}
