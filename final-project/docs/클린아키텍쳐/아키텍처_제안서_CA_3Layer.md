# FinalProject 아키텍처 제안서 (Clean Architecture 3-Layer) — 배포용

대상: 안드로이드 부트캠프 FinalProject 팀 공통  
목표: “폴더만 CA”가 아니라, **의존성 방향/경계/테스트 용이성**을 실제로 지키기

---

## 0. TL;DR (이 10줄만 지켜도 적용됩니다)

1. 의존성은 항상 **Presentation/Data → Domain** 방향으로만 향한다.
2. `domain`은 Android/Retrofit/Room/Firebase 같은 프레임워크 타입에 **의존하지 않는다**.
3. **DTO / Domain Entity / UI Model(UiState)** 는 섞지 않는다.
4. 매핑(mapper)은 **경계에서만** 한다. (DTO↔Domain: data, Domain↔UI: presentation)
5. Repository **interface는 domain**, 구현(implementation)은 **data**에 둔다.
6. Presentation은 data 구현체를 직접 참조하지 말고, **interface를 통해서만** 접근한다.
7. 개발 순서는 기본적으로 **Domain(계약) → Data(구현) → Presentation(상태/UI)** 이다.
8. 첫 목표는 “완벽한 구조”가 아니라, **3~5일 안에 데모 가능한 흐름 1개**를 만든다.
9. 화면은 성공만 만들지 말고, 최소 **로딩/실패**까지 같이 잡는다.
10. PR 리뷰 때는 “금지 패턴” 체크(6장)으로 빠르게 걸러낸다.

---

## 1. 이 문서를 왜 쓰나요?

Clean Architecture를 쓰는 이유는 “계층을 많이 만들기”가 아니라 아래 3가지입니다.

- **의존성 방향을 고정**해서 변경에 강하게 만들기
- DTO/UI 상태 같은 “구현 디테일”이 도메인 규칙을 오염시키지 않게 **경계 유지**
- 핵심 규칙을 테스트하기 쉽게 만들기

---

## 2. 3-Layer 한 장 요약

```text
presentation  ───────────────►  domain  ◄──────────────  data
  (UI/VM)                          (규칙)                  (구현)

presentation → domain (UseCase/Entity를 호출)
data → domain (Repository interface를 구현)
domain → nothing (외부 의존 없음)
```

---

## 3. 레이어별 규칙(해야 할 일 / 금지 / 체크)

### 3-1. Presentation (UI)

**해야 할 일(책임)**
- ViewModel에서 domain을 호출해, 결과를 **UiState(상태)** 로 만든다.
- UI는 UiState만 보고 그린다. (정책/비즈니스 로직은 UI에서 하지 않는다)
- feature마다 최소 상태를 고려한다: `Loading / Success / Error` (+ 필요 시 `Empty`)

**금지(하면 안 됨)**
- DTO를 ViewModel/UiState/Compose에서 그대로 쓰기
- Compose에서 “정책/규칙 분기”를 처리하기

**체크(완료 기준)**
- 성공/실패/로딩이 최소 포함된 “데모 흐름 1개”가 동작한다.

### 3-2. Domain (핵심 규칙)

**해야 할 일(책임)**
- Entity(도메인 모델), UseCase(규칙/조합), Repository interface를 둔다.
- “무엇이 옳은가(규칙)”를 코드로 표현한다.

**금지(하면 안 됨)**
- Android/네트워크/DB 프레임워크 타입에 의존하기
- Entity에 UI 상태(예: `isLoading`) 섞기
- Repository interface를 “DB 테이블 CRUD” 모양으로만 만들기

**체크(완료 기준)**
- `domain`이 Android/Retrofit/Room/Firebase 타입에 **전혀 의존하지 않는다**.

### 3-3. Data (구현)

**해야 할 일(책임)**
- Repository 구현체(implementation), Remote/Local datasource, DTO, mapper를 둔다.
- 외부 시스템(서버/Firebase/DB) 연동과 캐시/재시도/에러 매핑 같은 “구현 디테일”을 처리한다.

**금지(하면 안 됨)**
- domain interface 없이, presentation이 data 구현체에 직접 연결되게 만들기

**체크(완료 기준)**
- 외부 응답 스키마가 바뀌어도(필드 추가/이름 변경) Domain이 흔들리지 않는다.

---

## 4. 권장 패키지 구조 (Best Practice Structure)

단일 모듈 내에서 폴더를 통해 레이어를 나누되, **DI 설정까지 각 레이어 내부로 응집**시키는 것이 포인트입니다.

```text
app/src/main/java/com/team/project/
  ├── domain/                (비즈니스 핵심: 프레임워크 의존성 0%)
  │   ├── model/             # 순수 Kotlin 엔티티
  │   ├── repository/        # Repository 인터페이스 (계약)
  │   ├── usecase/           # 비즈니스 로직 단위 (캡슐화)
  │   └── di/                # UseCase 관련 DI 설정 (UseCaseModule)
  │
  ├── data/                  (구현 디테일: 외부 SDK/DB 연동)
  │   ├── datasource/        # 원격/로컬 데이터 소스 (Interface & Impl 분리)
  │   │   ├── XxxRemoteDataSource.kt
  │   │   └── XxxRemoteDataSourceImpl.kt
  │   ├── repository/        # Repository 구현체 (Domain 인터페이스 구현)
  │   ├── model/             # DTO (서버/DB 스키마 맞춤형)
  │   ├── mapper/            # DTO <-> Domain 변환기
  │   └── di/                # 데이터 관련 DI (Repository/DataSource/FirebaseModule)
  │
  ├── presentation/          (UI 및 상태 관리: 사용자 접점)
  │   └── feature_name/      # 기능 단위 패키징
  │       ├── ui/            # Compose Screen, Components
  │       ├── viewmodel/     # ViewModel (UseCase 호출 및 UiState 생성)
  │       └── model/         # UiState, UiEvent 정의
  │
  └── core/                  (공통 모듈: 프로젝트 전역 공통 코드)
      ├── navigation/        # 네비게이션 그래프 및 라우팅
      └── ui/                # 디자인 시스템 및 테마
```

---

## 5. 적용 순서(첫 데모 1개 만들기 중심)

목표: **3~5일 안에 “첫 데모 흐름 1개”** 완성

### Step 0) 킥오프(반나절~1일)
- 도메인(문제) 단위로 feature를 나눈다. (화면 수 기준 분할 금지)
- 핵심 사용자 시나리오 1~2개를 문장으로 고정한다.
- 핵심 Entity 후보 3~7개, Repository 후보를 적는다.

### Step 1) Domain 먼저 만든다(계약 고정)
**최소 산출물(시나리오 1개 기준)**: Entity 1~2개 + Repo interface 1개 (+ 필요 시 UseCase 1개)

### Step 2) Data 구현을 붙인다(외부 연동)
- datasource(remote/local) 구현
- DTO ↔ Domain 매핑(mapper)
- Repository 구현체가 domain interface를 구현

### Step 3) Presentation에서 상태(UiState)를 만든다
- ViewModel: UseCase 호출 → UiState 변환
- Screen: UiState를 그리는 UI
- 최소: 로딩/실패 상태까지 포함

### Step 4) 스프린트 단위로 반복 확장
- 기능이 커지면 “화면 추가”가 아니라 **시나리오 단위**로 쪼개고 같은 순서로 반복

---

## 6. PR 리뷰용 체크리스트(자주 터지는 실수 방지)

### 6-1. UseCase 규칙
- “모든 기능에 UseCase 1개”는 금지
  - UseCase는 규칙/조합 로직이 있을 때만 도입한다.
  - 도입 트리거: 정책/검증 로직, 여러 Repo 조합, 재사용, 테스트 필요

### 6-2. Repository 경계
- interface는 domain, implementation은 data
- presentation은 interface로만 접근(구현 타입 직접 참조 금지)

### 6-3. 상태/에러 처리
- UiState에 `Loading/Success/Error`는 기본으로 포함(필요 시 Empty)
- 외부 에러는 data에서 공통 에러 모델로 매핑하고, presentation에서 UI 메시지로 변환한다.

### 6-4. 금지 패턴(바로 탈락)
- ViewModel/Compose에서 DTO를 그대로 노출
- UI가 `data`의 구현 타입(예: `RepositoryImpl`, `RemoteDataSource`)을 직접 참조
- `domain`이 Retrofit/Firebase/Room/AndroidX 타입에 의존
- 폴더는 3레이어인데, 실제 의존성이 presentation → data로 직결되고 domain이 비어 있음

---

## 7. 팀별 Tech Spec에 남길 것(최소 체크)

- 레이어별 책임 정의(우리 팀 기준 3줄)
- 주요 도메인 Entity 목록(최소)
- Repository interface 목록 + 구현 위치
- DTO ↔ Domain ↔ UiState 매핑 위치
- 에러 모델 표준 + 재시도 정책(기본)
- 코루틴/Flow 사용 기준(스레드/취소/수명)

---

## 부록 A. 팀 샘플(선택)

이 섹션은 **리포지토리에 샘플 파일이 함께 있을 때만** 참고하세요.
(단독 배포 파일만 따로 복사해 간 경우, 아래 경로가 없을 수 있습니다.)

- Plant: `docs/FinalProject/docs/templates/13_아키텍처_샘플_Plant.md`
- JoopJoop: `docs/FinalProject/docs/templates/14_아키텍처_샘플_JoopJoop.md`
- Tasty: `docs/FinalProject/docs/templates/15_아키텍처_샘플_Tasty.md`

