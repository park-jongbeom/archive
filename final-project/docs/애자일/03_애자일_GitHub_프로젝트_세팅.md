# GitHub 프로젝트 세팅

> **메인 가이드:** `01_애자일_팀프로젝트_가이드.md`  
> 이 문서는 수강생이 그대로 따라 할 수 있는 **GitHub Projects(보드/필드/자동화) 세팅 가이드**입니다.

## 0) GitHub Projects는 어디에 있나요?

아래 중 **팀이 실제로 쓰는 위치 1곳**에서 프로젝트 보드를 만들면 됩니다.

- **(권장) Organization(팀) Projects**: Organization 페이지 → `Projects` 탭 → `New project`
- **Repository Projects**: Repo 페이지 → `Projects` → `New project`
- **개인 Projects**: 내 프로필 → `Projects` 탭 → `New project`

> 참고: UI가 바뀌어 버튼 위치가 조금 달라도, 핵심은 “**Projects로 들어가서 보드를 하나 만든다**”입니다.


## 1) 보드 생성

1. 위 `0)`에서 선택한 위치(Organization/Repo/개인)에서 → **Projects** → **New project**
2. **Board (Kanban, 칸반)** 템플릿 선택
3. 이름: `Sprint Board` (또는 팀명)

> 참고: `Board (Kanban)` 템플릿을 선택하면 **Status 필드와 기본 컬럼이 이미 만들어져 있는 경우가 많습니다.**  
> 이 문서의 목적은 “없으면 추가, 있으면 **이름/값이 팀 규칙과 같은지 확인**”입니다.

---

## 2) 필수 컬럼(Columns)

아래 5개만 쓰자 (과도하게 늘리지 마라):

```text
Backlog → Ready → In Progress → Review → Done
```

* **Backlog**: 아직 스프린트 미선정
* **Ready**: 이번 스프린트 대상(완료 기준(AC) 준비됨)
* **In Progress**: 작업 중(담당자 있음)
* **Review**: PR 생성/리뷰 중
* **Done**: 머지 완료

> 용어(빠른 정의)
> - **Ready**: 이번 주에 바로 시작할 수 있는 상태. (조건: 완료 기준(AC) 있음 + Flow 링크 + 주차(Iteration, Week) 지정)
> - **Done**: PR Merge + 완료 기준(AC) 충족.
> - **완료 기준(AC)**: Done 판정 체크리스트(관찰 가능/예-아니오).
---

## 3) 필수 필드(Fields)

GitHub Projects에서 “Fields”를 추가해 메타데이터를 관리한다.

### Fields는 어디서 추가/수정하나요?

- Projects 보드 상단에서 **Table view(표 보기)**로 전환(또는 추가)한다.
- 오른쪽 상단 메뉴(… 또는 설정/Customize)에 있는 **Fields**에서 추가/수정한다.
- 또는 Table view에서 컬럼 헤더 오른쪽의 `+`로 **새 필드 추가**를 할 수 있다.

> 중요한 원칙: 자동화(Workflows)는 결국 **Status/Priority/주차(Iteration, Week)** 같은 필드 값을 기준으로 동작합니다.  
> 그래서 “필드가 있는지”보다 “**필드 이름/값이 팀 규칙과 같은지**”가 더 중요합니다.

### Status (단일 선택)

* Backlog / Ready / In Progress / Review / Done
  - 컬럼과 동일하게 맞춰두면 자동화가 쉬움

---

### Priority (단일 선택)

```text
P0 (Must) / P1 / P2
```

---

### Size (단일 선택, 선택)

```text
S / M / L
```

- 추정이 필요할 때만

---

### 주차(Iteration) (Sprint)

* 1주 단위로 생성 (Week1, Week2…)

> 용어
> - **주차(Iteration, Week)/스프린트(Sprint)**: 1주 단위 실행 리듬/필드. “이번 주 할 일”을 Ready로 올릴 때 Week를 함께 지정.
---

### Assignee (기본)

* 담당자

---

## 4) 라벨(Labels) 규칙

Repo → Issues → Labels에서 최소만 정의:

```text
type: feature / bug
priority: p0 / p1 / p2
```

참고: 라벨과 Priority 필드 둘 다 써도 되지만
**하나만 쓰고 싶으면 Priority 필드만 써라**

> 과정 기본(권장): **Priority 필드만 사용**한다. (라벨은 팀이 따로 합의했을 때만 추가)

---

## 5) 자동화(Workflows) 설정

Projects → **Workflows**에서 설정

### Workflows(자동화)는 어디서 설정하나요?

- Projects 보드 화면에서 **Workflows**(또는 `…` 메뉴 안의 Automations/Workflows)로 들어간다.
- 아래 규칙을 “Trigger(언제) → Action(무엇을)” 형태로 추가한다.

> 참고: UI 표기가 영어/한글로 달라도, 핵심은 “**이벤트가 발생하면 Status를 바꾼다**”입니다.

## ① Issue 생성 시 Backlog로

* Trigger(언제): Item added to project (프로젝트에 아이템 추가)
* Action(무엇을): Set Status → Backlog (Status를 Backlog로 변경)

---

## ② Assignee 지정 시 In Progress

* Trigger(언제): Assignee added (담당자 지정)
* Action(무엇을): Set Status → In Progress (Status를 In Progress로 변경)

참고: “담당자 = 작업 시작” 규칙

---

## ③ PR 연결 시 Review

* Trigger(언제): Pull request opened (linked issue) (연결된 PR 생성)
* Action(무엇을): Set Status → Review (Status를 Review로 변경)

참고: Issue에 PR 연결 필수 (`Closes #123`)

---

## ④ PR Merge 시 Done

* Trigger(언제): Pull request merged (PR 머지)
* Action(무엇을): Set Status → Done (Status를 Done으로 변경)

---

## ⑤ Status 변경 시 컬럼 이동 (옵션)

* Status 변경 → 해당 컬럼으로 자동 이동

참고: 수동 드래그 줄이기

---

## 6) 스프린트(Iteration) 운영

## Week 시작 시

* Backlog → Ready 이동
* 주차(Iteration) 필드에 Week 지정

---

## 진행 중

* Ready → In Progress (Assignee 붙이면 자동)
* PR 생성 → Review 자동 이동

---

## 종료 시

* Done 확인
* 미완료 → 다음 주차(Iteration)으로 이동

---

## 7) 실제 운영 흐름

```text
Issue 생성 → Backlog
→ (스프린트 선정) → Ready
→ (담당자 지정) → In Progress
→ (PR 생성) → Review
→ (Merge) → Done
```

참고: 이 흐름만 지켜도 스크럼 돌아간다

---

## 8) 보드 뷰(View) 추천

## 기본 Board View

* 상태별 칸반

---

## Table View (관리용)

* Priority / 주차(Iteration) / Assignee 정렬

---

## Filter 예시

* `주차(Iteration) = Week2`
* `Assignee = 나`

---

## 9) 운영 규칙 (중요)

### In Progress 제한

* 1인당 1~2개

---

### Ready 기준

* 완료 기준(AC) 없으면 Ready 금지

> 용어
> - **Ready**: 착수 가능 상태. AC가 없으면 Ready로 올리지 않는다.
---

### Done 기준

* PR Merge + 완료 기준(AC) 충족

> 용어
> - **Done**: PR이 머지되고, AC를 만족한 상태.
---

### Review 오래 두지 말기

* 병목 지점

> 규칙(권장): PR이 올라오면 **24시간 내 1차 피드백**(승인/수정 요청/질문)까지는 반드시 남긴다.

---

## 10) 가장 흔한 실패

- 컬럼 많음 (복잡)
- 자동화 없음 (수동 지옥)
- Issue 내용 빈약 (실행 불가)
- PR 연결 안 함

---

## 11) 한 줄 정리

> **Status + 자동화 + PR 연결 = GitHub 스크럼 완성**

---

원하면
- “자동화 설정 스크린 기준으로 단계별 클릭 가이드”
- “주차(Iteration) 자동 생성 방법”
- “팀 인원별 운영 규칙 (리뷰 속도, 동시 작업 제한(WIP) 등)”

여기까지 붙이면 그냥 바로 팀에 적용 가능하다.
