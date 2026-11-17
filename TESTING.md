# Тестирование Kotlin Pokedex

## Структура тестового проекта

```text
Kotlin-Pokedex/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/dev/marcosfarias/pokedex/
│       │   │   ├── ui/pokedex/PokedexViewModel.kt
│       │   │   └── ui/pokedex/PokedexFragment.kt
│       │   └── res/
│       │       ├── layout/fragment_pokedex.xml
│       │       └── values/strings.xml
│       ├── test/
│       │   ├── java/dev/marcosfarias/pokedex/
│       │   │   ├── api/
│       │   │   │   ├── KtorPokeApi.kt
│       │   │   │   ├── KtorPokeApiTest.kt
│       │   │   │   ├── PokeApiClientTest.kt
│       │   │   │   └── PokeApiServiceTest.kt
│       │   │   ├── unit/PokedexViewModelTest.kt
│       │   │   ├── DashboardViewModelTest.kt
│       │   │   ├── GenerationViewModelTest.kt
│       │   │   ├── HomeViewModelTest.kt
│       │   │   └── PokedexViewModelTest.kt
│       │   └── resources/allure.properties
│       └── androidTest/
│           ├── java/dev/marcosfarias/pokedex/
│           │   ├── blackbox/
│           │   │   ├── PokemonNavigationScenarioTest.kt
│           │   │   ├── PokemonSearchScenarioTest.kt
│           │   │   ├── PokemonDetailsScenarioTest.kt
│           │   │   └── PokemonErrorScenarioTest.kt
│           │   ├── ui/
│           │   │   ├── PokemonListScreenTest.kt
│           │   │   ├── PokemonDetailsScreenTest.kt
│           │   │   ├── PokemonSearchScreenTest.kt
│           │   │   └── PokemonErrorScreenTest.kt
│           │   ├── robots/BaseRobot.kt
│           │   ├── HomeTest.kt
│           │   ├── PokedexTest.kt
│           │   ├── FragmentNewsDetailsTest.kt
│           │   └── ExampleInstrumentedTest.kt
│           └── resources/allure.properties
├── reports/
│   └── README.md
└── .github/workflows/android-tests.yml
```

## Что покрывается

- **Unit:** ViewModel и бизнес-логика с MockK, минимум пять отдельных тестов PokedexViewModel плюс тесты Home, Dashboard и Generation.
- **Espresso:** домашний экран, список покемонов, карточка деталей, поиск и отображение сетевой ошибки.
- **UI Automator:** переход Home → Pokédекс, открытие поиска, открытие деталей и сценарий недоступности API без подмены репозитория.
- **Ktor Client:** шесть тестов для трёх ресурсов PokéAPI: `pokemon`, `type`, `ability`; для каждого есть успешный и ошибочный ответ через MockEngine.
- **Allure:** JUnit4 Allure runner для JVM-тестов и Allure Android runner/TestStorage для инструментальных тестов.
- **JaCoCo:** HTML и XML отчёты покрытия unit-тестами; Android Gradle Plugin формирует отдельный coverage output для instrumented tests.

Приложение в исходном виде использует Retrofit и предоставляет один endpoint `pokemon.json`. Чтобы выполнить отдельное требование ТЗ о трёх API-методах на Ktor Client, тестовый набор проверяет три публичных ресурса PokéAPI с MockEngine. Это тестовый API-клиент, он не заменяет Retrofit-клиент приложения.

## Запуск

Из корня проекта:

```bash
# Unit tests
./gradlew :app:testDebugUnitTest

# JaCoCo unit coverage
./gradlew :app:jacocoTestReport

# Espresso + UI Automator (нужен эмулятор или устройство)
./gradlew :app:connectedDebugAndroidTest

# Скопировать результаты Allure с устройства
adb pull /sdcard/googletest/test_outputfiles/allure-results reports/allure-results/androidTest

# Android instrumentation coverage
./gradlew :app:createDebugCoverageReport

# Сформировать HTML Allure
./gradlew :app:allureReport
```

Сгенерированные материалы находятся в `reports/`. Репозиторий не содержит искусственно созданных отчётов: HTML и coverage-файлы должны быть получены после фактического запуска тестов. GitHub Actions запускает проверки и прикладывает результаты как workflow artifact `pokedex-test-reports`.
