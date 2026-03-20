# liontalk (Android) - Kotlin Chat App

이 프로젝트는 **Jetpack Compose + MVVM 아키텍처** 기반의 **Firebase(Firestore) 채팅 앱**입니다.

---
## 목표
- Compose 기반 화면 구성과 상태 관리 학습
- Feature-based 구조로 모듈/기능 단위 설계 학습
- Firestore 실시간 스트림으로 채팅 구현
- Firebase Auth로 로그인 흐름 구현
- Firebase Storage로 채팅 이미지 업로드 구현
- Hilt로 ViewModel/Repository 의존성 주입 구성

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
| DI | Hilt |
| 파일 업로드 | Firebase Storage |

---
## 프로젝트 구조
```
app/src/main/java/com/likelion/liontalk/
├── core/
│   ├── data/
│   │   ├── auth/      # 인증/세션 관련 Repository
│   │   ├── users/     # 사용자 프로필 Repository
│   │   └── storage/   # Firebase Storage Repository
│   ├── di/            # Hilt DI 모듈
│   ├── model/         # 공통 모델(ChatUser 등)
│   ├── navigation/    # Compose Navigation 라우팅
│   ├── ui/            # 공통 UI/theme
│   └── util/          # 유틸(예: 사운드 플레이어)
└── features/
    ├── auth/         # 로그인 UI/로직
    ├── launcher/     # 앱 시작 라우팅 결정
    ├── chat/         # 채팅방 목록/상세 및 메시지 흐름
    └── setting/      # 사용자 프로필 설정
```

---
## Hilt 적용 범위
- `LionTalkApplication` : `@HiltAndroidApp`
- `MainActivity` : `@AndroidEntryPoint`
- ViewModel
  - `LauncherViewModel`, `SignViewModel`, `ChatRoomListViewModel`, `ChatRoomViewModel`, `SettingViewModel`이 `@HiltViewModel` 사용
- DI Module
  - `features/auth/di/AuthHiltModule`
  - `core/di/FirebaseAuthHiltModule`
  - `features/chat/di/ChatHiltModule`

---
## 주요 라우팅
- `launcher` : `LauncherScreen`
- `sign` : `SignScreen`
- `chatroom_list` : `ChatRoomListScreen`
- `chatroom_detail/{roomId}` : `ChatRoomScreen`
- `setting` : `SettingScreen`


---
## 학습 포인트
- Compose 선언적 UI + Navigation 구성
- MVVM에서 ViewModel + Flow(StateFlow 포함)로 상태/이벤트 처리
- Firestore 실시간 수신(Flow/CallbackFlow)으로 UI 갱신
- Firebase Storage 업로드/다운로드 URL 활용
- Hilt를 통한 ViewModel/Repository 주입 구조화

