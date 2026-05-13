---
name: team-check-pm
description: "1/2/3팀 오후 4시 팀 미팅 사전 준비. 오늘 10:00 ~ 15:30 변화량 점검 + 오전 액션 진척 추적 + PM 양식 작성. 사용법: /team-check-pm <팀번호> (1/2/3). 보조강사 관점."
effort: medium
---

# team-check-pm — 오후 4시 팀 미팅 사전 준비

## 사용법

```
/team-check-pm 1     ← 1팀 오후 점검
/team-check-pm 2     ← 2팀
/team-check-pm 3     ← 3팀
```

---

## AM과의 차이 (핵심)

| 항목 | AM (10시) | PM (4시) |
|---|---|---|
| **분석 윈도우** | 18시간 (어제 PM ~ 오늘 AM) | **6시간** (오늘 AM ~ 현재) |
| **강조점** | 오늘 어떻게 시작할지 | **오전 결정 진척 + 오후 마무리** |
| **carry-over** | 어제 PM 액션 | **오늘 AM 액션** |

→ PM은 **"오전 회의에서 합의한 것이 실제로 진행됐는가"**를 가장 우선 점검.

---

## 절차 (순서 엄수)

### Step 1. 인자 파싱

AM과 동일. 팀별 경로 매핑은 [team-check-am SKILL](../team-check-am/SKILL.md) Step 1 참조.

### Step 2. 컨텍스트 로드 (순서 엄수)

다음 6개 파일을 **이 순서대로** 읽는다:

1. `teams-docs/.shared/daily_check_method.md`
2. `teams-docs/<X>team/review/context.md`
3. `teams-docs/<X>team/review/team_specific_checks.md`
4. `teams-docs/.shared/risk_taxonomy.md`
5. **`teams-docs/<X>team/snapshots/<오늘날짜>_am.md`** ← 오늘 AM 스냅샷 (가장 중요한 비교 기준)
6. (전날 PM 스냅샷이 있으면 reference로 가볍게)

⚠️ **오늘 AM 스냅샷이 없으면** 사용자에 알리고 다음 중 선택:
- (a) AM 스냅샷 없이 진행 (직전 스냅샷과 비교)
- (b) 사용자가 AM 스냅샷 수동 정보 제공
- (c) 본 호출 종료

### Step 3. 데이터 수집

```bash
# 3-1. Git — 6시간 윈도우
cd <팀 로컬 경로>
git fetch --all --prune 2>&1 | tail -3

# 오늘 09:30 ~ 현재
git log --all --since="6 hours ago" --pretty=format:"%ai | %an | %h | %s"

# 신규 PR (오전 회의 이후)
# WebFetch: github.com/...../pulls?q=is%3Apr+created%3A>=YYYY-MM-DD
```

```bash
# 3-2. Figma — lastModified 비교
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
curl -s -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/<fileKey>?depth=1"
# AM 스냅샷의 Figma lastModified와 비교 → 오전 회의 후 갱신 있었는지
```

### Step 4. 진단

PM 점검의 **핵심 우선순위 3가지**:

**(a) 오전 액션 아이템 진척** — AM 스냅샷의 "💡 보조강사 권장 액션" + 회의 메모에서 추출. 각 액션의 현재 상태:
   - 🟢 완료 (실제 commit/PR/Issue/Figma 변화로 검증됨)
   - 🟡 진행 중 (착수 흔적 있음)
   - 🚨 미시작 (5시간 지났는데 흔적 없음)

**(b) 신규 위험 신호** — 오후에 새로 발견된 R-항목

**(c) 인물별 오전 활동** — AM에서 🚨 표시됐던 멤버가 오후 들어 변화 있었는지 (송성호, 김명준, 정재훈 등)

### Step 5. PM 양식 작성

`teams-docs/.shared/meeting_prep_template.md § 2 PM 양식` 사용:

1. 🟢 한 줄 요약 (오전 회의 후 변화)
2. 📊 6시간 변화 (오늘 10:00 ~ 15:30)
3. ✅ **오전 회의 액션 아이템 진척** ← 가장 중요
4. 🟡 주의 항목 (오후 회의에서 짚을 것)
5. 🚨 즉시 조치 필요
6. 👤 인물별 오전 활동 비교
7. 💡 보조강사 권장 액션 (오후 회의용)
8. 🌙 내일 AM 미팅 carry-over

### Step 6. 스냅샷 저장

```
teams-docs/<X>team/snapshots/<YYMMDD>_pm.md
```

### Step 7. 사용자에 보고

AM과 동일하되 다음 추가:
- 오전 액션 진척 표를 **본문 최상단에** 배치
- 🚨가 있으면 강조
- 내일 carry-over 항목 명확히 표시

---

## 출력 끝 표준 멘트

```
---
✅ 스냅샷 저장: teams-docs/<X>team/snapshots/<YYMMDD>_pm.md
회의 종료 후 carry-over 항목을 team_specific_checks.md에 반영하세요.
오늘 일과 종료 — 내일 오전 `/team-check-am <팀번호>` 호출 (직전 비교 기준: 본 PM 스냅샷).
```

---

## 추가: 금요일 PM 호출 시

금요일 PM 점검 직후 사용자에게 다음 1줄을 안내:

```
💡 금요일입니다. 주1회 사전 부검 권장 → `/team-premortem <팀번호>` 호출 시 5가지 실패 시나리오 도출 가능.
```

(`/team-premortem`은 추후 옵션 4 도입 시 만들어질 별도 skill. 현재는 [.shared/premortem_template.md](../../../teams-docs/.shared/premortem_template.md) 양식 수동 사용)

---

## 주의 사항

- **AM 스냅샷이 없으면 비교 기준이 부실**해진다. 가능한 한 매일 AM부터 절차 시작.
- 오전 합의가 5시간이 지나도 아무 흔적이 없으면 🚨 — 그러나 점심 시간(12~13) 차감 고려하면 실 작업 시간 4시간. 단순 작업은 가능하지만 큰 작업은 부족할 수도.
- **3팀은 워터폴** — Sprint, mock-first 권고 ❌.

## 참조

- [team-check-am SKILL](../team-check-am/SKILL.md)
- [.shared/daily_check_method.md](../../../teams-docs/.shared/daily_check_method.md)
- [.shared/meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md)
- [.shared/risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md)
