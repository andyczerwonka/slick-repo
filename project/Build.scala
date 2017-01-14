/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys._
import com.typesafe.sbt.pgp.PgpKeys._

object Build extends Build {

  val dependencyResolvers = Seq("Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/")

  val dependencies = Seq(
    "com.typesafe.slick" %% "slick" % "3.1.1",

    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "com.typesafe.slick" %% "slick-extensions" % "3.1.0" % "test",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1" % "test",
    "com.h2database" % "h2" % "1.4.192" % "test",
    "mysql" % "mysql-connector-java" % "5.1.38" % "test",
    "org.postgresql" % "postgresql" % "9.4.1211" % "test",
    "org.slf4j" % "slf4j-simple" % "1.7.21" % "test",
    "org.apache.derby" % "derby" % "10.11.1.1" % "test",
    "org.hsqldb" % "hsqldb" % "2.3.4" % "test",
    "joda-time" % "joda-time" % "2.9.6" % "test"
  )

  lazy val project: Project =
    Project("root", file("."))
      .configs(AllDbsTest, Db2Test, SqlServerTest)
      .settings(inConfig(AllDbsTest)(Defaults.testTasks): _*)
      .settings(inConfig(Db2Test)(Defaults.testTasks): _*)
      .settings(inConfig(SqlServerTest)(Defaults.testTasks): _*)
      .settings(

        name := "slick-repo",
        description := "CRUD Repositories for Slick based persistence Scala projects",
        version := "1.2.2",

        scalaVersion := "2.11.8",
        crossScalaVersions := Seq("2.11.8", "2.10.6"),

        libraryDependencies ++= dependencies,

        resolvers ++= dependencyResolvers,

        parallelExecution in Test := false,
        coverageEnabled := true,

        testOptions in Test := Seq(Tests.Filter(baseFilter)),
        testOptions in Db2Test := Seq(Tests.Filter(db2Filter)),
        testOptions in AllDbsTest := Seq(Tests.Filter(allDbsFilter)),
        testOptions in SqlServerTest := Seq(Tests.Filter(sqlServerFilter)),

        publishMavenStyle := true,
        organization:= "com.byteslounge",
        pomIncludeRepository := { _ => false },
        publishArtifact in Test := false,
        publishTo := {
          val nexus = "https://oss.sonatype.org/"
          if (isSnapshot.value)
            Some("snapshots" at nexus + "content/repositories/snapshots")
          else
            Some("releases"  at nexus + "service/local/staging/deploy/maven2")
        },
        credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
        useGpg := true,
        pomExtra :=
          <url>https://github.com/gonmarques/slick-repo</url>
          <inceptionYear>2016</inceptionYear>
          <licenses>
            <license>
              <name>The Apache Software License, Version 2.0</name>
              <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            </license>
          </licenses>
          <developers>
            <developer>
              <id>gonmarques</id>
              <name>Gonçalo Marques</name>
              <url>https://github.com/gonmarques</url>
            </developer>
          </developers>
          <contributors>
            <contributor>
              <name>Cláudio Diniz</name>
              <url>https://github.com/cdiniz</url>
            </contributor>
          </contributors>
          <scm>
            <url>https://github.com/gonmarques/slick-repo.git</url>
            <connection>scm:git:git://github.com/gonmarques/slick-repo.git</connection>
          </scm>

      )

  lazy val mysql: Project =
    Project("mysql", file("src/docker/mysql"))
      .settings(
        name := "mysql"
      )

  lazy val oracle: Project =
    Project("oracle", file("src/docker/oracle"))
      .settings(
        name := "oracle"
      )

  lazy val db2: Project =
    Project("db2", file("src/docker/db2"))
      .settings(
        name := "db2"
      )

  lazy val postgres: Project =
    Project("postgres", file("src/docker/postgres"))
      .settings(
        name := "postgres"
      )

  val dbPrefixes = Seq("MySQL", "Oracle", "Postgres", "Derby", "Hsql")
  val db2Prefix = Seq("DB2")
  val sqlServerPrefix = Seq("SQLServer")
  lazy val AllDbsTest: Configuration = config("alldbs") extend Test
  lazy val Db2Test: Configuration = config("db2") extend Test
  lazy val SqlServerTest: Configuration = config("sqlserver") extend Test

  def testName(name: String): String = name.substring(name.lastIndexOf('.') + 1)

  def allDbsFilter(name: String): Boolean = dbPrefixes.exists(p => testName(name) startsWith p)

  def db2Filter(name: String): Boolean = db2Prefix.exists(p => testName(name) startsWith p)

  def sqlServerFilter(name: String): Boolean = sqlServerPrefix.exists(p => testName(name) startsWith p)

  def baseFilter(name: String): Boolean = !allDbsFilter(name) && !db2Filter(name) && !sqlServerFilter(name)
}
