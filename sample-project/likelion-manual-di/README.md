# likelion-manual-di — 수동 DI 샘플

**경로:** `archive/sample-project/likelion-manual-di`

**Jetpack Compose + MVVM** 기반 **Firebase(Firestore) 채팅 앱**입니다.  
`likelion-non-di`, `likelion-hilt-di`와 **기능은 같고**, **`AppContainer` + `ViewModelFactory`** 로 의존성을 수동 조립합니다.

---
## 목표
- Compose 기반 화면 구성과 상태 관리 학습
- Feature-based 구조로 모듈/기능 단위 설계 학습
- Firestore 실시간 스트림으로 채팅 구현
- Firebase Auth로 로그인 흐름 구현
- Firebase Storage로 채팅 이미지 업로드 구현
- 수동 DI(AppContainer + ViewModelFactory)로 ViewModel/Repository 연결

---
## 기술 스택
| 영역 | 사용 기술 |
|---|---|
| 언어 | Kotlin |
| UI | Jetpack Compose |
| 구조 | MVVM + Feature-based |
| 상태관리 | ViewModel + StateFlow/Flow |
| 채팅 데이터 | Firebase Firestore |
| 인증 | Firebase Auth (Email/Google + Kakao/Naver 커스텀 토큰) |
| DI | 수동 DI (`AppContainer`, `ViewModelFactory`) |
| 파일 업로드 | Firebase Storage |

---
## 프로젝트 구조

**패키지·`applicationId`:** `com.likelion.liontalk` (다른 두 샘플과 동일). 한 기기에 여러 샘플을 동시에 설치하려면 각 프로젝트에서 `applicationIdSuffix` 등으로 구분하세요.

```
app/src/main/java/com/likelion/liontalk/
├── core/
│   ├── data/
│   │   ├── model/
│   │   └── repository/
│   ├── di/              # AppContainer, ViewModelFactory
│   ├── navigation/
│   ├── ui/
│   └── util/
└── features/
    ├── auth/
    ├── launcher/
    ├── chat/
    └── setting/
```

---
## 의존성 주입(DI) 적용 방식
- **`LionTalkApplication`** — `AppContainer` 생성 후 `UserRepository` 시작
- **`AppContainer` / `ViewModelFactory`** — ViewModel·Repository를 수동으로 생성·연결 / `ChatRoomViewModel`(`SavedStateHandle`)도 factory에서 동일 패턴
- **화면** — `viewModel(viewModelStoreOwner = …, factory = …)` 로 ViewModel 생성

---
## 주요 라우팅
| route | 화면 |
|-------|------|
| `launcher` | `LauncherScreen` |
| `sign` | `SignScreen` |
| `chatroom_list` | `ChatRoomListScreen` |
| `chatroom_detail/{roomId}` | `ChatRoomScreen` |
| `setting` | `SettingScreen` |

---
## 학습 포인트
- Compose + Navigation
- ViewModel + Flow(StateFlow)로 상태·이벤트
- Firestore 실시간 수신으로 UI 갱신
- Firebase Storage 업로드·다운로드 URL
- 수동 DI로 ViewModel/Repository 주입 구조 익히기
