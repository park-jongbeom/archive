# GitHub Flow, 브랜치·이슈·마일스톤 for AOS 6기

**GitHub Flow**는 `main` 하나를 배포 가능한 기준으로 두고, 모든 작업을 **짧은 생명주기의 기능 브랜치**에서 하고 **Pull Request(PR)** 로 검토·병합하는 단순한 협업 방식입니다. **Issues**로 할 일을 쪼개고, **Milestones**으로 중간·최종 목표와 기한을 묶어 진행 상황을 볼 수 있습니다. 소규모 팀에서도 도입하기 쉽고, Android 프로젝트와도 잘 맞습니다.

이 문서는 **① 할 일을 이슈·마일스톤으로 정리** → **② 브랜치에서 구현** → **③ Pull Request로 리뷰 후 `main`에 합치는** 순서로 읽으면 흐름이 이어집니다.

---

## 1. GitHub Flow 한눈에

| 구분 | 설명 |
|------|------|
| 기본 브랜치 | `main` — 항상 **배포·제출 가능한 상태**를 목표로 유지 |
| 작업 브랜치 | 이슈·기능 단위로 `main`에서 분기 → 작업 → PR → `main`에 병합 |
| 병합 | PR 승인 후 `main`에 merge (일반적으로 **Squash merge** 또는 **Merge commit** — 팀 규칙에 따름) |
| 배포 | `main`이 곧 최신 제품선. 필요 시 태그·릴리즈로 버전 표시 |

**Git Flow**(long-lived `develop` 등)와 달리 브랜치가 단순해 충돌·동기화 부담이 적습니다.

---

## 2. GitHub Issues (이슈)

**이슈**는 “무엇을 할지”를 저장소에 기록하는 작업 카드입니다. 여기서 작업 단위를 나눈 뒤, **브랜치를 만들고 Pull Request를 열 때** 브랜치명·PR 본문과 **이슈 번호**로 연결합니다.

| 항목 | 권장 |
|------|------|
| 단위 | 한 이슈는 **한 작업 단위**(한 화면, 한 API 연동, 한 버그)에 가깝게 |
| 제목 | 나중에 검색될 만한 동사형·명사형 (`로그인 화면 UI 구현`, `프로필 조회 API 연동`) |
| 본문 | 배경, **완료 조건**(체크리스트), 스크린샷·참고 링크(기능 정의서 섹션 등) |
| 담당 | **Assignees**로 담당자 지정. 여러 명이면 이슈를 쪼개거나 서브태스크로 나누기 |
| 라벨 | `bug`, `enhancement`, `documentation` 등 팀에서 정한 **Labels**로 구분 |

**이슈 템플릿(선택)**: 버그 리포트·기능 요청 형식을 통일하고 싶으면 저장소 **`.github/ISSUE_TEMPLATE/`** 에 템플릿을 둔다([GitHub 문서](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/configuring-issue-templates-for-your-repository)).

**중복·방치 방지**

- 같은 내용 이슈는 검색 후 **중복 닫기**
- 블로커는 본문에 명시하고, 관련 이슈를 링크(`#12`)

---

## 3. Milestones (마일스톤)

**마일스톤**은 앞에서 만든 **GitHub Issues**들을 **하나의 목표·기한** 아래 묶는 단위입니다. 중간 발표, 스프린트 종료, “MVP 완료”처럼 팀이 정한 구간에 맞춥니다.

| 항목 | 설명 |
|------|------|
| 생성 | 저장소 **Issues → Milestones → New milestone** |
| 이름 | 예: `Midterm 데모`, `Sprint 2`, `v1.0 제출` |
| Due date | 제출·데모 날짜 등 **기한**을 넣으면 일정 관리에 유리 |
| 이슈 배정 | 각 이슈 오른쪽 **Milestone** 드롭다운에서 선택 |

**활용**

- 마일스톤 페이지에서 **열린 이슈 / 닫힌 이슈** 비율로 진행도 확인
- 범위가 커지면 마일스톤을 나누거나, 이슈를 재배정해 **한 마일스톤당 현실적인 양** 유지
- 팀 규칙 예: “이 마일스톤에 속한 이슈만 이번 스프린트에서 `main`에 합친다” 등

---

## 4. 권장 워크플로 (일반)

1. **이슈·마일스톤 정리(권장)**  
   새 작업이면 위 **GitHub Issues** 절의 권장안에 맞게 이슈를 만들고, 필요하면 **Milestones** 절에서 말한 대로 마일스톤·담당자를 맞춘다.
2. **최신 `main` 받기**  
   `git checkout main` → `git pull origin main`
3. **기능 브랜치 생성**  
   `git checkout -b feature/이슈번호-짧은설명` (아래 **브랜치 네이밍 예시**에 맞출 것)
4. **작은 단위로 커밋**  
   커밋 메시지는 무엇을/왜 바꿨는지 한 줄에 읽히게 작성
5. **원격 푸시**  
   `git push -u origin feature/...`
6. **PR 생성**  
   베이스: `main` ← 비교: 작업 브랜치. 아래 **Pull Request 관습**에 맞게 이슈 연결·설명을 채운다.
7. **리뷰·수정**  
   리뷰 코멘트 반영 후 추가 커밋 또는 amend(팀 규칙에 따름)
8. **승인 후 병합**  
   CI(있다면) 통과 후 merge. 로컬에서 `main`을 다시 pull하여 다음 작업 시작

이슈 없이 바로 브랜치만 쓰는 것도 가능하지만, 팀에서는 **이슈 번호 ↔ 브랜치 ↔ PR**을 맞춰 두면 추적이 쉬워진다.

---

## 5. 브랜치 네이밍 예시

**권장 워크플로**에서 기능 브랜치를 만들 때 그대로 쓰기 쉽도록, 팀에서 접두사를 하나로 통일하면 검색·자동화에 유리합니다.

| 접두사 | 용도 | 예시 |
|--------|------|------|
| `feature/` | 새 기능·화면 | `feature/12-login-ui` |
| `fix/` | 버그 수정 | `fix/45-crash-on-back` |
| `docs/` | 문서만 변경 | `docs/update-setup-guide` |
| `chore/` | 빌드·설정·잡일 | `chore/bump-compile-sdk` |

이슈 번호를 붙이면 PR·마일스톤과 추적이 쉬워집니다.

---

## 6. Pull Request 관습

| 항목 | 권장 |
|------|------|
| 단위 | 한 PR은 **한 목적**(한 이슈·한 기능)에 가깝게 유지 |
| 설명 | 변경 요약, 스크린샷(UI), 테스트 방법 |
| 리뷰 | 최소 1인 이상(팀 규칙). 본인 PR은 직접 merge 금지 등 규칙이 있으면 준수 |
| 충돌 | `main`이 앞서 갔으면 PR 전 또는 리뷰 중 `main`을 브랜치에 맞춘다. **merge**(`main`을 브랜치에 merge)가 역사 보존에 무난하고, **rebase**는 선형 기록을 선호할 때(팀 합의 시에만) 사용한다. |

**이슈와 연결**

- PR 본문에 `Closes #23` 또는 `Fixes #23`을 쓰거나 Development로 연결하면, **기본 브랜치**(보통 `main`)로 merge될 때 이슈가 자동으로 닫히는 경우가 많다([공식 안내](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue))
- PR 오른쪽 **Development**에서 이슈를 연결해도 됨
- 브랜치명에 이슈 번호를 넣으면(위 **브랜치 네이밍 예시** 참고) PR 목록에서도 대응 관계가 보기 쉽다

---

## 7. PR 템플릿·커밋 메시지 (선택)

항목을 많이 적을수록 부담이 커지므로, **팀이 부담 없이 지킬 수 있는 최소만** 두는 것을 권장한다.

### Pull Request 템플릿

**저장소 루트** 아래 `.github` 폴더에 `pull_request_template.md`를 두면(경로: `.github/pull_request_template.md`), 새 PR을 열 때 본문에 자동으로 채워진다. (다른 경로·여러 템플릿은 [공식 문서](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/creating-a-pull-request-template-for-your-repository) 참고)

- 리뷰에 필요한 것만 넣는다: **무엇을 했는지**, **이슈 번호**, UI면 **스크린샷** 정도면 충분한 경우가 많다.
- 아래는 **샘플**이다. 팀 저장소에 복사해 쓰거나, 항목을 줄여도 된다. 같은 내용의 파일: [templates/pull_request_template.md](./templates/pull_request_template.md).

```markdown
## 요약

<!-- 이 PR에서 한 일을 한두 문장으로 -->

## 관련 이슈

<!-- 예: Closes #12 -->

## 확인 방법 (선택)

<!-- 리뷰어가 재현할 수 있게: 화면 경로, 빌드 타입 등 -->

## 스크린샷 / 녹화 (UI 변경 시)

<!-- 없으면 "해당 없음" -->

## 빌드·테스트 (선택)

<!-- 예: `./gradlew assembleDebug` 성공, 수동 시나리오: 로그인 → 홈 -->

## 기타

<!-- 리뷰 시 특히 봐줬으면 하는 점, DB/보안 규칙·스키마 변경 여부 등 -->

---

*이 자료는 멋쟁이사자처럼 부트캠프 학습 목적으로 제공됩니다.*
```

### 커밋 메시지

- **템플릿 없이** 한 줄만 쓰는 팀도 많다.  
  예: `feat: 로그인 버튼에 Auth 연동`, `fix: 프로필 로딩 시 NPE 방지`
- 접두어를 맞추면 로그가 읽기 쉽다. (관례: [**Conventional Commits**](https://www.conventionalcommits.org/) 요약 — `feat`, `fix`, `docs`, `chore`, `refactor` 등)

**커밋 템플릿 파일**

- 편집기에 제목·본문 안내가 미리 열리게 하려면, Git의 **commit.template** 설정에 텍스트 파일을 지정한다.
- 샘플(제목 줄 + 안내 주석): [templates/git_commit_template.txt](./templates/git_commit_template.txt)  
  팀 저장소 루트에 복사할 때 관례적으로 **`.gitmessage`** 같은 이름을 쓰기도 한다.

**설정 방법**

| 구분 | 명령 | 설명 |
|------|------|------|
| 이 저장소만 | `git config --local commit.template .gitmessage` | 프로젝트 루트에 `.gitmessage`를 두었다고 가정 |
| 모든 저장소(본인 PC) | `git config --global commit.template ~/.gitmessage` | 홈 디렉터리 등 고정 경로에 파일을 두고 경로를 맞출 것 |
| 절대 경로 | `git config --local commit.template /절대/경로/.gitmessage` | 위치가 길 때 |

- **적용 확인**: `git config --get commit.template` (어느 파일이 적용됐는지는 `git config --show-origin --get commit.template`)
- **해제(로컬)**: `git config --local --unset commit.template`
- **IDE**: Android Studio 등은 버전·설정에 따라 커밋 UI가 `commit.template`을 쓰지 않을 수 있다. 템플릿 동작은 터미널에서 `git commit`으로 확인하는 것이 가장 확실하다.

**템플릿 안에서 `#` 줄**

- 기본 설정에서는 커밋 저장 시 **`#`으로 시작하는 줄이 메시지에서 제거**된다(`commit.cleanup` 등에 따라 달라질 수 있음). 안내 문구는 `#`으로 시작하게 두면 된다.
- 제목·본문은 **`#`이 아닌 줄**에만 적어야 커밋에 포함된다.

---

## 8. `main` 보호 (저장소 설정)

운영진·팀 리드는 GitHub **Settings → Branches → Branch protection rules**에서 예를 들어 다음을 검토한다.

- `main`에 **직접 push 금지**, **PR 필수**
- (선택) PR 승인 1개 이상, **대화 해결** 후 merge
- (선택) **Status checks** 통과 필수

프로젝트 성격에 따라 위 규칙은 완화해도 된다.

---

## 9. 자주 하는 실수

| 상황 | 피하기 |
|------|--------|
| 오래 붙잡은 브랜치 | 기능을 쪼개 PR을 자주 열고, `main`과 자주 맞추기 |
| 거대 PR | 리뷰가 어렵고 충돌이 커짐 → **GitHub Issues**에서 작업을 잘게 나누고, **권장 워크플로**대로 작은 PR 여러 개로 올리기 |
| `main`에 비밀·로컬 파일 커밋 | `.gitignore`·PR 전 diff 확인 |
| 이슈·마일스톤 없이만 진행 | 나중에 “무슨 작업이 왜 들어갔는지” 추적이 어려움 → **GitHub Issues**와 **Milestones** 쓰기를 습관화 |

---

## 10. 참고

- [GitHub Flow (공식 블로그)](https://github.blog/2011-08-15-git-workflows-for-pros/)  
- [About GitHub Flow (GitHub Docs)](https://docs.github.com/en/get-started/using-github/github-flow)  
- [About milestones (GitHub Docs)](https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/about-milestones)  
- [Linking a pull request to an issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue)  
- [Git — `commit.template`](https://git-scm.com/docs/git-config#Documentation/git-config.txt-committemplate)

---

**유의**: GitHub·Git·Android Studio UI는 버전에 따라 메뉴 이름·경로가 다를 수 있다. 막히면 이 문서 맨 아래 **참고** 절의 공식 문서 링크를 함께 확인한다.

**작성일** 2026-03-20

---

*이 자료는 멋쟁이사자처럼 부트캠프 학습 목적으로 제공됩니다.*
