group = "imagemagick"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application.mainClass = "imagemagick.MainKt"

tasks.getByName<Sync>("installDist").let {
    it.destinationDir = file("${System.getProperty("user.home")}/.local/magick-kt-tooling")
}
