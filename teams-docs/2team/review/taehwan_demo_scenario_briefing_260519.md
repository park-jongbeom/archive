# 2팀 김태환 2차 1:1 — 데모 시나리오 작성 범위 + 업무 분담 (5/20 AM 미팅 사전 자료)

> **작성**: 2026-05-19 / 보조강사
> **대상**: 5/20 AM 2팀 미팅 본인 발화 + 강사진 공유
> **목적**: 김태환 2차 자발 상담 결과 정리 + 산업 표준 자료 교차검토 후 5/20 AM 미팅 의제 확정
> **사전 1:1**: [taehwan_difficulties_briefing_260519.md](./taehwan_difficulties_briefing_260519.md) (5/19 오전)
> **관련 1:1**: [jaehoon_withdrawal_context_260518.md](./jaehoon_withdrawal_context_260518.md) (5/18 정재훈 중도포기 회복 사이클)

---

## ⚠ 사전 주의

1. **김태환 2차 자발 상담** — DM 채널 견고화 sentinel ⭐⭐ (오전 1:1 마지막 *"또 DM 할게요"* 발화의 실제 이행 확인)
2. 본 브리프는 1차 보조강사 답변 + **인터넷 자료 교차검토 결과**를 모두 포함. **1차 답변 중 1건 수정 사항** 존재 (§4 참조)
3. 5/20 AM 미팅에서 보조강사가 직접 발화할 의제이므로, 내용 톤은 "팀 자율 합의 우선 + 권고는 권고로" 유지

---

## 1. 상담 내용 요지

### 김태환 본인 안건
> "데모 시나리오 작성 범위 기준이 애매하다. 팀에서 2가지 의견이 나왔다.
> 1. **스프린트 기준** 데모시나리오
> 2. **플로우 기준** 데모시나리오"

### 김태환 후속 질문
> "플로우 기준으로 하면, 스프린트 기간 안에 플로우가 모두 안 끝나면 어떻게 하느냐?"

---

## 2. 보조강사 1차 답변 (상담 중 전달)

| # | 답변 | 근거 |
|---|---|---|
| A | 팀이 원하는 방향으로 **합의하는 게 베스트** | 팀 자율 우선 원칙 |
| B | 나라면 **플로우 기준** 추천 | 플로우에 성공 기준 이미 명시 → 모듈 단위 체크 용이 / 화면 흐름 검증 자연스러움 |
| C | 플로우가 스프린트 안에 안 끝나면 두 옵션 | (C-1) 플로우 자체가 너무 크면 **재정리** / (C-2) **스프린트 기간을 플로우 완료 시점에 가변 조정** |
| D | 데모 시나리오 작성 **분담 + 상호 논의** 권장 (한 명 전담 ❌) | 부트캠프 학습 단계 — "실무에선 결국 본인이 혼자 작성해야 한다. 지금 다 같이 경험을 쌓아야" |
| E | 정원화가 혼자 맡고 있는 **문서 작업도 분담** 권장 | 동일 학습 논리 |
| F | 차주 스프린트 문서는 **최소 필요분만 빠르게 합의** | 합의 자체에 시간 끌지 말 것 |

---

## 3. 산업 표준 자료 교차검토 결과

### ✅ A·B (플로우 기준 데모 시나리오) — 산업 표준과 정확히 일치

| 출처 | 핵심 인용 |
|---|---|
| Atlassian Sprint Demo | "데모는 **사용자 여정(user journey)** 워크스루 형태가 권장됨. 분리된 기술 컴포넌트보다 효과적" |
| UAT Best Practices (Quellit) | "사용자 스토리 기반 **end-user 관점 시나리오**를 plain language로 작성" |
| UX Design Sprint (Boana) | 첫날부터 "**user flow sessions**"으로 모든 스토리 함께 설계 |
| 오픈소스컨설팅 애자일 가이드(4) | "데모 가능한 시나리오로 표현할 것이 스토리 작성의 중요한 기준" |

→ "플로우에 이미 성공 기준이 명시되어 있어 모듈 단위 체크가 쉽다"는 1차 답변의 근거가 **정확함**. 5/20 AM 그대로 발화 가능.

---

### ⚠️ C-2 (스프린트 기간 가변 조정) — **1차 답변 수정 필요**

**문제**: C-2(스프린트 기간을 플로우 완료 시점에 맞춰 가변 조정)는 **Scrum 공식 anti-pattern으로 분류됨**.

| 출처 | 핵심 인용 |
|---|---|
| Scrum.org "27 Sprint Anti-Patterns" | "스프린트 길이를 며칠 연장해 Sprint Goal을 맞추는 것은 **agile이 아니라 책 조작(cooking the books)**" |
| Age-of-Product Sprint Anti-Patterns | "**이해관계자 참여(stakeholder inclusion)를 망가뜨리고**, 스크럼 이벤트의 정상 cadence를 무너뜨린다" |
| Agile Alliance "User stories fit in sprint" | "사용자 스토리는 **반드시 하나의 스프린트에 들어가도록 설계**되어야 한다. 안 들어가면 **쪼개라**(split), 기간을 늘리지 마라" |

**수정안** — 5/20 AM에는 C-2 대신 아래 두 옵션으로 발화:
- **(C-1) 플로우를 더 작게 쪼개기** (← 1차 답변 유지)
- **(C-2′) 만약 플로우가 본질적으로 짧게 못 쪼개진다면, 아예 고정 스프린트(Scrum)를 버리고 flow-based / Kanban으로 전환** — "스프린트인 척하면서 기간만 늘이기"가 가장 나쁨

**2팀 컨텍스트 적용**: 2팀은 v2 양식 83.3% (5/18 AM peak) + Sprint 2 완전 이행 사이클로 **이미 스프린트 cadence가 안정**. 칸반 전환은 굳이 필요 없음 → **(C-1) 플로우 쪼개기를 1순위 권고**.

(참고: 3팀이 워터폴+칸반 조합을 선택한 이유가 바로 이 지점)

---

### ✅ D·E (분담) — 방향 맞음, **단서 1개 추가 권장**

**검토 결과**: 산업 자료는 양면 모두 존재.

| 입장 | 주요 출처 인용 |
|---|---|
| **단일 담당 옹호** | Quora/Scrum.org: "Single-owner stories preferred when possible — 책임 명확화, 핸드오프 최소화, 속도" |
| **분담·페어 옹호** | "Pairing developers with testers" 짝 단위 한 시나리오 함께 작성 베스트 프랙티스 |

**수정안** — 5/20 AM에 단서 추가:
> "실무에선 단일 담당이 책임 명확화·속도 면에서 더 효율적일 수도 있습니다. **다만 지금은 부트캠프 학습 단계**이고, 모두가 한 번씩은 데모 시나리오·문서 작성을 경험해야 실무에서 혼자 작성할 수 있는 역량이 생깁니다. 그래서 **지금 시점에는 분담 + 상호 리뷰(pair review)**를 권장합니다."

→ 이 단서 없이 발화하면 *"그럼 실무에서도 분담이 정답인가?"* 라는 오해 가능성. 단서 추가 시 학습 목적이 명확해짐.

---

### ✅ F (문서 최소·빠른 합의) — Just-enough doc 원칙과 일치, 수정 불필요

---

## 4. 5/20 AM 2팀 미팅 의제 (확정안, 4블록)

### 블록 1. 데모 시나리오 기준 — 팀 합의 우선, 보조강사 추천은 "플로우 기준"
- **결정 원칙 명시**: 두 안 모두 가능. 팀이 합의한 방향이 정답. 합의 자체에 시간 끌지 말 것
- **보조강사 추천: 플로우 기준** (이유 2가지)
  1. 플로우 문서에 이미 **성공 기준/목표치**가 작성되어 있어 모듈 단위 체크가 쉬움
  2. 플로우 단위 테스트가 **화면 동작 흐름 검증**에 자연스러움

### 블록 2. "플로우가 스프린트 안에 안 끝나면?" — 조정 옵션 (**수정 적용**)
- **옵션 A (1순위)**: 플로우 자체가 너무 크게 산정되었다면 **플로우를 더 작게 쪼개기**
- **옵션 B (예외)**: 플로우가 본질적으로 짧게 못 쪼개지면 **고정 스프린트가 아닌 칸반(flow-based)으로 전환** — 단, 2팀은 이미 스프린트 cadence가 안정화되어 있으므로 굳이 필요 없음. **옵션 A 1순위**.
- ⚠️ **명시적 안내**: "스프린트 기간을 가변 조정하는 건 일반적으로 스크럼에서는 권장되지 않는 패턴(anti-pattern)입니다"

### 블록 3. 업무 분담 — "다 같이 경험하자" (**단서 추가**)
- **데모 시나리오**: 한 명 전담 ❌ → 스프린트당 플로우 2~4개를 **각자 1개씩 맡고 서로 논의**
- **문서 작업도 동일**: 정원화 훈련생 혼자 부담하는 구조 → **분담 + 상호 리뷰**
- **단서 (필수)**: "실무에선 단일 담당이 책임 명확화·속도 면에서 더 효율적인 경우도 많습니다. 다만 지금은 부트캠프 학습 단계라 분담 + 상호 리뷰를 권장"
- **근거 (꼭 같이 전달)**: "실무에선 결국 본인이 혼자 작성해야 한다. **프로젝트 경험할 때 다 같이 경험해야 학습이 됨**"

### 블록 4. 차주 스프린트 문서 범위 — 최소 필요분만 빠르게 합의
- 당장 차주 스프린트에 **필요한 문서만** 골라 빠르게 합의
- 합의 후 즉시 분담 → 각자 작성하면서 서로 보완

---

## 5. 발화 톤 가이드 (보조강사용)

| 블록 | 톤 |
|---|---|
| 1·2 | "옵션 제시" 톤 — 팀 자율 합의 강조 |
| 3 | "권고" 톤 + **학습 단계 단서** 필수. **정원화 훈련생 직접 호명 회피** → "한 명에게 몰린 업무 구조" 일반론으로 시작 → 자연스럽게 분담 합의 유도 |
| 4 | "원칙 제시" 톤 — Just-enough doc |

**근거**: 5/14 R-out-2 회복 이후 정원화 케어 사이클이 안정화된 패턴 유지 필요. 직접 호명 시 5/16 1:1 *"본인 능력에 강박적 align"* 패턴 재발 위험.

---

## 6. 김태환 본인 의도 보존 사항

본 1:1에서 김태환이 **본인이 5/20 AM 미팅에서 직접 발화할 의제**로 정리한 것:
- 데모 시나리오 작성 분담 (한 명 전담 ❌)
- 정원화 문서 작업 분담
- 차주 스프린트 작업 의견 제출

→ **보조강사 의제와 중복**되는 부분 있음. 5/20 AM 회의 진행 시 **김태환 본인 발화 우선 보장** → 보조강사는 보강·근거 보완 톤으로 이어받기 권장.

---

## 7. 5/19 오전 1:1 (구체화 격차)과의 연결

오전 1:1 5번 의제(*"2차 개발 기간 미합의"*)와 본 1:1의 4번 블록(*"차주 스프린트 문서 범위 최소 합의"*)이 **동일 운영 사이클 의제**.

→ **강사진 사전 통지 의제 1순위**(2차 개발 기간 정의 + 일정 1.5배 보정)에 **본 1:1 결과 묶어서 일괄 처리 권장**.

---

## 8. 참조

- [taehwan_difficulties_briefing_260519.md](./taehwan_difficulties_briefing_260519.md) — 오전 1:1 (구체화 격차·학습 사이클·R-burn)
- [jaehoon_withdrawal_context_260518.md](./jaehoon_withdrawal_context_260518.md) — 5/18 정재훈 회복 사이클 (태환 멘토 역할)
- [snapshots/260519_am.md](../snapshots/260519_am.md), [snapshots/260519_pm.md](../snapshots/260519_pm.md)
- [docs/sprint_operation_guide_260519.md](../docs/sprint_operation_guide_260519.md)

### 외부 자료 출처

- [Sprint Anti-Patterns — Scrum.org](https://www.scrum.org/resources/blog/27-sprint-anti-patterns)
- [Sprint Anti-Patterns: 29 Examples — Age-of-Product](https://age-of-product.com/sprint-anti-patterns-2/)
- [Should your user stories fit into one sprint? — Agile Alliance](https://agilealliance.org/why-you-need-your-user-stories-to-fit-into-one-sprint/)
- [How to conduct an effective sprint demo — Atlassian](https://www.atlassian.com/agile/project-management/sprint-demo)
- [What Is a Sprint Demo — Scrum Alliance](https://resources.scrumalliance.org/Article/sprint-demo)
- [Top 10 UAT Best Practices for Sprint Teams — Quellit](https://www.quellit.ai/blog/top-10-uat-best-practices-for-sprint-teams)
- [How to Plan Testing Within a Sprint — TestCaseLab/Medium](https://medium.com/@case_lab/how-to-plan-testing-within-a-sprint-a-step-by-step-guide-eb30e371db60)
- [In Agile, do multiple people work on different tasks? — Quora](https://www.quora.com/In-Agile-do-multiple-people-work-on-different-tasks-within-one-user-story-or-are-user-stories-written-in-such-a-way-that-one-person-can-own-each-user-story)
- [Time-boxed Sprints vs Process Flow — Agile Sherpas](https://www.agilesherpas.com/blog/sprints-vs-process-flow)
- [애자일 실무 가이드(4): 스프린트 리뷰 — 오픈소스컨설팅](https://tech.osci.kr/%EC%95%A0%EC%9E%90%EC%9D%BC-%EC%8B%A4%EB%AC%B4-%EA%B0%80%EC%9D%B4%EB%93%9C4-%EC%8A%A4%ED%94%84%EB%A6%B0%ED%8A%B8-%EB%A6%AC%EB%B7%B0sprint-review/)
