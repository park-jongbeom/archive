# likelion-non-di — Non-DI 샘플

**경로:** `archive/sample-project/likelion-non-di`

**Jetpack Compose + MVVM** 기반 **Firebase(Firestore) 채팅 앱**입니다.  
`likelion-manual-di`, `likelion-hilt-di`와 **기능은 같고**, Hilt·`AppContainer` 같은 **DI 컨테이너 없이** Repository를 Kotlin **`object`** 로 두는 변형입니다.

---
## 목표
- Compose 기반 화면 구성과 상태 관리 학습
- Feature-based 구조로 모듈/기능 단위 설계 학습
- Firestore 실시간 스트림으로 채팅 구현
- Firebase Auth로 로그인 흐름 구현
- Firebase Storage로 채팅 이미지 업로드 구현
- **DI 컨테이너 없이** 싱글톤 Repository + `viewModel()` / `ChatRoomViewModelFactory`(SavedState만)로 구성

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
| 의존성 구성 | **싱글톤 `object` Repository**, `AuthRepository.getInstance(Context)` |
| 파일 업로드 | Firebase Storage |

---
## 프로젝트 구조

**패키지·`applicationId`:** `com.likelion.liontalk` (다른 두 샘플과 동일). 한 기기에 여러 샘플을 동시에 설치하려면 각 프로젝트에서 `applicationIdSuffix` 등으로 구분하세요.

```
app/src/main/java/com/likelion/liontalk/
├── core/
│   ├── data/
│   │   ├── model/        # ChatUser
│   │   └── repository/   # UserRepository, StorageRepository (object)
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
## 의존성 구성 (DI 미사용)
- **`LionTalkApplication`** — `appContext` 노출, `onCreate`에서 `UserRepository.start()`로 Auth/Firestore 프로필 동기화 시작
- **Repository** — `UserRepository`, `StorageRepository`, 채팅용 Repository는 **`object`** / `AuthRepository`는 **`getInstance(Context)`**
- **ViewModel** — 대부분 기본 생성자 + 싱글톤 Repository 참조 / `ChatRoomViewModel`만 `SavedStateHandle` 때문에 **`ChatRoomViewModelFactory`**
- **화면** — `Launcher`·`Sign`·`Setting`·`ChatRoomList`: `viewModel(viewModelStoreOwner = backStackEntry)` / `ChatRoom`: `viewModel(..., factory = ChatRoomViewModelFactory(...))`

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
- **DI 프레임워크·컨테이너 없이** 싱글톤 Repository와 최소 Factory로 의존성 정리
