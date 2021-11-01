package com.sbuslab.marathon.plugin

import scala.collection.immutable

import mesosphere.marathon.plugin._
import org.apache.mesos.Protos.TaskInfo
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import play.api.libs.json.{JsObject, Json}


@RunWith(classOf[JUnitRunner])
class MarathonSecretsPluginTest extends FunSuite {

  private val config = Json.parse("""
    {
      "encryptKey": "aiHag0kaimaiSh5hiepheikieGhieKai2ui6uqu0aix9ewie9Laehahb8ohxahsh",
      "consulPath": "https://localhost:8500/v1/kv/services/keys"
    }
  """).as[JsObject]

  val appSpec: ApplicationSpec = new ApplicationSpec {
    val env: Map[String, EnvVarValue] = Map(
      "SERVICE_KEY" → new EnvVarString { val value = "exchange/order-service-test" },
    )

    val id: PathId = new PathId {
      def path: immutable.Seq[String] = List("exchange", "order-service-test")
      override def toString = "/" + path.mkString("/")
    }

    val user: Option[String] = Some("root")
    val labels: Map[String, String] = Map.empty
    val acceptedResourceRoles: Set[String] = Set.empty
    val secrets: Map[String, mesosphere.marathon.plugin.Secret] = Map.empty
    val networks: Seq[mesosphere.marathon.plugin.NetworkSpec] = Nil
    val volumes: Seq[mesosphere.marathon.plugin.VolumeSpec] = Nil
    val volumeMounts: Seq[VolumeMountSpec] = Nil
  }


  test("test") {
    val plugin = new MarathonSecretsPlugin()

    plugin.initialize(Map.empty, config)

    val builder = TaskInfo.newBuilder()

    // plugin.taskInfo(appSpec, builder)
  }

}
