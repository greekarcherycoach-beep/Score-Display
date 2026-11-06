ΟΔΗΓΙΕΣ ΔΗΜΙΟΥΡΓΙΑΣ APK (μία φορά, στον υπολογιστή)
1. Εγκαταστήστε το Android Studio (δωρεάν): https://developer.android.com/studio
2. File > Open > επιλέξτε αυτόν τον φάκελο (ArcheryLabApp). Περιμένετε να τελειώσει το "Gradle sync" (θέλει internet).
3. Menu: Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Το αρχείο βρίσκεται στο app/build/outputs/apk/debug/app-debug.apk
5. Αντιγράψτε το APK σε κινητά και tablet και εγκαταστήστε το (επιτρέψτε "εγκατάσταση από άγνωστες πηγές").
6. Ανοίξτε την εφαρμογή: "Κινητό αθλητή" στα κινητά, "Tablet κατάταξης" στο tablet.
Σύνδεση: tablet -> "📷 Σάρωση QR κινητού" μετά το QR του κινητού. Κινητό -> σαρώνει το QR του tablet και δείχνει δικό του QR.
Ο σαρωτής QR είναι ενσωματωμένος (δουλεύει χωρίς internet). Την πρώτη φορά η εφαρμογή θα ζητήσει άδεια κάμερας: πατήστε "Να επιτρέπεται".
Αν αλλάξετε τα αρχεία HTML, αντικαταστήστε τα στο app/src/main/assets/ και ξαναφτιάξτε το APK.

ΕΝΑΛΛΑΚΤΙΚΑ: ΔΗΜΙΟΥΡΓΙΑ APK ΧΩΡΙΣ ΕΓΚΑΤΑΣΤΑΣΗ ΣΤΟΝ ΥΠΟΛΟΓΙΣΤΗ (GitHub, δωρεάν)
1. Φτιάξτε δωρεάν λογαριασμό στο github.com και ένα νέο repository (Create new repository).
2. Add file > Upload files: ανεβάστε ΟΛΟ το περιεχόμενο αυτού του φακέλου (app, build.gradle.kts, settings.gradle.kts, gradle.properties).
3. Add file > Create new file. Στο όνομα γράψτε:  .github/workflows/build-apk.yml  και επικολλήστε το περιεχόμενο του αρχείου build-apk.yml. Commit.
4. Καρτέλα Actions > "Build APK" > Run workflow. Μετά από 3-6 λεπτά ανοίξτε το run και κατεβάστε το artifact "ArcheryLab-APK" (zip με το app-debug.apk).
ΣΗΜΑΝΤΙΚΟ: μην χρησιμοποιείτε online μετατροπείς HTML σε APK (html2apk κλπ): δεν έχουν τον ενσωματωμένο σαρωτή QR.
