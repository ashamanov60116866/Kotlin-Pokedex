# Test reports

This directory is the destination for generated test reports.

After running the commands below, the project writes:

- `allure-results/unit/` — raw Allure results for local JVM tests.
- `allure-results/androidTest/` — raw Allure results pulled from the emulator/device.
- `allure-report/allureReport/` — generated Allure HTML report.
- `coverage/html/` and `coverage/jacoco.xml` — JaCoCo coverage report.

Run from the repository root:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:jacocoTestReport
./gradlew :app:connectedDebugAndroidTest
adb pull /sdcard/googletest/test_outputfiles/allure-results reports/allure-results/androidTest
./gradlew :app:allureReport
```

Instrumented tests require an Android emulator or connected device (API 33 recommended).
The repository intentionally does not contain fabricated report output: Allure and JaCoCo
files are generated from actual test executions. CI uploads the generated reports as artifacts.
