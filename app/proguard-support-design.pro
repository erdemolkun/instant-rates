
# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know ic_about them, and they are safe.

# support design
-dontwarn android.support.**
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
-keep public class * extends android.support.design.widget.CoordinatorLayout$Behavior {
    public <init>(android.content.Context, android.util.AttributeSet);
}
