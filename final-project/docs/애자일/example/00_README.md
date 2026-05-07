# example/ 사용법 (수강생용)

이 폴더는 `01~04` 애자일 문서에서 쓰는 **산출물 샘플**을 모아 둔 곳입니다.

## 어떤 순서로 보면 되나요?

1. `01_product_brief.md` 샘플을 읽습니다.
2. `02_flow/`에서 Flow(지도) 샘플을 읽습니다.
3. `03_issue/flow_*/`에서 해당 Flow를 쪼갠 Issue(백로그 카드) 샘플을 봅니다.
4. `04_pr/flow_*/`에서 PR 샘플을 봅니다.
5. `05_demo/`에서 데모 시나리오 샘플을 봅니다.
6. (선택) `06_decision_log.md`, `07_retrospective.md`, `08_tech_overview.md`를 복사해 Notion 문서로 씁니다.

## 규칙(중요)

- **Issue가 SSOT**입니다. Flow/Brief/Demo는 “설명용”이며 실행 기준은 Issue+AC입니다.
- **Ready(이번 주 대상)**는 `AC + Flow 링크 + Iteration(Week)`가 있어야 합니다. (UI 포함 Issue는 `Design(Figma) 링크`도 필요)
- **Done**은 `PR Merge + AC 충족`으로만 판정합니다.

## PR 샘플에 대해

- 이 폴더는 **권장안(= Issue 1개당 PR 1개)** 기준으로 샘플을 제공합니다.
- 참고용으로 “통합 PR(비권장)” 샘플은 `04_pr/_deprecated/`에 별도 보관합니다.
- 버그 픽스도 동일하게 **Issue(재현/AC) → PR(Closes) → Merge → Done** 흐름으로 운영합니다. 샘플은 `03_issue/bugfix/`, `04_pr/bugfix/` 참고.

### 예외: PR 1개에 Issue 여러 개를 묶어도 되는 경우

> 원칙은 1:1이지만, 아래 조건을 모두 만족하면 **N:1(여러 Issue → 1 PR)** 을 허용합니다.

- **같은 Flow**에 속한 작은 Issue들이다.
- 합쳐도 PR이 **하루 내 리뷰 가능한 크기**다.
- PR 본문에 `Closes #123`, `Closes #124`처럼 **모든 Issue를 명시적으로 연결/종료**한다.

### 금지: 이런 묶음은 피한다

- 서로 다른 Flow를 한 PR에 섞기
- “새 기능”과 “완성도(로딩/에러/빈/재시도)”를 한 PR에 섞어서 리뷰가 어려워지는 경우

## 샘플 Flow 목록

- Flow: 로그인(`FLOW-LOGIN`)
- Flow: 로그아웃(`FLOW-LOGOUT`)
- Flow: 회원 탈퇴(`FLOW-WITHDRAW`)
- Flow: 홈 목록(`FLOW-HOME`)
- Flow: 상세 화면(`FLOW-DETAIL`)

