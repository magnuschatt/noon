plugins {
    base
}

allprojects {

    group = "chatt.noon"
    version = "1.0"

    repositories {
        jcenter()
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}

