# 미팅 사전 준비 양식 v2 — 통합 체크리스트

> **버전**: v2 (2026-05-14 작성, v1 [meeting_prep_template.md](./meeting_prep_template.md) 대체 후보)
> **근거**: [external_practices.md](../../operation/docs/research/external_practices.md) (외부 베스트 프랙티스) + [current_pattern_gap.md](../../operation/docs/research/current_pattern_gap.md) (gap 분석) + 사내 [애자일 가이드](../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md) / [FAQ](../../ta-guides/애자일_예제기반_FAQ.md)
> **구조**: 공통 80% + 팀별 부록 20% — 단일 파일
> **분량 목표**: 작성된 양식 1회분 A4 1.5장 이내, 회의 중 빠르게 참조 가능

---

## 0. 사용 흐름 (v1 대비 변경점만)

### 0-1. 작성 → 회의 → 갱신 (큰 흐름 v1 동일)

```
회의 30분 전: 양식 작성 (자동 데이터 + 보조강사 진단)
회의 5분 전: 🚨 강사 사전 통지 의제 3건을 강사 채팅/1:1로 전달  ← v2 신규
회의 진행: 강사 발언 우선, 보조강사는 마이크 가능시 보완 + 메모 기록
회의 후 5분: 양식에 회의 결과 반영 + carry-over 다음 회차로 자동 이월
```

**B2 운영 룰**: carry-over **2회+ 이월** 항목은 §🚨 강사 사전 통지에 자동 1순위로 격상 + **회의와 별개로 강사 1:1 별도 통지 권유** (회의 시간 부족으로 또 누락 방지).

### 0-2. v1 대비 핵심 변경 (요약)

| 변경 | 위치 | 근거 |
|---|---|---|
| 🚨 즉시 조치 칸을 양식 **최상단**으로 이동 | §1 | gap §3-1 (R13 강사 망각) |
| 🚨 **강사 사전 통지 의제 3건** 칸 신설 | §2 | gap §3-2 (보조강사 마이크 SPOF) |
| "Finished / Will finish by when" 프레임 | §4 | Phase1 §1-1 |
| **Walking the Board** "오늘 끝낼 수 있는 작업 3개" (AM only) | §5 | Phase1 §1-2 |
| **Carry-over 자동 재출현 + 이행률 N/M%** | §6 | Phase1 §4-3 |
| **신호등 1단어 회고** (PM 마지막 1줄) | §13 (PM) | Phase1 §4-2 |
| 주차별 최소 Done 1줄 헤더 자동 import | §0-3 | 가이드 §4-3 |
| R-attend / R-burn 표준 R-항목으로 노출 | §10 | gap §3-3 |
| Phase-gate Must Meet 6항목 헤더 (3팀 부록) | §14 | 가이드 §4-3 + 3팀 review |

### 0-3. 주차별 최소 Done (가이드 §4-3 자동 import)

| Week | 최소 Done | 1·2팀 적용 | 3팀(워터폴) 적용 |
|---|---|---|---|
| W1 (2026-05-04~) | Flow 1개 + Issue 3~7 + PR ≥ 1 + 데모 1개 | ✅ | (워터폴: 탐색·요구분석 단계) |
| W2 (2026-05-11~) | 플로우 A 성공 경로 데모 고정 | ✅ | (워터폴: 설계 단계 종료) |
| W3 (2026-05-18~) | 플로우 A에 로딩/에러/빈/예외 포함해 데모 | ✅ | (워터폴: 구현 단계 진입) |
| W4 (2026-05-25~) | 플로우 B 성공 경로 데모 고정 | ✅ | (워터폴: 구현 중반) |
| W5 (2026-06-01~) | 플로우 B에 로딩/에러/빈/예외 + 회귀 체크 | ✅ | (워터폴: 검증 단계) |
| W6 (2026-06-08~) | Freeze + Demo Scenario 무중단 데모 | ✅ | ✅ (공통) |

→ AM/PM 양식 헤더에 **해당 주차 최소 Done 1줄만 인용**.

---

## 1. AM 양식 (오전 10시 미팅용)

작성 시각: 09:30 / 분석 윈도우: **어제 16:00 ~ 오늘 09:30 (≈18h, 야간 포함)**
권장 회의 timebox: **AM 7~10분** (강제 X, 평균 5'21" 현 수준 대비 보강 목적)

```markdown
# {팀명} AM 일일 체크 — YYYY-MM-DD (Week N, D일차)

> **베이스라인**: [snapshots/YYMMDD_pm.md](./YYMMDD_pm.md)
> **이번 주 최소 Done**: {Week N 최소 Done 1줄 인용 — §0-3에서 import}
> **회의 timebox 권장**: 7~10분 / 실제 시간: __분 __초 (회의 후 기록)

---

## 🔍 사전 점검 자동화 명령 (양식 작성 직전 1줄 실행 — B1)

회의 30분 전 다음 명령으로 R13/R14/팀 클론 상태 점검:

```bash
cd /tmp/<team>-analysis/<TeamRepo>   # 1팀 Snoffee / 2팀 Umma / 3팀 BBip
# R13 시크릿 파일
git ls-files | grep -iE "google-services|\\.env$|\\.keystore$|\\.jks$|\\.p12$|\\.pem$|-key\\.json$"
# R13 API 키 평문
git ls-files | xargs grep -lE "AIza[0-9A-Za-z_-]{30,}|sk_live_|figd_" 2>/dev/null
# R14 IDE 아티팩트
git ls-files | grep -E "^\\.idea/|^\\.vscode/|\\.DS_Store|Thumbs\\.db" | wc -l
# 24h 활동
git shortlog -sne --since="24 hours ago" origin/develop
# 7일 활동 (R-out / R-quiet 후보)
git shortlog -sne --since="7 days ago" origin/develop
```

→ 결과를 §🚨 즉시 조치 + §🚦 R-항목 점검에 반영.
상세 명령: [risk_taxonomy.md §4](./risk_taxonomy.md).

---

## 🚨 즉시 조치 필요 (최상단 — 회의 시작 직후 강사 보고)
{24h+ 미해소 R 또는 보안·위생 critical. 없으면 "없음" 명시}

예: 🚨 R13 — `app/google-services.json` 36h+ 미해소. 강사 5/13 16:05 직접 지시했음.

---

## 🚨 강사 사전 통지 의제 3건 (회의 5분 전 채팅/1:1 전달)

회의에서 짚어달라고 강사에게 통지할 압축 의제 — 보조강사 마이크 이슈 대비 백업 채널:

1. **(회의 첫 안건 권유 — B3)** {1순위 — 가장 critical, 보통 §🚨 즉시 조치 항목 또는 carry-over 2회+ 이월}
2. {2순위 — 신규 발현 R-항목 또는 carry-over 2회+ 이월}
3. {3순위 — 인물별 신호 / 좋은 점 격려 1건}

→ 회의 후: 강사가 발화한 항목에 ✅ 표시, 미언급은 PM으로 자동 이월.
→ **B2**: carry-over 2회+ 이월 시 회의와 별개로 강사 1:1 별도 통지 (회의 또 누락 방지).

---

## 🟢 한 줄 요약
{전체 신호등 + 어제 종료 후 핵심 변화 1문장}

---

## 📊 18시간 변화 — Finished / Will finish by when

### 어제 PM 이후 Finished (완료된 작업)
- 머지된 PR: {번호 + 1줄 설명}
- 닫힌 Issue: {번호}
- 머지된 카드 (보드 Done 컬럼 신규): {N개}

### Will finish by when (오늘 마감 약속)
- {PR/Issue/카드 — 담당자 — 마감 시점}

### 정량 수치 (보조강사 관점)
- 신규 커밋: N건 (어제 PM 6h 대비 비율)
- 신규/닫힌 Issue: +N / -N (open 총 N건)
- Figma lastModified: {timestamp + 영역}
- 활성 기여자: {이름들 — 발화 0건이라도 작업 있으면 명시}

---

## 🎯 오늘 끝낼 수 있는 작업 3개 (Walking the Board)

보드 오른쪽(완료 임박) → 왼쪽(신규) 순회. "오늘 끝낼 수 있는 것"을 3개 선정:

1. {Issue #N / 담당자 / 어디서 막혔는지 — 없으면 "막힘 없음"}
2. {Issue #N / 담당자}
3. {Issue #N / 담당자}

→ 회의에서 강사가 발화 트리거: "오늘 끝낼 수 있는 게 뭐예요?"

---

## 🔁 Carry-over 자동 재출현 + 이행률

이전 회차에서 합의된 액션의 **회의-내 다뤄짐** vs **데이터-기반 자동 진척**을 분리:

| Carry-over 항목 | Owner | 마감 | 회의에서 다뤘나? | 데이터 상태 | 이월 횟수 |
|---|---|---|---|---|---|
| {항목1} | {담당} | {기한} | ✅/❌ | {결과} | N회 |
| {항목2} | ... | ... | ... | ... | ... |

**이행률**:
- 회의-내 이행률: N/M (M개 carry-over 중 회의에서 다뤄진 N개)
- 데이터-기반 진척률: N/M

**2회 이상 이월된 항목은 자동 🚨 격상** — §🚨 강사 사전 통지 의제 1순위 후보.

---

## 🚦 R-항목 점검 (공통 R1~R14 + 팀 특화 R + 신규 표준)

회의 시작 직전 정량 점검 결과:

| R-ID | 신호 | 정량 근거 | 어제 PM 상태 | 오늘 AM 상태 | 변화 |
|---|---|---|---|---|---|
| R13 | (해당시) | ... | ... | ... | ... |
| R-attend | (팀 멤버 1명 결석/조퇴) | 결석/조퇴 사유 명시 | ... | ... | ... |
| R-burn | (팀 단일 인물 1.3x 부담) | 활동량 + 발화 + 본인 진술 | ... | ... | ... |
| ... | ... | ... | ... | ... | ... |

→ 팀 특화 R는 §13~§15 부록에서 자동 import (1팀: R-PO·R-burn-1·R-MN·R-DL·R-Must7 / 2팀: R-out-2·R-WIP·R-burn / 3팀: R-W1~W6)

---

## 👤 인물별 24h 활동 + 발화 정합성

| 멤버 | 24h commit | 24h Issue | 24h PR/머지 | 7일 누적 | 어제 회의 발화 | 신호 |
|---|---|---|---|---|---|---|
| {이름} | N | N | N | N | ✅/❌ | 🟢/🟡/🚨 |

→ **commit ≥ 1 + 회의 발화 0** = R-quiet 자동 flag.
→ **commit 0 + 회의 발화 0 + 결석/조퇴 사유 없음** = R-out 후보.

---

## 📋 Issue↔PR 정합성 (1·2팀 애자일 — 3팀 §15로 분기)

- 신규 PR `Closes #N` 명시: M/N
- 신규 닫힌 Issue: {번호들}
- 7일+ stale open Issue: {번호들}

→ 0이면 R2 (Issue Closes 미준수).

---

## 🎨 FigJam/Design 보드 변화
- lastModified: {timestamp}
- 신규 페이지/섹션: {있으면}
- Sprint 1 / 현재 단계 진행: {애자일은 Sprint 1, 워터폴은 현재 단계명}

---

## 💡 보조강사 권장 액션 (회의 중 push 3건 압축 — 좋은 점 1건 포함 원칙)

1. 🟢 **격려 (좋은 점부터)** — {예: 정재훈 R-out-2 회복}
2. 🚨 **critical push** — {예: R13 처리 진척 직접 확인}
3. 🟡 **신호 환기** — {예: 인물별 R-quiet 발언 기회}

---

## 📁 저장
`teams-docs/{Xteam}/snapshots/YYMMDD_am.md`

---

## (선택) 📚 근거 인용 — 신규 R-항목 도입시만
- {R-새이름}: [가이드 §N](../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md) 또는 [FAQ §N](../../ta-guides/애자일_예제기반_FAQ.md)
```

---

## 2. PM 양식 (오후 4시 미팅용)

작성 시각: 15:30 / 분석 윈도우: **오늘 10:00 ~ 15:30 (≈6h, 오전 작업)**
권장 회의 timebox: **PM 10~15분** (carry-over 누적 시 ≥ 15분 권장)

```markdown
# {팀명} PM 일일 체크 — YYYY-MM-DD (Week N, D일차)

> **베이스라인**: [snapshots/YYMMDD_am.md](./YYMMDD_am.md)
> **이번 주 최소 Done**: {Week N 최소 Done 1줄}
> **회의 timebox 권장**: 10~15분 / 실제 시간: __분 __초

---

## 🔍 사전 점검 자동화 명령 (B1 — PM 작성 직전)

```bash
# 오전 활동 + R13/R14 재점검
git log origin/develop --since="$(date '+%Y-%m-%d') 10:00" --oneline
git ls-files | grep -iE "google-services|\\.env$|\\.keystore$"
git shortlog -sne --since="6 hours ago" origin/develop
```

---

## 🚨 즉시 조치 필요 (최상단)
{AM 후 새로 발현한 또는 미해소 24h+ 항목}

---

## 🚨 강사 사전 통지 의제 3건 (회의 5분 전 채팅/1:1)
1. **(회의 첫 안건 권유 — B3)** {1순위}
2. {2순위}
3. {3순위}

→ **B2**: carry-over 2회+ 이월 시 회의와 별개로 강사 1:1 별도 통지.

---

## 🟢 한 줄 요약
{오전 회의 후 변화 1문장}

---

## 📊 6시간 변화 — Finished / Will finish by when

### 오전 회의 후 Finished
- 머지된 PR: {번호}
- 닫힌 Issue: {번호}
- 머지된 카드: {N개}

### Will finish by when (오늘 일과 종료 약속 = 18:00 기준)
- {담당자별 마감 예정}

### 정량 수치
- 신규 커밋: N건 (오전 6h)
- 신규/닫힌 Issue: +N / -N
- Figma 변화: {Y/N}

---

## ✅ 오전 회의 액션 아이템 진척

| 액션 | 담당 | 오전 합의 상태 | 현재 (15:30) | 진척 |
|---|---|---|---|---|
| {액션1} | {담당} | 합의 | 진행중/완료/미시작 | 🟢/🟡/🚨 |

→ **미시작 비율 ≥ 50%** = R-action (PM에서 다시 한번 push 필요).

---

## 🔁 Carry-over 자동 재출현 + 이행률

(§1-AM과 동일 양식 — 어제 PM + 오늘 AM 미해소 항목 모두 import)

| 항목 | Owner | 마감 | AM 회의에서 다뤘나? | 데이터 상태 | 이월 횟수 |

→ **회의-내 이행률** = M/N + **데이터 진척률** 동시 표기.

---

## 🚦 R-항목 점검 (오전→오후 변화 강조)

| R-ID | 오전 09:30 | 오후 15:30 | 변화 |
|---|---|---|---|

---

## 👤 인물별 오전 활동 비교 (오전 회의 발화 정합성)

| 멤버 | 오전 commit | 오전 Issue/PR | 오전 회의 발화 | 신호 |

---

## 💡 보조강사 권장 액션 (오후 회의 3건 압축)
1. 🟢 좋은 점부터
2. 🚨 critical push
3. 🟡 신호 환기

---

## 🌙 내일 AM carry-over (자동 이월 + 우선순위)

회의 미해결 + 새 발생 항목 = 내일 AM의 §🚨 강사 사전 통지 후보:

| 항목 | Owner | 마감 | 이월 횟수 | 자동 격상? |
|---|---|---|---|---|
| {항목} | {담당} | {기한} | N회 | 2회+ 시 🚨 |

---

## 🚦 오늘 신호등 (1단어 회고 — 30초)

회의 종료 직전 강사 + 보조강사 각자 1단어:

- 강사: 🔴/🟡/🟢 + 1단어 (예: "🟢 진척", "🟡 부담", "🔴 정체")
- 보조강사: 🔴/🟡/🟢 + 1단어
- (선택) 팀장/PO: 🔴/🟡/🟢 + 1단어

→ 같은 색이면 align, 다르면 align 안 됨 = 다음 AM 의제 1순위.

---

## 📁 저장
`teams-docs/{Xteam}/snapshots/YYMMDD_pm.md`

---

## (선택) 📚 근거 인용 — 신규 R-항목 도입시만
```

---

## 3. 공통 작성 가이드라인

### 3-1. 시간 제한 — 양식 작성당 30분

| 작업 | 소요 | 비고 |
|---|---|---|
| 자동 데이터 수집 (git + Figma + Issue) | 5분 | [daily_check_method.md §5](./daily_check_method.md) |
| 직전 스냅샷 읽기 + carry-over 자동 import | 3분 | YYMMDD_(am|pm).md 직전 회차 |
| 팀별 컨텍스트 환기 + Top 5 점검 | 5분 | [Xteam/review/team_specific_checks.md §1](../1team/review/team_specific_checks.md) |
| 팀 특화 R-항목 진단 | 5분 | [Xteam/review/team_specific_checks.md §6](../1team/review/team_specific_checks.md) |
| 공통 R1~R14 정량 점검 | 5분 | [risk_taxonomy.md §4](./risk_taxonomy.md) |
| 양식 작성 (변경 부분만 채움 — v1 대비 자동화 ↑) | 7분 | 본 문서 §1 / §2 |

총 30분. **자동 import 비중 ↑로 v1 대비 5분 단축 가능**.

### 3-2. 작성 톤 (v1 §3-3 유지)
- **사실 → 평가 → 액션** 순서
- "OO팀원이 게으르다" ❌ / "OO팀원 commit 0 + Issue 댓글 0 = R-quiet" ✅
- **좋은 점부터 1건 이상** ([FAQ §10](../../ta-guides/애자일_예제기반_FAQ.md) "❌ 칭찬만" 안티패턴 회피 + "좋은 점부터" 원칙 동시 충족)

### 3-3. 보조강사 권장 액션 3건 압축 룰

- **3건 초과 X** — 더 많은 항목은 carry-over로 다음 회차 이월
- **각 1줄 30자 이내** — 강사가 즉시 발화 가능한 분량
- **순서**: 🟢 격려 → 🚨 critical → 🟡 환기 (좋은 점 → 위험 → 정상화)

### 3-4. 강사 사전 통지 채널 (마이크 SPOF 백업)

- **1차 채널**: 강사 1:1 채팅 (회의 5분 전 1줄 전달)
- **2차 채널**: 강사 본인 카톡 (회의 직전 메모 형식)
- **3차 채널**: 회의 중 보조강사 본인 화이트보드/공유 메모

→ 본 양식의 §🚨 강사 사전 통지 의제 3건은 **회의 전에 무조건 전달 완료**. 마이크는 회의 중 추가 보완용.

---

## 4. 갱신 정책

### 4-1. v1 → v2 전환 일정
- 2026-05-14 PM: v2 일부 적용 (강사 사전 통지 의제 채널 도입)
- 2026-05-15 AM: v2 전체 적용 시작
- 2026-05-19 (월) 회고: 1주 운영 후 효과 측정

### 4-2. 측정 지표
- **회의-내 carry-over 이행률** — 목표: 30% → 50% (1주 후)
- **회의 시간** — 현재 평균 5'21" → 권장 7~10분 (AM) / 10~15분 (PM)
- **🚨 즉시 조치 24h+ 정체** — 목표: 0건

### 4-3. 갱신 주체
- 매주 일요일: 보조강사 1인 회고 + 본 양식 수정
- 단계 전환 시 (예: 3팀 설계 → 구현): §14 부록 갱신
- 신규 R-항목 발견: [risk_taxonomy.md](./risk_taxonomy.md) 등록 후 본 양식 자동 import

---

## 5. 부록 A — 신규 표준 R-항목 (v2에서 risk_taxonomy.md에 추가 예정)

기존 [risk_taxonomy.md](./risk_taxonomy.md) 에 다음 2건 추가 필요:

### R-attend (출결 / 결석)
- **정량 기준**: 팀 멤버 1명이 회의 결석 + 1일 활동 < 평소의 30%
- **조치**:
  1. 사유 확인 (단순 결석 / 거리 / 건강 / 가정)
  2. 1일 단발: 회의록 기록 + 다음 회의 복귀 확인
  3. 2일 연속: 강사 직접 1:1 통지 + 분담 재조정
  4. 3일 이상: 강사 보고 + 회사 정책 확인
- **근거**: [가이드 §11](../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md) (팀 역할 — 결석시 분담), 2026-05-14 1팀 정섭 결석 사례

### R-burn (피로 누적 / 번아웃 leading indicator)
- **정량 기준**: 팀 단일 인물이 다음 중 2개 이상 발현
  - 일일 활동량 ≥ 다른 멤버 평균 × 1.3
  - open assign ≥ 4
  - 본인 또는 강사가 명시 우려 발언 ("힘들다", "무리한다" 등)
- **조치**:
  1. 작업 분산 권장 (페어 또는 위임)
  2. 강사가 본인에게 페이스 조절 명시 권유
  3. 매일 점검 (PM 회의 끝 신호등 회고로 보강)
- **근거**: 가이드 §15-1 "진행이 안 될 때", 2026-05-14 1팀 손지희 사례 ("아 너무 힘들어요"), Phase 1 §3-2 (Burnout 대응)

→ 기존 1팀 R-burn-1 / 2팀 R-burn은 **R-burn의 팀별 변형**으로 재정의. 양식에서는 공통 R-burn으로 표시 + 팀별 임계값만 부록에서 명시.

---

## 6. 부록 B — 1팀 특화 (애자일, 3명 체제, 인원 변경 후속)

### B-1. 1팀 Top 5 — 매 회의 점검 ([team_specific_checks.md §1](../1team/review/team_specific_checks.md))

1. 손지희 신임 팀장 부담 (R-burn-1) — open assign ≤ 3
2. PO 공백 (R-PO) — 결정사항 ≥ 1건/회의
3. Issue Closes 컨벤션 — 신규 PR 모두 `Closes #N`
4. Sprint 1 FigJam 상태 — Week 3 진입 후 > 0
5. Mock-first + Must-4/5 진척 — Week 3 시작 시 Issue 등록

### B-2. 1팀 R-항목 추가
- R-PO, R-burn-1, R-MN, R-DL, R-Must7 (전부 [team_specific_checks.md §6](../1team/review/team_specific_checks.md))

### B-3. 1팀 특별 톤
- "비전공자 PO 부담 완화" 톤 X → **"손지희 신임 팀장 부담 분산 + 작업 재배분 합의"** 톤
- 매일 인물 활동표 3행 (송성호 제거)

---

## 7. 부록 C — 2팀 특화 (애자일, 5명 체제, 적응 멤버 2명)

### C-1. 2팀 Top 5

1. 김명준/정재훈 적응 멤버 활동 — commit/Issue/PR ≥ 1
2. 박재민 WIP 제한 — open assign < 4
3. google-services.json 노출 (R13-2) — 해소까지 매일 환기
4. 정원화 부담 점검 (R-burn) — 활동 ≤ 박재민 × 1.3
5. Gemini Live 비용/레이턴시

### C-2. 2팀 R-항목 추가
- R-out-2, R-WIP, R-burn, R13-2

### C-3. 2팀 특별 톤
- 적응 멤버 본인 발화 의도적 유도 (R-quiet 페어 매칭)
- 박재민에게 책임 가중 X, "분담 합의" 톤
- PR 자동 템플릿·Hilt 가이드 등 좋은 작업 격려

---

## 8. 부록 D — 3팀 특화 (워터폴, 4명 체제, Phase-gate)

### D-1. 3팀 Top 5

1. 현재 단계 종료 기준 진척 (Must Meet 6항목)
2. Figma UI 파일 갱신 (24h 이내)
3. 장지은 외 멤버 활동 (commit 외 Issue/Figma 등가 가중치)
4. bbipit/ 모듈 정체 명확화
5. 명세서 완성도

### D-2. 3팀 R-항목 추가 (워터폴 변형)
- R-W1 (Must Meet 미충족 단계 종료)
- R-W2 (단계 정체)
- R-W3 (명세-구현 비동기)
- R-W4 (핸드오프 문서 부재)
- R-W5 (bbipit 모듈 정체)
- R-W6 (패키지 리팩토링 반복)

### D-3. 🚧 Phase-gate Must Meet 6항목 — 매 회의 헤더 자동 import

설계 단계 종료 ([team_specific_checks.md §2-2](../3team/review/team_specific_checks.md)):
- [ ] Figma UI 모든 Must 화면 와이어프레임
- [ ] DB 스키마 다이어그램
- [ ] 음성 무전 시퀀스 다이어그램
- [ ] 위치 공유 데이터 흐름 다이어그램
- [ ] 워치 UI 5종 명세
- [ ] 권한 요청 시점 명시

→ 매 AM/PM 양식 헤더에 **충족률 N/6** 노출.

### D-4. 3팀 절대 금지 톤 ([team3_methodology.md](../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md))
- ❌ MVP 다이어트 권고
- ❌ Sprint 단위 빠른 반복
- ❌ 회고 후 백로그 재조정
- ✅ "명세 단계에서 범위 잠금 → 구현 단계는 명세 준수"
- ✅ 명세 완성도 강조

---

## 9. 참조

- v1 양식: [meeting_prep_template.md](./meeting_prep_template.md)
- Phase 1 외부 조사: [external_practices.md](../../operation/docs/research/external_practices.md)
- Phase 2 gap 분석: [current_pattern_gap.md](../../operation/docs/research/current_pattern_gap.md)
- 사내 가이드: [애자일 본 가이드](../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md), [보조강사 FAQ](../../ta-guides/애자일_예제기반_FAQ.md)
- 운영 지침: [operation/CLAUDE.md](../../operation/CLAUDE.md)
- 일일 점검 방법론: [daily_check_method.md](./daily_check_method.md)
- 위험 분류: [risk_taxonomy.md](./risk_taxonomy.md)
- Pre-mortem (주1회): [premortem_template.md](./premortem_template.md)
