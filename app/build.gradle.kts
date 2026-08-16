import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// 署名情報はリポジトリ管理外の keystore.properties から読む（鍵やパスワードをコミットしない）
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

// バージョンは git tag (vMAJOR.MINOR.PATCH) を正として、Gradle property から注入する。
// 例: ./gradlew assembleRelease -PversionName=1.2.3
// property を渡さないローカルビルドはプレースホルダ値 (1 / "1.0") にフォールバックする。
//
// versionCode の採番規則（変更禁止・単一正本）:
//   versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
//   例: v1.0.0 → 10000 / v1.2.3 → 10203
//   MINOR / PATCH は 99 以下でなければならない（超えると桁が衝突し単調増加が崩れる。
//   例: v1.0.100 と v1.1.0 はどちらも 10100 になる）。versionName が MAJOR.MINOR.PATCH
//   形式で渡された場合、versionCode の指定有無に関わらずこの境界を検証する。
val versionNameProperty = project.findProperty("versionName") as String?
val versionCodeProperty = project.findProperty("versionCode") as String?

data class TaggedVersion(val major: Int, val minor: Int, val patch: Int)

fun parseTaggedVersion(versionName: String): TaggedVersion {
    val match = Regex("""^(\d+)\.(\d+)\.(\d+)$""").matchEntire(versionName)
        ?: throw GradleException(
            "versionName must be MAJOR.MINOR.PATCH to derive versionCode, got: $versionName"
        )
    val (major, minor, patch) = match.destructured
    val tagged = TaggedVersion(major.toInt(), minor.toInt(), patch.toInt())
    if (tagged.minor > 99 || tagged.patch > 99) {
        throw GradleException(
            "versionName $versionName has MINOR or PATCH over 99: versionCode would collide " +
                "with the next MAJOR/MINOR. Revisit the numbering scheme before releasing."
        )
    }
    return tagged
}

val taggedVersion = versionNameProperty?.let(::parseTaggedVersion)

val resolvedVersionName = versionNameProperty ?: "1.0"
val resolvedVersionCode = when {
    versionCodeProperty != null -> versionCodeProperty.toIntOrNull()
        ?: throw GradleException("versionCode property must be an integer, got: $versionCodeProperty")
    taggedVersion != null -> taggedVersion.major * 10000 + taggedVersion.minor * 100 + taggedVersion.patch
    else -> 1
}

android {
    namespace = "io.github.swat9013.apphub"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.swat9013.apphub"
        minSdk = 26
        targetSdk = 35
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // keystore.properties がある時のみ署名する。無ければ unsigned release ビルド
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                null
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
