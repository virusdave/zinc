/**
 * Copyright (C) 2012 Typesafe, Inc. <http://www.typesafe.com>
 */

import sbt._
import sbt.Keys._
import sbt.internal.inc.classpath.ClasspathUtilities
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.{packageZipTarball, stage, stagingDirectory}
//import com.typesafe.sbt.S3Plugin.{ S3, s3Settings }

object Dist {
  val distLibs = taskKey[Seq[Attributed[File]]]("List of dist jars")
  val create = taskKey[File]("Create the distribution")

  lazy val settings: Seq[Setting[_]] = distSettings //++ s3PublishSettings

  lazy val distSettings: Seq[Setting[_]] = /*packagerSettings ++*/ Seq(
    publishArtifact in Universal := false,
    distLibs := (fullClasspath in Compile).value.filter(cpElem => ClasspathUtilities.isArchive(cpElem.data)),
    create := {
      (stage in Universal).value
      (stagingDirectory in Universal).value
    },
    mappings in Universal ++= distLibs.value.map(named),
    mappings in Universal += {
      val zincJar = (packageBin in Compile).value
      val zincArtifact = (artifact in Compile).value
      zincJar -> filename(zincArtifact)
    }
  )

// NB: We're not trying to publish the zinc artifacts to S3, so i didn't bother to upgrade the s3 publishing-related
// Sbt plugins to the newer Sbt version
/*
  lazy val s3PublishSettings: Seq[Setting[_]] = s3Settings ++ Seq(
    mappings in S3.upload := {
      val name = "zinc"
      val file = "%s-%s.tgz" format (name, version.value)
      val path = Seq(name, version.value, file) mkString "/"
      Seq((packageZipTarball in Universal).value -> path)
    },
    S3.host in S3.upload := "downloads.typesafe.com.s3.amazonaws.com",
    S3.progress in S3.upload := true,
    credentials in S3.upload := Seq(Credentials(Path.userHome / ".typesafe-s3-credentials"))
  )
*/

  private def filename(a: Artifact) =
    "lib/" + a.name + a.classifier.map("-"+_).getOrElse("") + "." + a.extension
  private def named(a: Attributed[File]) =
    a.data -> a.get(artifact.key).map(filename).getOrElse(a.data.name)
}
