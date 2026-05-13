# 주 1회 사전 부검 (Pre-mortem) 양식

> **빈도**: 주 1회, 매주 **금요일 PM 회의 직후** (또는 일요일 저녁)
> **목적**: "이 프로젝트가 실패한다면 무엇이 원인일까"를 미리 상상하여 **overconfidence bias**를 차단
> **근거**: [McKinsey 연구](https://www.parabol.co/resources/pre-mortem-questions/) — pre-mortem 사용 팀은 일반 risk analysis 대비 overconfidence를 유의미하게 감소
> **소요**: 20~30분 (보조강사 단독 수행, 결과는 다음 주 일일 점검에 반영)

---

## 1. 기본 가정 (mental setup)

> **"6주 후, 2026-06-16. 1팀(또는 2팀/3팀) 프로젝트가 실패로 끝났다. 데모는 끊겼고 평가는 낮았다. 이제 거꾸로 원인을 분석한다."**

이 가정에 들어가면 보조강사는 **현재 상태의 낙관 편향에서 벗어나** 잠재 실패 시나리오를 자유롭게 도출할 수 있습니다.

---

## 2. 작성 절차

1. 직전 주 [meeting_prep_template.md 결과 5건](./meeting_prep_template.md) (월~금 PM 스냅샷) 다시 읽기
2. § 3 질문 6개에 답하기
3. 도출된 시나리오 중 **상위 5개** 선정 (가능성 × 영향도)
4. 각 시나리오에 대한 **예방 액션** 1~2개씩 작성
5. [Xteam/snapshots/YYMMDD_premortem.md](../1team/snapshots/) 로 저장
6. 다음 주 일일 체크 시 [Xteam/review/team_specific_checks.md](../1team/review/team_specific_checks.md) 에 반영

---

## 3. 6가지 핵심 질문 ([Parabol pre-mortem 40+ questions](https://www.parabol.co/resources/pre-mortem-questions/) 기반 압축)

### Q1. 우리가 잘못 가정하고 있는 것은?

핵심 의존 가정(예: "Gemini API가 안정적이다", "팀원 모두 일관된 일정으로 작업 가능하다")이 무너지면 어떻게 되는지.

### Q2. 가장 조용한 멤버에게서 무엇을 놓치고 있는가?

발언이 적은 팀원이 막혀있는 영역이 데모 전 발견되지 않으면 어떤 결과로 이어지나.

### Q3. 디자인↔코드 / Issue↔PR 추적 단절이 누적되면?

지금까지 묵인된 단절이 6주차에 어떻게 폭발하나.

### Q4. 데모 시연 1주 전(Week 5)에 무엇이 부족할 것 같은가?

핵심 FLOW의 어느 부분이 가장 늦게까지 끊겨있을 가능성이 큰가.

### Q5. 보안·위생 부채가 임계점을 넘으면?

google-services.json 노출, 키 누출, 위생 부재가 평가에 영향 줄 시나리오.

### Q6. 강사와 보조강사의 시선 차이로 인해 놓칠 가능성은?

강사가 "OK"라고 본 영역에서 실제로 문제가 자라고 있을 시나리오.

---

## 4. 출력 양식

```markdown
# {팀명} Pre-mortem — YYYY-MM-DD (Week N)

## 🎬 가정
2026-06-16 데모 후, {팀명} 프로젝트가 다음 이유로 실패했다고 가정한다.

## 📉 도출된 실패 시나리오 5선

### 시나리오 1: {제목}
- **가능성**: 🔴 높음 / 🟡 중간 / 🟢 낮음
- **영향도**: 🔴 큼 / 🟡 중간 / 🟢 작음
- **현재 신호**: {지금 시점에 이미 보이는 leading indicator}
- **예방 액션**:
  1. {액션}
  2. {액션}
- **점검 시점**: {언제 다시 볼지}

### 시나리오 2: ...

(시나리오 3~5 동일 형식)

## 🎯 다음 주 일일 점검에 반영할 항목
1. {점검 항목 1 — team_specific_checks.md 에 추가할 라인}
2. {점검 항목 2}

## 🔁 지난 주 Pre-mortem에서 도출한 시나리오 진척
| 시나리오 | 상태 (한 주 후) | 비고 |
|---|---|---|
| {지난주 #1} | 해소 / 진행 / 악화 | {근거} |
| {지난주 #2} | ... | ... |
```

---

## 5. 예시 (1팀 가상)

```markdown
# 1팀 Pre-mortem — 2026-05-16 (Week 2 종료)

## 🎬 가정
2026-06-16 데모 후, Scoffee 프로젝트가 다음 이유로 실패했다고 가정한다.

## 📉 실패 시나리오 5선

### 시나리오 1: 손지희 신임 팀장 부담 누적 → 데모 직전 핵심 영역 정체
- 가능성: 🔴 높음
- 영향도: 🔴 큼
- 현재 신호: 5/12부 송성호 PO 중도포기 후 손지희가 (테크 리드 + 신임 팀장 + PO 후보) 3중 역할. 일일 활동이 이미 다른 멤버 평균의 1.3배 초과 추세 → 번아웃 시 Firebase/아키텍처 결정 정체로 FLOW-A·B 둘 다 멈춤
- 예방:
  1. 1주 안에 PO 역할 이제이 또는 임정섭에게 명시 위임 합의
  2. Firebase 단독 책임이던 영역에 페어 1회 (지식 전파)
  3. 강사에 인원 변경 후 부담 상태 1회 보고
- 점검 시점: 매일 (R-burn-1)

### 시나리오 2: WearOS 알림 Week 4까지 미완 → FLOW-A 데모 끊김
- 가능성: 🟡 중간
- 영향도: 🔴 큼 (Brief 핵심 가치)
- 현재 신호: wear/ 모듈 화면 골격만, 실제 발신 로직 미시작, Issue 미등록
- 예방:
  1. Week 3 첫날 WearOS 알림 Issue 등록 + Must-5로 명시
  2. 섭취 직후 알림 1종만 Week 3 종료까지 완성 목표
- 점검 시점: Week 3 중간 (05-16)

### 시나리오 3: Gemini API 비용 폭발 / 응답 정체로 컷오프 산출 실패
- 가능성: 🟡 중간
- 영향도: 🟡 중간
- 현재 신호: 아직 mock 단계, 실호출 시 비용·레이턴시 미검증
- 예방:
  1. Week 3 진입 시 50회 정도 실호출 테스트 + 평균 응답시간 측정
  2. 캐싱 전략 결정 (하루 1회만 호출)
- 점검 시점: Week 3 중간

### 시나리오 4: Issue 추적 단절 누적 → 회고 시 무엇 했는지 복원 불가
- 가능성: 🔴 높음
- 영향도: 🟡 중간 (평가에 영향)
- 현재 신호: 13 PR 머지에 Issue 0건 닫힘
- 예방:
  1. Week 3 첫 PR부터 `Closes #N` 의무화
  2. 회고용 산출물 정리 시간 Week 5에 미리 확보
- 점검 시점: 매일

### 시나리오 5: Sprint 1 백로그 미수립 지속 → Week 3 작업 즉흥 진행
- 가능성: 🔴 높음
- 영향도: 🟡 중간
- 현재 신호: FigJam Sprint 1 빈 상태 지속
- 예방:
  1. 다음 회의 의제 1순위로 격상
  2. Must-4~7 Issue 일괄 등록을 Sprint 1 항목으로 사용
- 점검 시점: Week 3 첫날

## 🎯 다음 주 일일 점검 반영
1. 매일 송성호 활동 ≥ 1건 점검
2. 매일 WearOS 모듈 변화 점검
3. 매 PR description의 `Closes #N` 존재 점검
4. 매 PM Sprint 1 섹션 진척 점검

## 🔁 지난 주 Pre-mortem 진척
(최초 운영이라 해당 없음 — 다음 주부터 채움)
```

---

## 6. 운영 팁

### 6-1. 혼자서도 효과적

Pre-mortem은 보통 팀 단위 워크숍이지만, 보조강사 1인이 실시해도 효과는 큽니다. 핵심은 **현재 시점에서 벗어나 미래의 실패를 상상**하는 것.

### 6-2. 너무 비관적으로 쓰지 말기

- 시나리오는 **현재 leading indicator에 기반**해야 함 (근거 없는 우려 ❌)
- 모든 시나리오는 **예방 액션이 있어야** 함 (불안만 늘리는 부검 ❌)

### 6-3. 지난 주 시나리오 진척 추적

매주 본 양식 § 출력의 마지막 섹션 "지난 주 진척"을 채워 **부검의 부검**도 함께. 시간이 지나면 어떤 패턴의 시나리오가 잘 맞는지 학습 가능.

### 6-4. 강사와 공유 여부

본 양식은 **보조강사 내부용**입니다. 회의에서 활용 가능한 형태로 변환할 때:
- "강사님께 보고할 위험" 1~2개로 압축
- 시나리오 자체보다 "예방 액션 권고"로 표현

---

## 7. 참조

- [.shared/daily_check_method.md](./daily_check_method.md) § 3-5 — 본 양식 사용 맥락
- [.shared/meeting_prep_template.md](./meeting_prep_template.md) — 일일 양식
- [Parabol — 40+ Pre-mortem Questions](https://www.parabol.co/resources/pre-mortem-questions/)
- [Dropbox — How to Run a Pre-mortem](https://experience.dropbox.com/virtual-first-toolkit/effectiveness/run-a-pre-mortem)
- [Scrum.org — Pre-mortem: Preventing Product Failure](https://www.scrum.org/resources/blog/pre-mortem-preventing-product-failure-it-strikes)
