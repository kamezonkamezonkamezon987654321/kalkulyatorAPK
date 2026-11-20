# Kalkulyator APK

Android uchun tarixli kalkulyator (Kotlin + Jetpack Compose).

## Imkoniyatlar

- Qo'shish, ayirish, ko'paytirish, bo'lish, foiz (`%`), qavslar
- Aniq o'nlik hisob: `0.1 + 0.2 = 0.3` (BigDecimal)
- **Ilmiy rejim** (`f(x)` tugmasi): `sin`, `cos`, `tan`, `ln`, `log`, `√`, `xʸ`, `x²`, `π`, `e`.
  Burchaklar uchun `DEG` (gradus) va `RAD` (radian) almashtirgich bor
- Juda katta va juda kichik natijalar `1.5×10^-12` ko'rinishida chiqadi
- Yozish davomida natija oldindan ko'rinadi
- **Tarix**: har bir hisob saqlanadi (oxirgi 100 ta), ilova yopilsa ham yo'qolmaydi.
  Tarixdagi natijani bosib, hisobni davom ettirish mumkin
- Tungi rejim va Android 12+ da dinamik ranglar

## APK ni yuklab olish

1. Repodagi **Actions** bo'limiga o'ting
2. Oxirgi muvaffaqiyatli **APK build** ishini oching
3. Pastdagi **Artifacts** dan `kalkulyator-apk` ni yuklab oling, zip ichida `app-debug.apk` bor
4. Telefonga o'tkazib o'rnating ("noma'lum manbalardan o'rnatish" ruxsati kerak bo'ladi)

## O'zingiz build qilish

Android Studio'da oching yoki:

```bash
./gradlew testDebugUnitTest   # testlar
./gradlew assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
```

Talablar: JDK 17, Android SDK 35. Minimal Android versiyasi: 8.0 (API 26).

## Tuzilishi

```
app/src/main/java/uz/kalkulyator/
├── MainActivity.kt
├── CalculatorViewModel.kt      # holat va tugmalar mantig'i
├── calc/
│   ├── ExpressionEvaluator.kt  # ifodani hisoblash (parser)
│   ├── InputEditor.kt          # kiritishni to'g'rilash qoidalari
│   └── HistoryStore.kt         # tarixni saqlash
└── ui/
    ├── CalculatorScreen.kt     # ekran, tugmalar, tarix ro'yxati
    └── Theme.kt
```
