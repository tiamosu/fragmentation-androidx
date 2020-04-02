@file:Suppress("unused")

object Android {
    const val compileSdkVersion = 29
    const val buildToolsVersion = "29.0.2"
    const val minSdkVersion = 15
    const val targetSdkVersion = 29

    const val versionName = "1.0"
    const val versionCode = 1
}

object Publish {
    const val userOrg = "weixia" //bintray.com用户名
    const val groupId = "me.xia" //jcenter上的路径
    const val publishVersion = "1.1.8" //版本号
    const val desc = "Oh hi, this is a nice description for a project, right?"
    const val website = "https://github.com/tiamosu/fragmentation-androidx"
    const val gitUrl = "https://github.com/tiamosu/fragmentation-androidx.git"
    const val email = "djy2009wenbi@gmail.com"
}

object Deps {
    const val appcompat = "androidx.appcompat:appcompat:1.1.0"
    const val material = "com.google.android.material:material:1.1.0"
    const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
    const val cardview = "androidx.cardview:cardview:1.0.0"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.0.0"
    const val eventbus = "org.greenrobot:eventbus:3.2.0"
}