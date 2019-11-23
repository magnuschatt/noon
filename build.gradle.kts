plugins {
    base
}

allprojects {

    group = "chatt.noon"
    version = "1.0"

    repositories {
        jcenter()
        mavenCentral()
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}

