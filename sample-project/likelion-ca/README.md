# Likelion-CA: Clean Architecture (3-Layer) Gold Standard Sample

본 프로젝트는 안드로이드 부트캠프 수강생들을 위해 **클린 아키텍처(Clean Architecture)**의 핵심 원칙을 단일 모듈 환경에서 가장 직관적으로 구현한 '골드 스탠다드' 샘플 앱입니다.

## 🏗️ 아키텍처 설계 원칙

본 프로젝트는 **Presentation - Domain - Data**의 3계층 구조를 따르며, 의존성은 항상 **Domain(중심)**을 향합니다.

### 1. 계층별 역할 및 구성
| 계층 | 역할 | 주요 구성 요소 |
|------|------|----------------|
| **Domain** | 순수 비즈니스 로직 및 규칙 | Entity, Repository Interface, UseCase, Result/Error 모델 |
| **Data** | 데이터 획득 및 변환 (외계와의 통신) | Repository Implementation, DataSource, DTO, Mapper |
| **Presentation** | 사용자 인터페이스 및 상태 관리 | UI (Compose), ViewModel, UiState |

### 2. 핵심 설계 포인트
- **Pure Domain:** Domain 레이어는 Android 프레임워크나 외부 라이브러리(Firebase 등)에 의존하지 않는 순수 Kotlin 코드로만 구성됩니다.
- **UseCase Driven:** 모든 비즈니스 액션은 UseCase로 캡슐화되어 ViewModel의 비대화를 방지하고 비즈니스 로직의 재사용성을 높입니다.
- **Strict Separation:** `datasource` 및 `repository`는 인터페이스와 구현체를 물리적으로 분리하여 결합도를 낮추고 테스트 용이성을 확보했습니다.
- **Layered DI:** DI 설정을 각 레이어(`domain/di`, `data/di`) 내부에 배치하여 레이어별 응집도를 극대화했습니다.

---

## 📂 패키지 구조 (Package Structure)

```text
com.likelion.ca
├── domain (비즈니스 중심부)
│   ├── model/       # 도메인 엔티티 (ChatRoom, ChatMessage 등)
│   ├── repository/  # Repository 인터페이스 (계약)
│   ├── usecase/     # 비즈니스 로직 단위 (LoginUseCase, SendMessageUseCase 등)
│   └── di/          # UseCase 주입 설정 (UseCaseModule)
├── data (데이터 구현부)
│   ├── datasource/  # 원격/로컬 데이터 소스 (Interface & Impl 분리)
│   ├── repository/  # Repository 구현체 (Domain 인터페이스 구현)
│   ├── model/       # DTO (Data Transfer Object)
│   ├── mapper/      # DTO <-> Entity 변환기
│   └── di/          # 데이터 관련 DI (FirebaseModule, DataSourceModule 등)
├── presentation (UI/표현부)
│   ├── auth/        # 인증(로그인) 기능
│   ├── chat/        # 채팅 목록 및 상세 채팅방 기능
│   ├── launcher/    # 앱 시작 및 라우팅 결정
│   └── setting/     # 사용자 설정 기능
└── core (공통 모듈)
    ├── navigation/  # 앱 전역 네비게이션 그래프
    └── ui/          # 공통 UI 컴포넌트 및 테마
```

---

## 🚀 주요 기능 및 UseCase 활용

본 앱은 다음과 같은 실무급 비즈니스 로직을 UseCase를 통해 완벽하게 처리합니다.

- **인증:** `LoginUseCase`(소셜/이메일 통합), `LogoutUseCase`
- **채팅방:** `GetChatRoomsUseCase`(방 목록 필터링), `EnterChatRoomUseCase`(권한 검증)
- **메시징:** `SendMessageUseCase`(텍스트/이미지), `GetMessagesUseCase`(실시간 스트리밍)
- **이벤트:** `ListenRoomEventsUseCase`(입퇴장/타이핑 실시간 감지), `SendRoomEventUseCase`
- **관리:** `KickUserUseCase`(방장 전용 추방 로직), `LeaveRoomUseCase`

---

## 🛠️ 기술 스택 (Tech Stack)

- **UI:** Jetpack Compose (Modern Declarative UI)
- **DI:** Hilt (Dependency Injection)
- **Asynchronous:** Coroutines & Flow (Reactive Programming)
- **Backend:** Firebase (Auth, Firestore, Storage, Functions)
- **Auth SDK:** Kakao & Naver Social Login

---

## 📖 학습 포인트

1. **의존성 역전 원칙(DIP):** Data 레이어가 Domain 레이어의 인터페이스를 구현함으로써 의존성 방향을 역전시키는 방법을 학습합니다.
2. **상태 관리:** `UiState` 패턴을 사용하여 단방향 데이터 흐름(UDF)을 구현하는 방법을 익힙니다.
3. **관심사 분리:** UI 로직, 비즈니스 로직, 데이터 로직이 각각 어디에 위치해야 하는지 명확한 가이드를 제공합니다.

---
**제작자:** 천재 개발자 코다리 (Powered by Clean Architecture)
