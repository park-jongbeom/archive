# 6주 일정(Iteration) — 1팀

기능 정의서의 **모든 기능 ID**를 Week1~Week6에 배치했다. 실행 시 GitHub Issue에 동일한 **주차(Iteration, Week)** 를 붙인다.

**Week6 원칙**: `01_애자일_팀프로젝트_가이드.md`의 **Freeze** — 새 기능 추가는 최소화하고, QA·회귀·데모·README 우선.

| 주차 | 초점 | 포함 기능 ID |
| --- | --- | --- |
| **Week1** | Foundation & Kickoff — 인증·최소 홈·데모 1개 | `F-Auth-01`, `F-Auth-02`, `F-Auth-03`, `F-Home-01` |
| **Week2** | FLOW-STUDY 핵심 — 화분·공부 세션 성공 경로 | `F-Home-02`, `F-Home-03`, `F-Study-01`, `F-Study-03` |
| **Week3** | FLOW-STUDY 보완 — 결과·함께공부·계획 상세·로그아웃·추적표 정리 | `F-Study-04`, `F-Study-02`, `F-StudyPlanDetails-01`, `F-Auth-05`, `F-Study-05` |
| **Week4** | FLOW-COMMUNITY 핵심 — 목록·작성·상세·댓글·통합 상세 | `F-community-01`, `F-Community-03`, `F-community-04`, `F-Community-05`, `F-Community-Detail-Screen` |
| **Week5** | FLOW-COMMUNITY·학습계획·마이 — 검색·편집·계획 CRUD·공유·프로필 | `F-Community-02`, `F-Community-06`, `F-StudyPlanDetails-03`, `F-StudyPlanDetails-04`, `F-StudyPlanDetails-05`, `F-StudyPlanDetails-06`, `F-StudyPlanDetails-07`, `F-Mypage-01` |
| **Week6** | Final / Freeze — 인증 부가·마이·아카이브·테마·기른나무 메인 | `F-Auth-04`, `F-Auth-06`, `F-Mypage-02`, `F-Home-Screen`, `F-MypageSetting-01`, `F-Mypage-archive-01` |

## 주차별 데모 권장

- **Week1**: 로그인(또는 가입) → 홈 최소 표시.
- **Week2**: 화분 생성·선택 → 공부 시작~종료 다이얼로그까지(기록 저장 전까지도 가능).
- **Week3**: Week2 + 결과·이미지 저장 + 계획 상세 1화면 + 로그아웃.
- **Week4**: 커뮤니티 목록·작성·상세·댓글.
- **Week5**: Week4 + 검색 또는 편집 + 계획 공유·프로필.
- **Week6**: 전체 회귀 + 스토어/제출물(팀 정책).

## 맵핑 검증

- 명세 상단 **기능 목록 표의 ID**는 모두 위 6주 표에 포함된다. (`F-Study-05`는 하단 추적표 항목 — `06_decision_log.md` D-003 병합)
