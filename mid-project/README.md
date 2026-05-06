# AOS-6 Archive

> 멋쟁이사자처럼 안드로이드 부트캠프 6기 자료 모음입니다.  
> 강의 노트·가이드 문서와 PR/커밋 템플릿 샘플을 두었습니다.

---

## 📌 개요

이 폴더는 6기 수업·팀 과제에서 쓰는 문서를 한곳에 모아 둔 아카이브입니다.

---

## 📂 구성

| 구분 | 설명 |
|------|------|
| 문서 | 강의 노트, 가이드, 실습 정리 |
| 템플릿 | PR·커밋 메시지 샘플(`templates/`) — 팀 저장소 `.github` 등에 복사 |

### 문서 목록

- [AOS6-Kotlin-Project-Guide.pdf](./AOS6-Kotlin-Project-Guide.pdf) — Kotlin 프로젝트 가이드
- [개발-셋업-가이드.md](./개발-셋업-가이드.md) — Studio·Gradle·클론·빌드, Firebase 최소 연동, 부록(`local.properties`·`.gitignore`·BuildConfig)
- [개발-가이드.md](./개발-가이드.md) — 아키텍처·Firebase 제품·백엔드·트러블슈팅·체크리스트
- [코딩-컨벤션.md](./코딩-컨벤션.md) — Compose·Coroutine 포함 코드 스타일, PR 전에 볼 체크리스트
- [팀-협업-가이드.md](./팀-협업-가이드.md) — 5명 기준 F1~F5 나누기, 이슈 쪼개기, Repository·모델 누가 먼저
- [github-flow-브랜치-전략.md](./github-flow-브랜치-전략.md) — 브랜치·PR·이슈·마일스톤, 템플릿 경로 안내
- [Firestore-설계가이드.md](./Firestore-설계가이드.md) — Firestore로 SNS/커뮤니티 짤 때 컬렉션·비정규화
- [Supabase-설계가이드.md](./Supabase-설계가이드.md) — Supabase(PostgreSQL)로 같은 도메인 짤 때 테이블·RLS

과제마다 Firestore만 쓸지 Supabase만 쓸지 갈린다. 위 두 가이드는 서로 짝을 이뤄 놓은 참고용이니, 팀에서 메인으로 정한 쪽을 기준으로 보고 나머지는 필요할 때만 보면 된다.

템플릿 (복사해서 쓰기)

- [templates/pull_request_template.md](./templates/pull_request_template.md)
- [templates/git_commit_template.txt](./templates/git_commit_template.txt) — `git config commit.template`용

*(추가 문서는 저장소 업데이트에 따라 반영됩니다.)*

`archive` 안에 `.git`이 같이 있으면, 상위 프로젝트와 별도 저장소로 클론해 둔 경우일 수 있다. 문서만 두는 폴더라면 중첩 `.git` 여부만 한번 확인해 두면 된다.

---

## 🚀 활용 방법

1. 문서: 처음이면 개발 셋업 → 개발 가이드 → 코딩 컨벤션. 팀 과제면 분배 초안·GitHub Flow를 이어서 본다.
2. 템플릿: 팀 앱 repo에 `templates/` 내용을 복사해 `.github/pull_request_template.md` 등으로 맞춘다.
3. 질문·피드백: 운영진·사자처럼 채널로 올린다.

---

## 👤 작성 및 관리

- 작성자: 김갑석  
- 프로젝트: 멋쟁이사자처럼 안드로이드 부트캠프 6기 (AOS-6)

---

## 공유·재사용 시

- 가이드는 교육·팀 내부 참고용이다. Gradle·Firebase·GitHub는 공식 문서를 같이 본다.
- 외부 배포 시 작성자·프로젝트 표기와 소속 정책을 따른다.

---

*이 자료는 멋쟁이사자처럼 부트캠프 학습 목적으로 제공됩니다.*
