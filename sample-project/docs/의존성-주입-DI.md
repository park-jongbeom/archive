# 의존성 주입(DI, Dependency Injection)

**Kotlin**과 **Jetpack Compose**로 UI를 구성하는 안드로이드 앱을 기준으로, **의존성 주입(DI)**의 정의, 이유, 시기, 구현 방식, 장단점을 정리합니다. **용어·코드로 맥락을 잡은 뒤 익숙한 예로 다시 짚습니다.** (XML·View 시스템·Fragment 전용 설명은 다루지 않습니다.)  
용어 사전·FAQ·**본문과 대응하는 실제 코드 예시(저장소 내 세 샘플 앱, Compose UI)**는 **하단 부록**에 두었습니다. (이 파일 위치: `archive/sample-project/docs/`.)

---

## 읽기 전

**도움이 되면 좋은 것:** Kotlin 클래스·생성자, `@Composable` 함수로 화면을 그리는 경험, ViewModel이 **구성 변경(화면 회전 등) 후에도 같은 화면 상태를 이어 줄 때 쓰는 클래스**라는 정도의 이해입니다.

모르는 용어는 **부록: 용어**를 참고하면 됩니다.

---

## 1. 의존성(Dependency)이란?

**한 클래스가 동작하기 위해 필요로 하는 다른 객체**를 그 클래스의 **의존성**이라고 부릅니다.

예를 들어 `UserViewModel`이 사용자 정보를 가져오려면 `UserRepository`가 필요합니다. 이때 ViewModel은 Repository에 **의존**한다고 말합니다.

| 내 클래스 | 흔한 의존성 예 |
|-----------|----------------|
| `LoginViewModel` | `AuthRepository` |
| `ChatViewModel` | `MessageRepository`, `UserRepository` |
| `AuthRepository` | Retrofit `ApiService`, `DataStore` |

의존성은 클래스들이 **서로 필요로 하는 관계**일 뿐이고, 그다음 **누가 객체를 만들고 몇 개를 재사용할지**를 정리하다 보면 DI 이야기로 이어집니다.

### Kotlin에서 “안에서 직접 생성”

```kotlin
// 필드 초기화로 인스턴스를 직접 만드는 모습
class UserViewModel : ViewModel() {
    private val repository = UserRepository()
}
```

이 문서에서는 위와 같이 **클래스 안에서 직접 만드는 경우**를 **안에서 직접 생성**이라고 부릅니다.

---

## 2. 의존성 주입(DI)이란?

**필요한 객체를 클래스 안에서 직접 만들지 않고, 밖에서 만들어서 “주입”해 주는 방식**입니다.

```kotlin
// Repository는 밖에서 만들어 생성자로 넘김 = 의존성 주입
class UserViewModel(
    private val repository: UserRepository
) : ViewModel()
```

| 용어 | 의미 |
|------|------|
| **의존성** | 내가 쓰는 다른 객체(서비스, Repository, API 클라이언트 등) |
| **주입** | 그 객체를 내가 만들지 않고, 생성자·함수 인자 등으로 **받는 것** |
| **DI** | 위 과정을 일관되게 적용하는 설계·패턴(그리고 이를 돕는 프레임워크) |

> **핵심 한 줄:** “내가 쓸 객체를 **내가 직접 만들지 않고**, **받는다**.”

---

## 3. 안에서 만들기 vs 밖에서 받기

### 3.1 안에서 만들기

```kotlin
class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    fun loadUser() {
        val user = repository.getCurrentUser()
    }
}
```

**짧은 과제에서는** 한 파일에서 흐름이 보이고, 팩토리 없이 빠르게 쓸 수 있습니다.

**앱이 커지면** Repository 안에서 Retrofit·Context 등이 연쇄 생성되기 쉽고, 테스트에서 가짜로 바꾸려면 클래스를 뜯어야 하며, `UserRepository` 생성자가 바뀌면 호출부가 여럿 생길 수 있습니다.

### 3.2 밖에서 받기 (생성자 주입)

```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    fun loadUser() {
        val user = repository.getCurrentUser()
    }
}
```

**밖**은 예를 들어 `Application`의 컨테이너, **ViewModelFactory**, **Hilt**가 만든 팩토리입니다. ViewModel 파일에는 **무엇이 필요한지**만 두고, **어떻게 조립하는지**는 한곳에 모읍니다.

---

## 4. 익숙한 예로 다시 보기

앞의 **의존성**, **안에서 직접 생성**, **밖에서 받기(주입)**를 일상에 대응해 보면 다음과 같습니다.

### 4.1 부품 공장 vs 조립을 맡김 (3절과 대응)

**카메라 앱**을 예로 들면, 코드 안에서 `PhotoStorage()`처럼 저장소를 직접 만드는 것은 **3.1 안에서 만들기**와 같습니다. 저장 **사용**뿐 아니라 저장소 **생성**까지 앱이 맡습니다.  
반대로 `PhotoStorage`에 필요한 동작만 정해 두고 구현체는 밖에서 넣는 것은 **3.2 밖에서 받기**와 같습니다. 카메라 기능에 집중하고, 조립은 팩토리·Hilt 등이 맞춥니다.

**한 줄:** 사진 찍기·보여주기에 집중하고, 저장소를 앱 안에서 끝까지 “공장”처럼 돌리지 않는 쪽이 DI에 가깝습니다.

### 4.2 콘센트와 어댑터 (구체 구현을 바깥에 맡김)

기기는 “전원을 받을 수 있으면 된다”만 알고, 콘센트 규격은 어댑터가 맞춥니다. ViewModel이 “`UserRepository`에서 사용자를 가져올 수 있으면 된다”만 알고, Retrofit / Room 등 **실제 구현**은 바깥에서 정하는 그림과 같습니다.

### 4.3 레스토랑 (생성자 파라미터)

- **직접 생성:** 손님이 주방에 들어가 재료·조리까지 함 → **3.1**에 가깝습니다.  
- **DI:** 메뉴만 주문하고 요리는 주방에서 준비해 받음 → **3.2**에 가깝습니다.

코드에서는 **생성자 파라미터**가 그 “주문서”에 해당합니다.

---

## 5. 왜 DI를 쓰나요?

### 5.1 직접 생성의 문제

1. **테스트** — 실제 네트워크·DB를 쓰는 `UserRepository`를 고정하면 단위 테스트에서 **Mock/Fake로 바꾸기** 어렵습니다.  
2. **변경 영향** — `UserRepository` 생성 방식이 바뀌면(예: 인자 추가) 그걸 쓰는 **모든 클래스를 수정**해야 할 수 있습니다.  
3. **역할 혼잡** — 화면 상태만 맡으면 될 ViewModel이 Repository **생성 방법**까지 알게 되어 단일 책임이 흐려집니다.  
4. **수명·범위** — 앱 전체 하나만 있어야 할 것과 화면마다 새로 있어야 할 것을 **한곳에서 정책으로 관리**하기 어렵습니다.

### 5.2 DI를 쓰면 좋아지는 점

- **구현 교체** — 인터페이스에 의존하고, 테스트는 Fake, 실서비스는 실제 구현을 주입합니다.  
- **의존 관계 가시화** — 생성자만 보면 이 클래스가 무엇을 필요로 하는지 드러납니다.  
- **생성 규칙 한곳에** — Hilt, Koin, 수동 DI의 `Application` / `Factory` 등에서 **누가 어떤 스코프로 생성되는지**를 모읍니다.

---

## 6. 언제 DI를 쓰나요?

| 상황 | 이유 |
|------|------|
| **Repository, API 클라이언트, DB** 등 여러 화면(Composable)·ViewModel이 공유 | 중복·불일치를 막고 수명(싱글톤 vs 화면 단위)을 통일하기 좋음 |
| **단위·UI 테스트** | Mock/Fake 주입으로 격리된 테스트가 가능 |
| **구현 교체**가 잦을 때(개발용 스텁 ↔ 실서버) | 주입만 바꾸면 됨 |
| **팀 프로젝트** | 의존 관계가 명확해져 리뷰·온보딩에 유리 |

**DI 프레임워크는 필수는 아닙니다.** 작은 과제·프로토타입에서는 생성자 주입만으로도 충분한 경우가 많고, 의존성이 한두 개면 Hilt까지 도입하지 않아도 됩니다.

**Kotlin·Compose 앱에서 자주 보는 시점:** ViewModel·Repository·Retrofit·Room이 얽이기 시작할 때, 테스트 코드를 본격적으로 쓸 때, 모듈을 나누거나 여러 팀이 같은 앱을 만질 때입니다.

---

## 7. Jetpack Compose에서 DI가 자주 나오는 이유

구성 변경(화면 회전 등)으로 **호스트 `Activity`가 새로 만들어져도**, 같은 화면의 데이터는 **ViewModel에 남기고 싶은** 경우가 많습니다. ViewModel은 시스템이 **생명주기에 맞게 한 인스턴스를 유지**해 주므로, Composable 안에서 매번 `UserViewModel()`을 **새로 만들어 쓰는 것**과는 맞지 않습니다. 보통은 **`viewModel()`**(Hilt면 **`hiltViewModel()`**)처럼 **프레임워크가 정한 방식**으로 가져옵니다.

그런데 ViewModel에 **생성자 인자**(예: `UserRepository`)가 있으면, 시스템은 그 인자를 어떻게 만들지 **모릅니다**. 그때 **`viewModel(factory = …)`**에 넘기는 **ViewModelFactory**(또는 Hilt가 만든 팩토리)가 Repository 등을 **넘겨 주며** 인스턴스를 만듭니다. “ViewModel을 어떻게 만들까?”와 “의존성을 어디서 넣을까?”가 **같은 문제**로 이어지므로, Compose 앱에서도 DI 이야기가 자주 붙습니다.

---

## 8. Mock / Fake

**단위 테스트**는 특정 클래스만 떼어 두고, 빠르고 예측 가능하게 로직을 검사하는 것입니다.

Repository가 실제 서버를 호출하면 네트워크·속도·환경에 테스트가 흔들립니다. 그래서 **미리 정한 응답만 하는 가짜 Repository**를 넣고 싶습니다. 생성자 주입이면 실제 앱에서는 실제 구현, 테스트에서는 Fake만 바꾸면 되고 ViewModel 코드는 그대로입니다. 안에서 `UserRepository()`를 고정하면 가짜로 바꾸기가 어렵습니다.

---

## 9. 인터페이스는 꼭 필요한가요?

DI는 **구체 클래스**만 받아도 됩니다.

```kotlin
class UserViewModel(
    private val repository: NetworkUserRepository
) : ViewModel()
```

구현을 자주 바꿀 때는 **인터페이스**로 경계를 두는 경우가 많습니다. 아래 이름은 위와 구분되도록 했습니다.

```kotlin
interface UserRepository {
    suspend fun getCurrentUser(): User
}

class UserViewModel(
    private val repository: UserRepository
) : ViewModel()
```

과제 규모에 **인터페이스를 전부** 둘 필요는 없고, 테스트·교체가 필요한 지점부터 두면 됩니다.

---

## 10. DI를 구현하는 방법

### 10.1 생성자 주입 (Constructor Injection) — 권장

```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel()
```

불변에 가깝게 설계하기 좋고, 필수 의존성 누락은 **컴파일 타임**에 잡히기 쉽습니다.

### 10.2 필드 / 세터 주입

가능은 하지만 테스트·불변성 측면에서 생성자 주입보다 불리한 경우가 많습니다. 새 코드는 **생성자 주입**을 우선하는 것이 일반적입니다.

### 10.3 수동 DI · Hilt · Koin

| 방식 | 설명 | 특징 |
|------|------|------|
| **수동** (`Application`, `Factory`, AppContainer) | 객체를 만들어 생성자로 넘김 | 도구 의존이 적고 흐름 추적이 쉬움. 화면·Repository가 늘면 분기·조립 코드가 길어짐 |
| **Hilt** (Dagger 기반) | 컴파일 타임에 그래프 생성, Activity·ViewModel·Compose용 `hiltViewModel()` 등과 맞물림 | 누락을 비교적 일찍 잡는 편. 어노테이션·모듈 학습 필요 |
| **Koin** 등 | 런타임에 모듈로 등록 | 설정이 단순하게 느껴질 수 있음. 실행 후에야 문제가 드러나는 경우도 있음 |

개념은 같고 **누가 객체를 만들어 주느냐**만 다릅니다.

**학습 순서 예:** 생성자 주입 → ViewModelFactory + 수동 조립 → 필요 시 Hilt.

> **코드 예시:** 위 세 방식(싱글톤만 쓰기 / 수동 DI / Hilt)을 **같은 앱**에 적용한 예는 저장소의 세 프로젝트에 있습니다. 파일·라인 인용은 **부록 B**를 보세요.

---

## 11. 수명(스코프)

| 종류 | 의미 | 예 |
|------|------|-----|
| **앱 전체 하나** | 앱 실행 중 같은 인스턴스 재사용 | Retrofit, 설정용 DataStore |
| **화면(ViewModel) 단위** | 해당 ViewModel 생명 주기 동안 | 화면 상태를 들고 있는 ViewModel |

Hilt 등은 어노테이션으로 스코프를 표시합니다. 수동 DI는 보통 `Application`에 앱 단위 객체를 두고, 팩토리에서 화면별로 조립합니다.

**싱글톤이 항상 좋은 것은 아닙니다.** 상태를 들고 있는데 앱 전체 하나로 두면 화면 전환 후에도 이전 데이터가 남을 수 있습니다. **무상태에 가까운 객체**가 앱 단위 싱글톤에 잘 맞는 경우가 많습니다.

---

## 12. 장점과 단점

### 12.1 장점

| 장점 | 설명 |
|------|------|
| **테스트 용이** | Fake/Mock 주입으로 단위 테스트가 쉬워짐 |
| **변경에 강함** | 구현 교체 시 호출부 수정을 최소화할 수 있음 |
| **의존 관계 가시화** | 생성자만 봐도 협력 객체가 드러남 |
| **객체 생명주기 정책** | 싱글톤 vs 스코프별 인스턴스를 규칙적으로 관리 가능 |
| **협업** | 모듈 경계와 책임을 나누기 좋음 |

### 12.2 단점·주의

| 단점·주의 | 설명 |
|-----------|------|
| **초기 학습 비용** | DI 개념과 도구(Hilt 등)를 함께 배워야 함 |
| **보일러플레이트** | 모듈·컴포넌트·Qualifier 설정이 늘어날 수 있음(도구에 따라 다름) |
| **과도한 추상화** | 작은 과제에서 인터페이스·팩토리를 과하게 쓰면 복잡해짐 |
| **런타임 DI 실수** | 설정 누락 시 실행 중에야 오류가 나는 경우가 있음(Hilt는 컴파일 타임에 많이 잡음) |
| **디버깅** | “누가 이 인스턴스를 만들었는지” 추적이 익숙하지 않으면 어려울 수 있음 |

| 부담 | 현실적인 대응 |
|------|----------------|
| 파일·클래스 증가 | 조립을 맡는 코드가 생김 |
| 추상화 과다 | 과제 규모에 맞게 인터페이스 수 조절 |
| 디버깅 | 팩토리·모듈에도 브레이크포인트를 찍는 습관 |

처음에는 **생성자 주입 + Application/Factory 한 곳**으로도 충분한 경우가 많고, 앱이 커지면 Hilt 등으로 옮기는 경우가 많습니다.

---

## 13. 자주 하는 오해

1. **“DI = 반드시 Hilt/Dagger다”** — **생성자로 받기만 해도** DI입니다. 라이브러리는 규모와 팀에 맞게 선택합니다.  
2. **“DI 하면 무조건 좋다”** — 과제 규모에 맞지 않으면 **간단한 팩토리**가 더 읽기 쉬울 수 있습니다.  
3. **“인터페이스를 무조건 많이 써야 한다”** — 테스트·교체가 필요한 경계에만 두는 경우가 많습니다.

---

## 14. 자주 하는 실수

1. ViewModel에 `Context`를 오래 들고 있기 → 메모리 누수 위험. `ApplicationContext` 또는 책임 이동을 검토합니다.  
2. 싱글톤에 화면마다 달라져야 할 상태 넣기 → 화면 전환 후 값이 남는 버그.  
3. **“DI 썼으니 설계가 다 해결”** — God 클래스·나쁜 책임 분리는 그대로입니다.  
4. 테스트 없이 인터페이스만 늘리기 → 비용만 커질 수 있습니다.

---

## 15. FAQ

**Q. DI를 안 쓰면 안 되나요?**  
작은 예제·초반 과제는 생략 가능합니다. 다만 생성자로 받는 습관은 이후에 도움이 됩니다.

**Q. Koin vs Hilt?**  
난이도는 케이스마다 다릅니다. 공식 자료·채용 요구는 Hilt 비중이 큰 편입니다.

**Q. `@Inject`만 붙이면 DI인가요?**  
문법에 가깝고, 본질은 **밖에서 필요한 것을 넣어 준다**는 것입니다.

**Q. Navigation Compose / 화면마다 ViewModel 스코프는?**  
**한 화면에서만 쓸 ViewModel**인지, **상위에서 여러 화면이 공유**할지는 내비게이션·Composable 구조로 정합니다. 같은 트리 안의 `viewModel()`과, Navigation의 **백스택 엔트리**마다 다른 인스턴스를 쓰는 `hiltViewModel()` 등은 그때의 **UI/스코프** 이야기입니다. 반면 **Repository를 어떻게 만들어 ViewModel 생성자에 넣을지**는 DI와 같고, 이 둘을 구분해 이해하면 됩니다.

---

## 16. 점검 체크리스트

다음에 해당이 많으면 DI(또는 DI 도구)를 본격적으로 쓸 타이밍에 가깝습니다.

- [ ] ViewModel이 여럿이고 Repository 생성 코드가 복붙된다.  
- [ ] `UserRepository()` 수정이 여러 파일을 동시에 건드린다.  
- [ ] 테스트가 네트워크에 흔들린다.  
- [ ] “앱에서 이건 하나만” 같은 규칙이 필요하다.  

해당이 거의 없으면 생성자 주입만으로도 충분한 경우가 많습니다.

---

## 17. 한 장 요약

- **DI:** 클래스가 필요로 하는 객체를 **스스로 만들지 않고 밖에서 주입**받는 설계입니다.  
- **이유:** 테스트, 변경 용이성, 책임 분리, 생명주기 정책.  
- **시기:** 의존 관계가 늘고, 테스트·모듈화가 필요해질 때.  
- **인터페이스:** 필수는 아니나 교체·테스트 경계에 두면 유리합니다.  
- **Compose 앱:** `viewModel()` / `hiltViewModel()` 등 ViewModel 획득 방식 때문에 **팩토리/Hilt** 이야기가 자주 붙습니다.  
- **장점:** 유연성, 테스트, 가시성, 협업.  
- **단점:** 학습·설정 부담, 과도한 추상화 위험 — **프로젝트 크기에 맞게** 도입하세요.

---

## 부록: 용어

| 용어 | 설명 |
|------|------|
| **클래스 / 인스턴스** | 설계도 vs 실제 만들어진 객체 |
| **생성자** | 객체 생성 시 필요한 값을 받는 부분 |
| **Composable** | `@Composable`로 선언해 Compose UI 트리에 참여하는 함수 |
| **ViewModel** | UI(Compose)와 데이터 사이에서 화면 상태·로직을 담는 계층(대략) |
| **Repository** | 서버·DB 등 출처를 앱에 맞게 묶는 계층(대략) |
| **싱글톤** | 앱 안에서 인스턴스 하나만 쓰도록 두는 패턴 |
| **Factory** | 객체 생성을 맡기는 코드 |
| **Mock / Fake** | 테스트용 가짜 구현 |

---

## 부록 B: 세 샘플 프로젝트로 보는 코드 (참고)

본문(특히 **3·7·10절**)을 읽은 뒤, **같은 기능의 앱**에서 DI만 다르게 구현한 예로 소스를 열어볼 때 쓰는 부록입니다.  
`archive/sample-project/` 아래 세 프로젝트는 **Kotlin·Jetpack Compose** UI이며, 패키지 **`com.likelion.liontalk`**로 동일하고 **폴더 이름**으로만 구분합니다.

| 폴더 | DI 방식 | 대표 파일 |
|------|---------|-----------|
| [likelion-non-di](../likelion-non-di/README.md) | 컨테이너 없음 — `object`·`getInstance` 등 | `…/core/data/repository/UserRepository.kt`, `…/features/auth/viewmodel/SignViewModel.kt`, `…/features/chat/viewmodel/ChatRoomViewModelFactory.kt` |
| [likelion-manual-di](../likelion-manual-di/README.md) | 수동 DI — `AppContainer` + `ViewModelFactory` | `…/core/di/AppContainer.kt`, `…/core/di/ViewModelFactory.kt`, `…/LionTalkApplication.kt` |
| [likelion-hilt-di](../likelion-hilt-di/README.md) | Hilt | `…/LionTalkApplication.kt`, `…/features/auth/di/AuthHiltModule.kt`, `…/core/di/FirebaseAuthHiltModule.kt`, `…/features/chat/di/ChatHiltModule.kt` |

경로 접두: `archive/sample-project/<폴더>/app/src/main/java/com/likelion/liontalk/`.  
아래는 로그인 **`SignViewModel`** 등을 기준으로 한 발췌입니다.

### likelion-non-di — 싱글톤 `object` + 내부에서 조립

`UserRepository`를 **`object`**로 두어 앱 전체가 한 인스턴스를 공유합니다.

```22:24:archive/sample-project/likelion-non-di/app/src/main/java/com/likelion/liontalk/core/data/repository/UserRepository.kt
/** Auth 상태랑 Firestore 유저 문서 둘 다 여기서 맞춤. authUser는 Firebase 쪽, me는 앱에서 쓰는 프로필. */
object UserRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
```

`SignViewModel`은 **생성자 주입 없이** `getInstance`와 `UserRepository`를 필드에서 직접 가져옵니다 (**3.1절 안에서 만들기**에 가깝습니다).

```36:38:archive/sample-project/likelion-non-di/app/src/main/java/com/likelion/liontalk/features/auth/viewmodel/SignViewModel.kt
class SignViewModel : ViewModel() {
    private val authRepository = AuthRepository.getInstance(LionTalkApplication.appContext)
    private val userRepository = UserRepository
```

`SavedStateHandle`이 필요한 `ChatRoomViewModel`만 **전용 Factory**로 생성합니다.

```17:27:archive/sample-project/likelion-non-di/app/src/main/java/com/likelion/liontalk/features/chat/viewmodel/ChatRoomViewModelFactory.kt
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ChatRoomViewModel::class.java) ->
                ChatRoomViewModel(handle) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
```

### likelion-manual-di — `AppContainer` + `ViewModelFactory`

```9:18:archive/sample-project/likelion-manual-di/app/src/main/java/com/likelion/liontalk/LionTalkApplication.kt
class LionTalkApplication : Application() {
    val container: AppContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        // ...
        container.userRepository.start()
```

```22:26:archive/sample-project/likelion-manual-di/app/src/main/java/com/likelion/liontalk/core/di/AppContainer.kt
    val userRepository: UserRepository by lazy {
        UserRepository(firebaseAuth = firebaseAuth)
    }

    val authRepository: AuthRepository by lazy { AuthRepository(appContext) }
```

`ViewModelFactory`에서 생성자로 넘기는 부분(**3.2절**).

```36:40:archive/sample-project/likelion-manual-di/app/src/main/java/com/likelion/liontalk/core/di/ViewModelFactory.kt
            modelClass.isAssignableFrom(SignViewModel::class.java) ->
                SignViewModel(
                    authRepository = appContainer.authRepository,
                    userRepository = appContainer.userRepository,
                ) as T
```

```34:37:archive/sample-project/likelion-manual-di/app/src/main/java/com/likelion/liontalk/features/auth/viewmodel/SignViewModel.kt
class SignViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
```

### likelion-hilt-di — Hilt

```10:13:archive/sample-project/likelion-hilt-di/app/src/main/java/com/likelion/liontalk/LionTalkApplication.kt
@HiltAndroidApp
class LionTalkApplication : Application() {
    @Inject
    lateinit var userRepository: UserRepository
```

```15:26:archive/sample-project/likelion-hilt-di/app/src/main/java/com/likelion/liontalk/features/auth/di/AuthHiltModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AuthHiltModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepository(context = context)
}
```

```34:41:archive/sample-project/likelion-hilt-di/app/src/main/java/com/likelion/liontalk/features/auth/viewmodel/SignViewModel.kt
@HiltViewModel
/**
 * 로그인 화면에서 인증 동작과 로그인 이후 라우팅을 담당하는 ViewModel입니다.
 */
class SignViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
```

### 한눈에 비교

| 항목 | non-di | manual-di | hilt-di |
|------|--------|-----------|---------|
| Repository 한 벌 유지 | `object` / `getInstance` | `AppContainer`의 `lazy` | Hilt `@Singleton` 등 |
| ViewModel에 넣는 주체 | 클래스 내부 참조 | `ViewModelFactory` | Hilt ViewModel 팩토리 |
| 조립 규칙이 모이는 곳 | 흩어짐 | `AppContainer` + Factory | `@Module` |

라우팅·학습 포인트: [likelion-non-di](../likelion-non-di/README.md) · [likelion-manual-di](../likelion-manual-di/README.md) · [likelion-hilt-di](../likelion-hilt-di/README.md).

---

## 참고

- Android Developers: [Dependency injection in Android](https://developer.android.com/training/dependency-injection)
- **샘플 앱 README:** [likelion-non-di](../likelion-non-di/README.md) · [likelion-manual-di](../likelion-manual-di/README.md) · [likelion-hilt-di](../likelion-hilt-di/README.md) — 소스 발췌는 **부록 B**.
