# App-specific R8 rules, applied on top of proguard-android-optimize.txt.
#
# No keep rules are needed: AAPT2 keeps the manifest components, the default file keeps native
# methods, enum values()/valueOf() and Parcelable CREATORs, and the libraries bundle their own
# rules (Retrofit's keep the service interfaces, suspend functions and generic signatures).
# Add a rule here only for a class that is looked up by reflection.

-repackageclasses ''

# Readable stack traces: keep line numbers, but name every source file "SourceFile".
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*

-dontwarn com.google.errorprone.annotations.*
-dontwarn org.jetbrains.annotations.**
