# 미팅 사전 준비 양식 (10AM / 4PM 변형)

> **사용 방법**: 회의 30분 전 자동 데이터 수집 후 본 양식을 채워 [`Xteam/snapshots/YYMMDD_(am|pm).md`](../1team/snapshots/) 로 저장
> **양식 두 종류**: AM(오전 10시) / PM(오후 4시) — 분석 윈도우와 강조점이 다름
> **분량**: 한 미팅당 A4 1장 분량, 회의 중 빠르게 참조 가능해야 함

---

## 1. AM 양식 (오전 10시 미팅용)

회의 시작 30분 전(09:30) 작성. 분석 윈도우 = **어제 16:00 ~ 오늘 09:30** (≈ 18시간, 야간 작업 포함).

```markdown
# {팀명} 일일 체크 — YYYY-MM-DD AM (Week N, D일차)

## 🟢 한 줄 요약
{전체 신호등 + 어제 종료 후 핵심 변화 1문장}

## 📊 18시간 변화 (어제 16:00 ~ 오늘 09:30)
- 신규 커밋: N건 (어제 PM 대비)
- 머지된 PR: {번호 목록}
- 신규/닫힌 Issue: +N / -N (open 총 N개)
- Figma lastModified 갱신: {Y/N + 영역명}
- 활성 기여자: {이름들}

## 🎯 오늘의 키 포인트 (회의 의제로 추정)
- Must-N 진척 — {지표}
- 잔여 Demo Scenario 작업
- {팀 고유 점검 항목 — team_specific_checks.md 참조}

## 🟡 주의 항목 (강사가 놓칠 가능성)
- {보조강사 관점 — 인물별 미세 활동·코드 디테일 등}
- 예: 신임 팀장 손지희 open assign 4건 누적 (R-burn-1 후보)
- 예: 어제 머지된 #18 — Issue #20 미닫힘 (R2 패턴 지속)

## 🚨 즉시 조치 필요
{R1/R13/R-out 등 발견 시. 없으면 "없음"}

## 👤 인물별 활동 (보조강사 특화)
| 멤버 | 24h commit | 24h Issue | 24h PR 리뷰 | 신호 |
|---|---|---|---|---|
| {팀원1} | N | N | N | 🟢/🟡 |
| {팀원2} | N | N | N | 🟢/🟡 |

## 📋 Issue↔PR 정합성
- 신규 PR 중 `Closes #N` 명시: M/N
- 신규 닫힌 Issue: {번호들}
- 7일+ stale Issue: {번호들}

## 🎨 FigJam/Design 보드 변화
- lastModified: {타임스탬프}
- 신규 섹션/페이지: {있으면 이름}
- Sprint/단계 진행: {애자일은 Sprint 1 상태, 워터폴은 현재 단계}

## 💡 보조강사 권장 액션 (오전 회의에서 짚을 항목)
1. {액션 — 강사에게 보완 제안할 형태}
2. {액션}

## 🔁 어제 PM에서 carry-over 항목 진척 점검
- {어제 4PM 회의 액션 아이템} → {오늘 09:30 시점 상태}

## 📁 저장
`teams-docs/{Xteam}/snapshots/YYMMDD_am.md`
```

---

## 2. PM 양식 (오후 4시 미팅용)

회의 시작 30분 전(15:30) 작성. 분석 윈도우 = **오늘 10:00 ~ 15:30** (≈ 6시간, 오전 작업 시간만).

```markdown
# {팀명} 일일 체크 — YYYY-MM-DD PM (Week N, D일차)

## 🟢 한 줄 요약
{오전 회의 후 변화 1문장}

## 📊 6시간 변화 (오늘 10:00 ~ 15:30)
- 오전 회의 후 신규 커밋: N건
- 신규 머지 PR: {번호}
- 신규/닫힌 Issue: +N / -N
- Figma 변화: {Y/N}

## ✅ 오전 회의 액션 아이템 진척
| 액션 | 담당 | 오전 합의 상태 | 현재 (15:30) | 진척 |
|---|---|---|---|---|
| {액션1} | {담당자} | 합의 | 진행 중/완료/미시작 | 🟢/🟡/🚨 |
| {액션2} | {담당자} | 합의 | ... | ... |

## 🟡 주의 항목 (오후 회의에서 짚을 것)
- {오전 결정이 실행으로 안 이어졌으면 그 패턴}
- {오후에 새로 발견된 보조강사 시각}

## 🚨 즉시 조치 필요
{있으면}

## 👤 인물별 오전 활동 비교
| 멤버 | 오전 commit | 오전 Issue/PR | 신호 |
|---|---|---|---|

## 💡 보조강사 권장 액션 (오후 회의에서 짚을 항목)
1. {액션}
2. {액션}

## 🌙 내일 AM 미팅으로 carry-over
- {오늘 PM에서 합의했는데 내일 점검할 항목}
- {잠재 위험 — 미해결 채로 일과 종료 시 영향}

## 📁 저장
`teams-docs/{Xteam}/snapshots/YYMMDD_pm.md`
```

---

## 3. 작성 가이드라인 (공통)

### 3-1. 시간 제한 — 미팅당 30분

| 작업 | 소요 | 비고 |
|---|---|---|
| 데이터 수집 (Bash + WebFetch + Figma API) | 5분 | [daily_check_method.md § 5](./daily_check_method.md) 명령 |
| 직전 스냅샷 읽기 (diff용) | 3분 | YYMMDD_(am|pm).md 직전 회차 |
| 팀별 컨텍스트 환기 | 3분 | [Xteam/review/context.md](../1team/review/context.md) |
| 팀 고유 항목 진단 | 7분 | [Xteam/review/team_specific_checks.md](../1team/review/team_specific_checks.md) |
| 위험 신호 점검 | 5분 | [risk_taxonomy.md § 4 명령](./risk_taxonomy.md#4-위험-신호-점검-명령-모음) |
| 양식 작성 | 7분 | 본 문서 § 1 / § 2 |

총 30분. 익숙해지면 20분으로 단축 가능.

### 3-2. 우선순위 5문항 (시간 부족 시 이것만)

[daily_check_method.md § 3](./daily_check_method.md) 보조강사 5가지 관점을 시간 제약 시 압축:

1. 어제 대비 PR/커밋 수는?
2. Issue 닫힘률은? 추적 단절은?
3. 인물별 미세 활동에 이상 없나?
4. 보안/위생 (R13/R14) 신규 발견?
5. Mock-first / phase-gate 위반 신호?

### 3-3. 작성 톤

- **사실 → 평가 → 액션** 순서로
- "OO팀원이 게으르다" ❌ / "OO팀원 commit 0 + Issue 댓글 0 = R-quiet" ✅
- 강사 발언을 대체하지 않고 보완
- 좋은 점 1개 이상 발견·언급

### 3-4. 카운트가 어려운 시간대

- 야간 작업 (어제 22:00 ~ 자정): AM 윈도우에 포함됨
- 자정 ~ 새벽 (00:00 ~ 09:30): AM 윈도우에 포함됨
- 점심 (12:00 ~ 13:00): PM 윈도우 안에 있어 영향 적음

---

## 4. 예시 (1팀 AM — 2026-05-14 가상, 5/12 인원 변경 반영)

```markdown
# 1팀 일일 체크 — 2026-05-14 AM (Week 3, 9일차)

## 🟢 한 줄 요약
Week 3 진입 첫날. 어제 4PM 회의에서 손지희 신임 팀장 승계 + PO 역할 분담 합의(이제이 PO). Sprint 1 백로그 등록 흔적 일부 있음 🟡.

## 📊 18시간 변화 (어제 16:00 ~ 오늘 09:30)
- 신규 커밋: 3건 (juu124 2, son 1)
- 머지된 PR: 없음
- 신규/닫힌 Issue: +1 / -0 (open 7개)
- Figma lastModified: 변화 없음
- 활성 기여자: juu124, son (3명 체제)

## 🎯 오늘의 키 포인트
- Must-4 Gemini 컷오프 mock → 실연동 진입 (Week 3 합의)
- Issue Closes #N 컨벤션 시작
- Sprint 1 백로그 등록 (R11 해소 시도)
- 인원 변경 Decision Log 1건 작성 (어제 PM 약속)

## 🟡 주의 항목 (강사가 놓칠 가능성)
- 손지희 open assign 3건 → 4건 임박 (R-burn-1 leading indicator)
- 어제 PR #18 머지 후 Issue #20 여전히 open (R2 지속)
- 임정섭 어제 commit 0건 (R-quiet 후보, 2일 연속)

## 🚨 즉시 조치 필요
없음

## 👤 인물별 활동 (3명 체제)
| 멤버 | 24h commit | 24h Issue | 24h PR 리뷰 | 신호 |
|---|---|---|---|---|
| 손지희 (신임 팀장) | 1 | 1 | 0 | 🟡 부담 추세 |
| 이제이 (PO 분담) | 2 | 0 | 0 | 🟢 |
| 임정섭 | 0 | 0 | 0 | 🟡 2일 연속 |

## 💡 보조강사 권장 액션
1. 손지희 부담 분산 — Firebase 외 영역에 페어 1회 합의
2. 강사가 "Sprint 1 어떻게 채울지" 의제 잡으실 때, 백로그 후보(Must-4~7)를 보조 제시
3. 임정섭 발언 기회 의도적으로 — 어제 작업 보고 1회 청취 권유

## 🔁 carry-over 진척
- 어제 PM "인원 변경 Decision Log 1건" → 부분 진행 (mom/260513_personnel_change.md 초안 있음)
- 어제 PM "PO 역할 이제이 분담 합의" → ✅ 합의됨
- 어제 PM "Sprint 1 백로그 등록" → 미진행 (FigJam Sprint 1 여전히 빈 상태)
```

---

## 5. 양식 변형 — 주 1회 Pre-mortem (별도)

매주 금요일 PM 회의 후 [premortem_template.md](./premortem_template.md) 별도 작성. 본 양식은 매일 사용, pre-mortem은 주 단위.

---

## 6. 참조

- [.shared/daily_check_method.md](./daily_check_method.md) — 사용 절차
- [.shared/risk_taxonomy.md](./risk_taxonomy.md) — 위험 신호 정량 기준
- [.shared/premortem_template.md](./premortem_template.md) — 주1회 사전 부검
- 외부:
  - [Range — Daily Standup Agenda](https://www.range.co/blog/complete-guide-daily-standup-meeting-agenda)
  - [Geekbot — Daily Standup Questions](https://geekbot.com/blog/daily-standup-questions/)
