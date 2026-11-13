# Configuration parsers use JSON-smart's runtime JSON provider.
-keep class net.minidev.json.** { *; }
-keep class net.minidev.asm.** { *; }
# json-path 2.x ships no consumer rules; its internals are reached reflectively.
-keep class com.jayway.jsonpath.** { *; }
# json-path bundles optional SPI providers (Jackson/Gson/jakarta.json/tapestry/jettison)
# that this app never uses — it runs on the default json-smart provider above. Keeping
# all of com.jayway.jsonpath drags those providers in, so silence their absent deps.
-dontwarn com.fasterxml.jackson.core.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn com.google.gson.**
-dontwarn jakarta.json.**
-dontwarn org.apache.tapestry5.json.**
-dontwarn org.codehaus.jettison.json.**
# Shizuku exposes its binder interfaces across processes.
-keep class rikka.shizuku.** { *; }
