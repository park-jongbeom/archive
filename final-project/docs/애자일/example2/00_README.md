# example2/ 사용법 (중간 프로젝트 기반 샘플)

이 폴더는 **멀티캠퍼스 중간 프로젝트**의 팀별 기능 정의서를 바탕으로, 파이널 애자일 가이드(`01_애자일_팀프로젝트_가이드.md`)에 맞춘 **산출물 예시**를 팀별로 묶어 둔 곳입니다.

## `example/` 과의 차이

| 구분 | `example/` | `example2/` |
| --- | --- | --- |
| 출처 | 가상 앱 “오늘의 기록” | 실제 팀 명세: `docs/MidProject/1팀`, `2팀`, `3팀` |
| 구조 | Flow·Issue·PR·Demo 풀세트 | Brief·Flow·Backlog·Demo·Decision·Retro + **Issue 샘플만** (최소) |
| PR 샘플 | `04_pr/` 제공 | **생략** — PR 패턴은 [`../example/04_pr/`](../example/04_pr/) 참고 |

## 읽는 순서 (팀 폴더 기준)

1. `01_product_brief.md` — 제품 한 장 요약
2. `04_six_week_iteration.md` — **기능 정의서 전 기능 ID**를 Week1~Week6에 배치한 일정표
3. `03_backlog.md` — 기능 ID 전체 + **주차(Week)** + 우선순위(명세 표 기반)
4. `02_flow/` — 핵심 사용자 플로우 2개 (A/B)
5. `03_issue/` — GitHub Issue **샘플** (실행 SSOT은 실제 저장소 Issue; Issue에도 동일 Week 지정)
6. `05_demo/` — 주간 데모 시나리오
7. (선택) `06_decision_log.md`, `07_retrospective.md`

## 규칙(중요)

- **실행 기준(SSOT)**: GitHub **Issue** + 완료 기준(AC). Flow/Brief/Backlog는 설명·계획용이다.
- **Done**: PR Merge + AC 충족 (`01_애자일_팀프로젝트_가이드.md`와 동일).
- **Ready**: AC + Flow 링크 + 주차(Iteration). UI 포함 이슈는 Design(Figma) 링크.

## 팀 폴더

- [`1팀/`](1팀/) — 학습·화분·공부·커뮤니티 (기능 정의서 기준 Flow 재구성)
- [`2팀/`](2팀/) — JoopJoop (지도·쪽지)
- [`3팀/`](3팀/) — Tasty MVP (피드 + 지도 식당 탐색을 Flow B로 설정)
