# likelion-ca — Clean Architecture (3-Layer) 샘플

**경로:** `archive/sample-project/likelion-ca`

`likelion-hilt-di`와 **동일한 기능**(Compose + Firebase 채팅·인증)을 유지하면서, **멀티모듈 클린 아키텍처**로 나눈 버전입니다.

---

## 모듈·의존성 방향

| 모듈 | 역할 | 의존 |
|------|------|------|
| **domain** | 엔티티, Repository **인터페이스**, UseCase (순수 Kotlin, Android/Firebase 없음) | 없음 |
| **data** | Firebase·SDK, Repository **구현**, Hilt `@Binds` | `domain` |
| **app** | UI, ViewModel, Navigation, `CaApplication` | `domain`, `data` |

Presentation → Domain 계약만 바라보고, Data는 Domain 인터페이스를 구현합니다.

---

## 패키지 개요

- `com.likelion.ca.domain` — `model`, `repository`, `usecase`
- `com.likelion.ca.data` — `repository/*Impl`, `di` (FirebaseAuth, Repository 바인딩)
- `com.likelion.ca` — `MainActivity`, `CaApplication`, `di/UseCaseModule`
- `com.likelion.ca.core` — 테마, 네비게이션, 공통 UI 이벤트
- `com.likelion.ca.features.*` — 화면·ViewModel (기능 단위)

---

## 원본 대비 주요 변경

- **구글 로그인:** `Intent` 파싱은 `SignViewModel`(presentation)에서 하고, domain `AuthRepository`에는 `signInWithGoogleIdToken(idToken)`만 노출합니다.
- **시작 화면 결정:** `ComputeStartDestinationUseCase` + `AppStartDestination` → `RouteMapper.toRoute()`로 Compose 라우트 문자열 매핑.
- **세션:** `UserRepository`는 `FirebaseUser` 대신 `AuthSession`을 `StateFlow`로 노출합니다.
- **Storage:** `uploadChatImage`는 domain에서 `localImageUri` 문자열(content 스킴)로 받고, data에서 `Uri.parse` 합니다.

---

## 빌드 전 준비

1. **`local.properties`** — `sdk.dir` 설정 (Android Studio에서 열면 자동 생성).
2. **`app/google-services.json`** — Firebase 콘솔에서 내려받아 `app/`에 배치 (저장소에는 포함하지 않음, `.gitignore` 처리).

원본 `likelion-hilt-di`에 쓰던 Firebase 프로젝트를 그대로 쓰려면, 패키지/앱 ID가 **`com.likelion.ca`**로 바뀌었으므로 Firebase Android 앱 설정을 맞추거나 `applicationId`를 원래 값으로 되돌려야 할 수 있습니다.

---

## 기술 스택

원본과 동일: Jetpack Compose, MVVM, Hilt, Firebase Auth / Firestore / Functions / Storage, 카카오·네이버 SDK(커스텀 토큰).

---

## 참고 문서

워크스페이스의 `archive/final-project/docs/클린아키텍쳐/아키텍처_제안서_CA_3Layer.md`와 같은 규칙(의존성 방향, DTO/도메인/UI 분리)을 따르도록 구성했습니다.
