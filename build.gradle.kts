// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
}
// 顶级 build.gradle.kts 文件

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // 添加 JitPack 仓库
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0") // 使用合适的版本
    }
}