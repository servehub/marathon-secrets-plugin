package servehub.marathon.secures.plugin

import java.io.ByteArrayInputStream
import java.nio.file.Files
import scala.sys.process._
import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpRequest}

import mesosphere.marathon.plugin.plugin.PluginConfiguration
import mesosphere.marathon.plugin.task._
import mesosphere.marathon.plugin.{ApplicationSpec, PodSpec}
import org.apache.commons.codec.binary.Base64
import org.apache.mesos.Protos
import org.apache.mesos.Protos.{ExecutorInfo, TaskGroupInfo, TaskInfo}
import org.slf4j.LoggerFactory
import play.api.libs.json._


class SecureVarsPlugin extends RunSpecTaskProcessor with PluginConfiguration {

  private val log = LoggerFactory.getLogger(getClass.getName)

  private[plugin] var httpClient: HttpRequest = _
  private[plugin] var privateKey: String = ""
  private[plugin] var varPrefix: String = ""
  private[plugin] var env: String = ""

  implicit val targetReaders = Json.reads[Target]
  implicit val secretReaders = Json.reads[Secret]

  def initialize(marathonInfo: Map[String, Any], configuration: JsObject): Unit = {
    httpClient = Http((configuration \ "secretsUrl").as[String].trim)
    privateKey = (configuration \ "privateKey").as[String].trim
    varPrefix  = (configuration \ "varPrefix").as[String].trim
    env        = (configuration \ "env").as[String].trim
  }

  def taskInfo(appSpec: ApplicationSpec, builder: TaskInfo.Builder): Unit = {
    val appId = appSpec.id.toString
    val resp = httpClient.asString

    val envBuilder = builder.getCommand.getEnvironment.toBuilder

    if (resp.is2xx) {
      val json = Json.parse(resp.body)

      (json \ "secrets").as[Map[String, Secret]] foreach { case (key, secret) ⇒
        if (secret.target.exists(_.app.exists(appId.startsWith))) {
          secret.value.get(env) foreach { encValue ⇒
            val tryValue =
              if (encValue.trim.take(4) equalsIgnoreCase "enc:") {
                SecureVarsPlugin.decrypt(privateKey, encValue.drop(4))
              } else {
                Success(encValue.trim)
              }

            tryValue match {
              case Success(value) ⇒
                envBuilder.addVariables(
                  Protos.Environment.Variable.newBuilder()
                    .setName((varPrefix + key.trim).toUpperCase.replaceAll("[^0-9A-Z]+", "_").toUpperCase)
                    .setValue(value))

              case Failure(e) ⇒
                log.error(s"Error on decrypt value: ${e.getMessage}", e)
            }

          }
        }
      }

      builder.setCommand(
        builder.getCommand.toBuilder
          .setEnvironment(envBuilder))
    } else {
      log.error(s"got unexpected response from secretsUrl $resp")
    }
  }

  def taskGroup(podSpec: PodSpec, executor: ExecutorInfo.Builder, taskGroup: TaskGroupInfo.Builder): Unit = {}
}


object SecureVarsPlugin {
  def decrypt(privateKey: String, value: String): Try[String] = Try {
    val privateFile = Files.createTempFile("msv", "secrets-key")

    try {
      Files.write(privateFile, Base64.decodeBase64(privateKey))
      (s"openssl smime -decrypt -inform pem -inkey $privateFile" #< new ByteArrayInputStream(Base64.decodeBase64(value))).lineStream.mkString
    } finally {
      Files.deleteIfExists(privateFile)
    }
  }
}


case class Secret(
  value: Map[String, String],
  target: List[Target]
)

case class Target(app: Option[String])
