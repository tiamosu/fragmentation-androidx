@file:Suppress("all")

object Android {
    const val compileSdkVersion = 28
    const val buildToolsVersion = "28.0.3"
    const val minSdkVersion = 15
    const val targetSdkVersion = 28

    const val versionName = "1.0"
    const val versionCode = 1
}

object Publish {
    const val userOrg = "weixia" //bintray.com用户名
    const val groupId = "me.xia" //jcenter上的路径
    const val publishVersion = "1.0.0" //版本号
    const val desc = "Oh hi, this is a nice description for a project, right?"
    const val website = "https://github.com/wexia/fragmentation-androidx"
    const val gitUrl = "https://github.com/wexia/fragmentation-androidx.git"
    const val email = "djy2009wenbi@gmail.com"
}

object Deps {
    const val appcompat = "androidx.appcompat:appcompat:1.0.2"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val material = "com.google.android.material:material:1.0.0"
    const val cardview = "androidx.cardview:cardview:1.0.0"
    const val recyclerview = "androidx.recyclerview:recyclerview:1.0.0"
    const val eventbus = "org.greenrobot:eventbus:3.1.1"
}