# Bhasha Pul — Android translator app

Ye app har dusre app ke andar (WhatsApp, Instagram, browser, koi bhi) screen par
aane wale text ko padhta hai, aapki select ki hui bhasha mein translate karta hai,
aur ek chote overlay box mein neeche dikhata hai.

## Setup — bina Android Studio install kiye (GitHub Actions se cloud mein build)

Aapko apne computer pe kuch bhi install nahi karna. Sirf browser chahiye.

1. [github.com](https://github.com) par free account banayein (agar nahi hai).
2. GitHub par "New repository" banayein (koi bhi naam, jaise `bhasha-pul`), public ya private dono chalega.
3. Us naye repo ke page par "uploading an existing file" link dikhega — us par click karke
   is poore `BhashaPulApp` folder ke andar ke saare files/folders drag-and-drop karke upload
   kar dein (build.gradle, app folder, .github folder, sab kuch), aur "Commit changes" dabayein.
4. Repo ke upar "Actions" tab par jaayein — build apne aap shuru ho jayegi (2-4 minute lagenge).
5. Build poora hone ke baad, us workflow run ke andar "Artifacts" section mein
   "BhashaPul-debug-apk" milega — usse download kar lein (ek zip milega, andar APK hai).
6. Wo APK apne Android phone mein bhej dein (WhatsApp, Google Drive, email — kuch bhi),
   phone par kholkar install kar lein. Phone "Install from unknown sources" allow karne
   ko bolega — settings mein wo permission de dein.

Isme kahin bhi Android Studio ya coding tool install karne ki zaroorat nahi — sab kuch
GitHub ke servers par ban jayega, aap sirf final APK file download karte hain.

## Setup — agar phone pe hi sab karna hai (Termux, thoda technical)

Agar computer bilkul use nahi karna, Android phone par "Termux" app (F-Droid se) install
karke, usme Gradle aur Android SDK command-line tools setup karke bhi build kiya ja sakta
hai — lekin ye kaafi lamba aur technical process hai. GitHub Actions wala tarika bahut
aasan hai, wahi try karein pehle.

## Purana tarika (agar Android Studio install karna chahen)

1. Is poore `BhashaPulApp` folder ko Android Studio mein "Open" karein.
2. Gradle sync hone dein (pehli baar internet chahiye hoga dependencies ke liye).
3. Phone ko USB se connect karein (Developer Options + USB Debugging on karke), ya emulator use karein.
4. App run karein.
5. App khulne par:
   - Apni target language select karein.
   - "Overlay permission dein" dabakar permission on karein.
   - "Accessibility mein Bhasha Pul on karein" dabakar Settings > Accessibility mein jaake
     "Bhasha Pul" ko on karein.
6. Ab koi bhi app (WhatsApp, Instagram, etc.) kholiye — jaise hi naya text screen par
   aayega, translation neeche overlay mein apne aap dikhega.

## Kaise kaam karta hai

- `TranslateAccessibilityService.kt` — Android ki Accessibility API se screen ka text padhta hai.
- `TranslationClient.kt` — us text ko MyMemory (free translation API) ko bhejta hai.
- `OverlayManager.kt` — translated text ko ek floating box mein dikhata hai jo har app ke upar rehta hai.
- `MainActivity.kt` — language select karne aur permissions dene ka simple screen.

## Limitations (important, honestly bata raha hoon)

- Free MyMemory API rate-limited hai (roughly 5000 words/day bina key ke). Zyada use
  ke liye Google Cloud Translate ya DeepL API key daal sakte hain `TranslationClient.kt` mein.
- Accessibility service **pura screen text padh sakta hai** — matlab ye apps ke andar ka
  data dekh sakta hai. Ye zaroori hai translation ke liye, lekin iska matlab ye bhi hai
  ki koi bhi app jisko aap "Accessibility" permission dete hain, sensitive info
  (passwords, OTPs) bhi dekh sakta hai agar wo screen par ho. Ye app sirf translate
  karta hai, kahin data bhejta nahi (translation API ke alawa) — lekin ye ek strong
  permission hai, isliye samajh kar hi on karein.
- Encrypted messaging apps (jaise WhatsApp) mein screenshot-block ya secure fields
  kabhi kabhi accessibility se text nahi dete.
- Voice-to-English wala feature already web tool (bhasha-pul.html) mein hai —
  isko is Android app mein bhi add karna ho to bataiye, agla step wahi hoga.
