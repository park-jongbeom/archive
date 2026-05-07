# [Feature] 홈 목록 UI + 상태(Loading/Empty) 뼈대

## User Story
As a 사용자,
I want 홈에서 목록을 확인하고 상태(로딩/빈)를 이해할 수 있다,
So that 콘텐츠가 없거나 로딩 중이어도 당황하지 않는다.

---

## Acceptance Criteria
- [ ] 홈 화면에 목록 영역이 있다.
- [ ] 로딩 상태 UI가 있다(Loading).
- [ ] 데이터가 0개일 때 빈 상태 안내와 다음 행동(CTA)이 있다(Empty).
- [ ] 목록 항목을 탭하면 상세로 이동 이벤트가 발생한다(실연동은 다른 Issue).

---

## Flow (링크)
- (FLOW-HOME) `example/02_flow/flow_home_list.md`

---

## How to Test
1. 홈 진입 → 로딩 UI 노출 확인
2. 데이터 0개 조건 → Empty 안내 + CTA 확인
3. 목록 항목 탭 → 상세 이동 이벤트 발생 확인(연동은 다른 Issue에서)

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p0

