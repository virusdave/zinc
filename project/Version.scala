/**
 * Copyright (C) 2012 Typesafe, Inc. <http://www.typesafe.com>
 */

import java.util.{ Date, TimeZone }
import sbt._
import sbt.Keys._
import scala.sys.process.Process
import xsbti.compile.CompileAnalysis

object Version {
  val currentCommit = settingKey[String]("current-commit")

  lazy val settings: Seq[Setting[_]]  = Seq(
    //currentCommit := gitCommitHash(baseDirectory.value, streams.value),
    currentCommit := "HEAD",

    resourceGenerators in Compile +=
      Def.task { generateFile(version.value, currentCommit.value, resourceManaged.value, compile in Compile value, streams.value) }
  )

  def gitCommitHash(dir: File, s: TaskStreams): String = {
    try { Process(Seq("git", "rev-parse", "HEAD"), dir) !! s.log }
    catch { case e: Exception => "unknown" }
  }

  def generateFile(version: String, commit: String, dir: File, analysis: CompileAnalysis, s: TaskStreams): Seq[File] = {
    val file = dir / "zinc.version.properties"
    val formatter = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")
    formatter.setTimeZone(TimeZone.getTimeZone("GMT"))
    val timestamp = formatter.format(new Date)
    val content = """
      |version=%s
      |timestamp=00000000-000000
      |commit=%s
      """.trim.stripMargin format (version, commit)
    if (!file.exists || file.lastModified < Util.lastCompile(analysis)) {
      s.log.info("Generating version file: " + file)
      IO.write(file, content)
    }
    Seq(file)
  }
}
