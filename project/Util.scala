/**
 * Copyright (C) 2012 Typesafe, Inc. <http://www.typesafe.com>
 */

import sbt._
import xsbti.compile.CompileAnalysis

object Util {

  def environment(property: String, env: String): Option[String] =
    Option(System.getProperty(property)) orElse Option(System.getenv(env))

  def lastCompile(analysis: CompileAnalysis): Long = {
    val times = analysis.readCompilations.getAllCompilations.toSeq.map(_.getStartTime)
    if( times.isEmpty) 0L else times.max
  }
}
