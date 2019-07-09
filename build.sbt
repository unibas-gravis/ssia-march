organization  := "ch.unibas.cs.gravis"

name := """scalismo-tutorial"""
version       := "0.16.0"

scalaVersion  := "2.12.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.bintrayRepo("unibas-gravis", "maven")

resolvers += Opts.resolver.sonatypeSnapshots


libraryDependencies  ++= Seq(
            "ch.unibas.cs.gravis" % "scalismo-native-all" % "4.0.0",
            "ch.unibas.cs.gravis" %% "scalismo-ui" % "develop-d588c40861c818a7a2e2c972f5e3cc3e57df9410-SNAPSHOT",
            "com.cibo" %% "evilplot" % "0.6.3"
)


lazy val root = (project in file("."))

lazy val docs = project       // new documentation project
  .in(file("myproject-docs"))
  .settings(
    mdocIn := new java.io.File("docs/mdocs/"),
    mdocOut := new java.io.File("docs/")
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin)
 
