# 3팀 Figma 보드 자료 (BBip)

> **팀명**: BBipit
> **앱명**: **BBip**
> **GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip
> **방법론**: **워터폴** (메모리: [team3_methodology.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md))
> **최초 수집**: 2026-05-13 (Week 2 마감 시점)

> **3팀은 2개 Figma 파일 운영** — 워터폴 단계 분리 구조

| 파일 | 타입 | 단계 | fileKey |
|---|---|---|---|
| **BBip 브레인스토밍** | FigJam | 요구분석·탐색 | `X1hPf6U7hRuUGadqBoAr77` |
| **BBip UI** | Design | 설계 | `gjhxpOboz1famP1hlLVbjq` |

---

## 1. 프로젝트 컨텍스트 (Figma에서 추론)

| 항목 | 추론 |
|---|---|
| 앱 명 | **BBip** (또는 그 변형) |
| 앱 종류 | **친구 위치 공유 + 워치 연동 앱** (Pulse 디자인 레퍼런스 + 지도 + 친구 관리 페이지) |
| 핵심 기능 | 메인페이지/친구관리/메시지/위치 공유 + Watch 페이스 (5종) |
| 컬러 톤 | **보라색 계열** (#7900CE 등) |
| 워치 디자인 | 5개 워치 화면 명시 — WearOS/Watch OS 연동 |

> 1팀 (카페인) / 2팀 (영어 학습) 과 완전히 다른 도메인.

---

## 2. 폴더 구조

```
teams-docs/.shared/.figma_token   ← 공유 토큰 (1/2/3팀 공통)

teams-docs/3team/figma/
├── README.md                       ← 본 문서
├── raw/                            ← API 원본 JSON
│   ├── brainstorm_depth2.json      ← FigJam 보드 메타
│   ├── brainstorm_urls.json        ← FigJam PNG URL
│   ├── ui_depth2.json              ← Design 파일 메타
│   └── ui_urls.json                ← Design PNG URL
└── screens/                        ← PNG (파일별 prefix 사용)
    ├── brainstorm_references_YYMMDD.png    ← FigJam: 레퍼런스
    ├── brainstorm_skeleton_YYMMDD.png      ← FigJam: 뼈대 잡기
    ├── ui_screens_YYMMDD.png               ← Design: 화면 모음
    └── ui_db_YYMMDD.png                    ← Design: DB 스키마
```

> 파일이 2개이므로 PNG 파일명에 `brainstorm_` / `ui_` prefix 구분.

---

## 3. 보드 1 — BBip 브레인스토밍 (FigJam)

- **URL**: https://www.figma.com/board/X1hPf6U7hRuUGadqBoAr77/멋사-최프-3조-BBip-브레인스토밍
- **lastModified**: 2026-05-12 07:06 UTC (어제 마지막 수정)
- **Page 1 자식**: 166개 (대부분 작은 메모/그리기)
- **핵심 SECTION 2개**:

| 섹션명 | Node ID | 크기 | 내용 |
|---|---|---|---|
| **레퍼런스** | 24:2 | 6176×18224 (매우 큼) | 마인드맵, Pulse 디자인 참고, 친구 관리 UI 레퍼런스 |
| **뼈대 잡기** | 28:10 | 5968×3808 | 와이어 스케치, 메인페이지/친구관리 mockup |

### 워터폴 단계 매핑

| 단계 | 보드 영역 | 상태 |
|---|---|---|
| **요구분석/탐색** | 레퍼런스, 뼈대 잡기 | 🟢 진행 |
| 설계 | (별도 파일 — BBip UI로 이전) | 🟢 진행 |

---

## 4. 보드 2 — BBip UI (Design)

- **URL**: https://www.figma.com/design/gjhxpOboz1famP1hlLVbjq/멋사-최프-3조-BBip-UI
- **lastModified**: 2026-05-13 05:26 UTC (**오늘 갱신**, 활성도 높음)
- **권한**: **editor** (브레인스토밍은 viewer — 권한 차이 주의)
- **Page 1 자식**: 57개
- **핵심 SECTION 2개**:

| 섹션명 | Node ID | 크기 | 내용 |
|---|---|---|---|
| **화면** | 59:437 | 5764×4257 | 회원가입/로그인/메인/친구추가/알림 + Watch 화면 5종 |
| **DB** | 59:438 | 5764×4153 | DB 스키마 다이어그램 |

### 발견된 화면 (PNG에서 vision 확인)

- BBip Color Guide (#7900CE 보라)
- 회원가입 / 이메일 인증 / 로그인
- 메인 / 친구 추가 / 알림 설정
- **Watch UI 5종** — 지도 / 친구 디스크(여러 화면) / 워치 화면

→ **WearOS 워치 연동 요건을 명확히 충족**. 부트캠프 6기 워치 필수 요건 OK.

### 워터폴 단계 매핑

| 단계 | 보드 영역 | 상태 |
|---|---|---|
| **설계** | 화면, DB | 🟢 진행 |
| 구현 | (코드 저장소 — 등록 필요) | 미확인 |

---

## 5. 3팀 작업 패턴 (점검 시 핵심)

### 5-1. 워터폴 단계 분리 운영

브레인스토밍 ↔ UI 파일 분리는 **워터폴 방법론의 단계 격리 원칙**과 정합. 1팀(애자일, 단일 보드에서 모든 작업) 과 본질적으로 다른 패턴.

### 5-2. UI 파일 활성도가 핵심 지표

브레인스토밍 보드는 탐색 단계라 변동 적음(05-12 이후 정체 가능). **UI 파일의 lastModified가 일일 활성도 지표**.

### 5-3. 명세 완성도 강조 (워터폴)

⚠️ 메모리 노트: **3팀은 애자일이 아닌 워터폴 방법론**. 점검 시 다음 톤 유지:
- MVP 다이어트 권고 ❌
- 스프린트 권고 ❌
- mock-first 권고 ❌
- **명세서 완성도 / 화면 충실도 / DB 스키마 정합성** 강조 ✅
- **단계 종료 명확성** (요구분석 → 설계 → 구현 전환 신호) ✅

---

## 6. 매일 자동 수집 명령 (2개 파일)

```bash
cd /c/Users/ibebu/bootcamp6_final/archive
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
DATE=$(date +%y%m%d)

# 1. 브레인스토밍 보드
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/X1hPf6U7hRuUGadqBoAr77?depth=2" \
  -o teams-docs/3team/figma/raw/brainstorm_${DATE}.json

curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/images/X1hPf6U7hRuUGadqBoAr77?ids=24:2,28:10&format=png&scale=1"

# 2. UI Design 파일 (활성도 높음 — 디핑 우선순위)
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/gjhxpOboz1famP1hlLVbjq?depth=2" \
  -o teams-docs/3team/figma/raw/ui_${DATE}.json

curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/images/gjhxpOboz1famP1hlLVbjq?ids=59:437,59:438&format=png&scale=1"
```

### 디핑 포인트

- 두 파일의 `lastModified` 각각
- "화면" 섹션 자식 수 (신규 화면 추가 추적)
- "DB" 섹션 변동 (스키마 변경 추적)
- "뼈대 잡기"에서 "화면"으로의 이전 흐름 (탐색 → 설계 단계 전환 신호)

---

## 7. 일일 체크 통합 항목 (3팀 특화)

- [ ] UI 파일 `lastModified` 24h 이내 갱신 있었나
- [ ] 브레인스토밍 파일 정체 길어지지 않았나 (탐색 단계 종료 신호)
- [ ] "화면" 섹션에 신규 화면 추가됐나
- [ ] "DB" 스키마 변동 (필드 추가/삭제/타입 변경)
- [ ] Watch UI 추가/수정 있나 (워치 필수 요건)
- [ ] 브레인스토밍에서 미반영 아이디어 누적 (탐색→설계 전환 누락)

---

## 8. BBip 저장소 베이스라인 스캔 결과 (2026-05-13 기준)

GitHub 저장소 (https://github.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip) 스캔:

### 8-1. 활동 요약

| 지표 | 값 | 평가 (워터폴 컨텍스트) |
|---|---|---|
| 기본 브랜치 | `develop` (Git Flow) | ✅ |
| 총 브랜치 | 4개 (main, develop, feature/Dm, feature/auth) | 🟡 1팀(11)/2팀(11) 대비 적음 |
| 총 커밋 | 15개 (develop 기준) | 🟡 **워터폴 — 설계에 시간 투자, 구현 늦게 시작** |
| 최근 커밋 | 2026-05-13 09:09 (오늘) | ✅ 활성 |
| 총 PR | 1개 머지 (#1 Main) | 🟡 코드 초기 단계 |
| 총 Issue | **10개 (open 6 / closed 4)** | 🟢 **닫힘률 40% — 1팀(0%) 대비 우수, 모범** |

### 8-2. 모듈 구조 (멀티 모듈 + Firebase Functions)

```
FinalProject-BBipit-BBip/
├── app/                    ← 메인 Android 앱
├── bbipit/                 ← 별도 모듈 (워치? 라이브러리?)
├── functions/              ← Firebase Cloud Functions
├── .firebaserc + firebase.json  ← Firebase 프로젝트 설정 (안전, secret 아님)
└── settings.gradle.kts
```

→ **1팀(app + wear) / 2팀(app)보다 복잡한 멀티 모듈 구성**. Firebase Functions까지 직접 활용. 점검 시 모듈 간 의존성·책임 분리도 봐야 함.

### 8-3. 기여자 매핑 (이메일 기반)

| GitHub | 이메일 | 추정 인물 | 커밋 수 |
|---|---|---|---|
| `zzing` | wkdwldms2001@naver.com | (동일인 추정) | 13 |
| `장지은` | 134998025+zzingenius@users.noreply.github.com | zzingenius (장지은) | 2 |

→ 같은 사람일 가능성 매우 높음 (zzing = zzingenius = 장지은). **실제로는 1인 단독 커밋 상태**.

### 8-4. Issue 작성자 vs 커밋 기여자 차이 ⚠️

Issue 작성자: `zzingenius`, `useok2481-stack`, `yyukong`, `Theo-Brie` (4명 이상)
커밋 기여자: `zzing`/`장지은` (1명)

→ **4명 이상이 Issue로 기여 의사를 표명했지만, 실제 커밋은 1명만 하고 있는 상태.**
→ 워터폴 초기엔 정상일 수 있으나, **Week 3 진입 이후에도 지속되면 R8(단일 인물 과부하) 격상**.

### 8-5. 보안 / 위생 점검 (R13 / R14)

| 항목 | 결과 |
|---|---|
| `google-services.json` 커밋 | ✅ 없음 |
| keystore / jks / p12 / pem | ✅ 없음 |
| `.env` / `local.properties` | ✅ 없음 |
| IDE 아티팩트 (.idea/, .vscode/, .DS_Store) | ✅ 없음 |
| API 키 평문 grep | ✅ 없음 |
| `.gitignore` 존재 | ✅ 4개 (root + app + bbipit + functions, 모듈별 분리 잘됨) |
| `.firebaserc` / `firebase.json` 커밋 | ⚪ 정상 (project ID 등 설정, secret 아님) |

→ **R13/R14 모두 깨끗**. 1팀·2팀과 다르게 보안·위생 관리 모범.

### 8-6. Issue 명명 컨벤션 (모범)

이슈 제목이 일관됨:
- `[feature/voice] 무전 수신 기능 개발`
- `[feature/voice] 무전 송신 기능 개발`
- `[feature/auth] 이메일 인증 기반 회원가입 UI 구성`
- `[feature/Dm] DM 목록 화면 UI 구성`
- `[Feature/noti] 알림 목록 화면 UI 구성`

→ 1팀(`[Issue A-N]`)과는 다른 `[feature/X]` 패턴이지만 일관성 양호.

### 8-7. 발견된 앱 핵심 기능 (Issue 제목 기반)

| 기능 | Issue | 상태 |
|---|---|---|
| 무전 송신 (voice) | #8 | open |
| 무전 수신 (voice) | #9 | open |
| 이메일 인증 회원가입 | #6 | open |
| DM 목록 | #5 | open |
| 알림 목록 | #4 | open |
| 로그인 화면 UI | #10 | open |

→ **"무전" 키워드로 미루어 앱의 핵심 인터랙션은 음성 무전(워키토키)**. Figma의 친구 위치 공유 + Watch UI와 결합하면 **친구 그룹 음성 무전 + 위치 공유 앱**으로 추정.

---

## 9. 향후 필요 자료

- ✅ ~~3팀 GitHub URL~~ — 입수됨
- ⬜ **3팀 PRD/명세서** — [teams-docs/3team/prd/](../prd/) 폴더 활용 (워터폴이라 명세 완성도 우선)
- ⬜ **3팀 멤버 정보** (장지은 외 useok2481/yyukong/Theo-Brie 실명)
- ⬜ 워터폴 단계 일정표 — 각 단계 종료 시점 정의
- ⬜ Week 1 검토 작성 여부 (1팀 [brief_review_w1.md](../../1team/review/brief_review_w1.md) 와 같은 산출물이 3팀에도 필요한지 — 단, **워터폴 톤**으로)

---

## 10. 참조

- [공유 가이드](../../.shared/README.md)
- [1팀 figma README (애자일 예시)](../../1team/figma/README.md)
- [2팀 figma README (Design 파일 예시)](../../2team/figma/README.md)
- [3팀 prd](../prd/)
- [3팀 mom](../mom/)
- **3팀 GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip
- 3팀 워터폴 메모: 메모리 시스템 ([team3_methodology.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md))
