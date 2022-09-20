package com.sbuslab.marathon.plugin

import java.net.{HttpURLConnection, URL}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.{KeyPairGenerator, SecureRandom, Security}

import javax.crypto.{AEADBadTagException, Cipher, SecretKey, SecretKeyFactory}
import javax.crypto.spec.{GCMParameterSpec, PBEKeySpec, SecretKeySpec}
import mesosphere.marathon.plugin.{ApplicationSpec, EnvVarString, PodSpec}
import mesosphere.marathon.plugin.plugin.PluginConfiguration
import mesosphere.marathon.plugin.task._
import net.i2p.crypto.eddsa.{EdDSAPrivateKey, EdDSAPublicKey, EdDSASecurityProvider, Utils}
import org.apache.mesos.Protos
import org.apache.mesos.Protos.{ExecutorInfo, TaskGroupInfo, TaskInfo}
import play.api.libs.json.{JsObject, Json}


class MarathonSecretsPlugin extends RunSpecTaskProcessor with PluginConfiguration {

  private var encryptKey: String = ""
  private var consulAddress: String = ""
  private var keysPath: String = ""
  private var identitiesPath: String = ""

  override def initialize(marathonInfo: Map[String, Any], configuration: JsObject): Unit = {
    encryptKey     = (configuration \ "encryptKey").as[String].trim
    consulAddress  = (configuration \ "consulAddress").as[String].trim
    keysPath       = (configuration \ "keysPath").asOpt[String].getOrElse("/v1/kv/services/keys").trim
    identitiesPath = (configuration \ "identitiesPath").asOpt[String].getOrElse("/v1/kv/services/sbus/identities").trim
  }

  Security.addProvider(new EdDSASecurityProvider)

  def taskInfo(appSpec: ApplicationSpec, builder: TaskInfo.Builder): Unit =
    appSpec.env.get("SERVICE_KEY") collect { case serviceId: EnvVarString ⇒
      require(appSpec.id.toString.startsWith("/" + serviceId.value), throw new Exception("Incorrect $SERVICE_KEY env variable!"))

      createIdentityIfMissing(serviceId)

      val privateKey: String = getOrCreatePrivateKey(serviceId)

      val envBuilder = builder.getCommand.getEnvironment.toBuilder

      envBuilder.addVariables(
        Protos.Environment.Variable.newBuilder()
          .setName("SERVICE_PRIVATE_KEY")
          .setValue(privateKey))

      builder.setCommand(
        builder.getCommand.toBuilder
          .setEnvironment(envBuilder))
    }

  private def createIdentityIfMissing(serviceId: EnvVarString) = {
    val resp = (new URL(consulAddress + identitiesPath + "/" + serviceId.value + "?raw=true").openConnection()).asInstanceOf[HttpURLConnection]

    if (resp.getResponseCode == 404) {
      consulPut(consulAddress + identitiesPath + "/" + serviceId.value, Json.toJson(List("service")).toString())
    }
  }

  private def getOrCreatePrivateKey(serviceId: EnvVarString) = {
    val resp = (new URL(consulAddress + keysPath + "/private/" + serviceId.value + "?raw=true").openConnection()).asInstanceOf[HttpURLConnection]

    if (resp.getResponseCode == 404) {
      val generator = KeyPairGenerator.getInstance("EdDSA", "EdDSA")
      val pair = generator.generateKeyPair()

      consulPut(consulAddress + keysPath + "/public/" + serviceId.value, Json.toJson(Map("publicKey" → Utils.bytesToHex(pair.getPublic.asInstanceOf[EdDSAPublicKey].getAbyte))).toString())

      val privateSeed = Utils.bytesToHex(pair.getPrivate.asInstanceOf[EdDSAPrivateKey].getSeed)

      consulPut(consulAddress + keysPath + "/private/" + serviceId.value, Json.toJson(Map("privateKey" → AesPbkdf2.encrypt(encryptKey, privateSeed))).toString())

      privateSeed
    } else {
      AesPbkdf2.decrypt(encryptKey, (Json.parse(resp.getInputStream) \ "privateKey").as[String])
    }
  }

  private def consulPut(url: String, body: String): Int = {
    val conn1 = (new URL(url).openConnection()).asInstanceOf[HttpURLConnection]
    conn1.setRequestMethod("PUT")
    conn1.setDoOutput(true)

    val bytes = body.getBytes("utf-8")
    val os = conn1.getOutputStream
    os.write(bytes, 0, bytes.length)

    conn1.getResponseCode
  }

  def taskGroup(podSpec: PodSpec, executor: ExecutorInfo.Builder, taskGroup: TaskGroupInfo.Builder): Unit = {}


  object AesPbkdf2 {
    private val VERSION: Integer = 1
    private val SALT_SIZE: Int   = 16
    private val IV_SIZE: Int     = 12
    private val ITERATIONS: Int  = 200000

    def encrypt(secret: String, text: String): String = try {
      val salt: Array[Byte] = new Array[Byte](SALT_SIZE)
      val random: SecureRandom = SecureRandom.getInstanceStrong
      random.nextBytes(salt)

      val key: SecretKey = new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(secret.toCharArray, salt, ITERATIONS, 256)).getEncoded, "AES")
      val iv: Array[Byte] = new Array[Byte](IV_SIZE)
      random.nextBytes(iv)

      val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv))

      val encrypted: Array[Byte] = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8))
      val buf: ByteBuffer = ByteBuffer.allocate(1 + SALT_SIZE + IV_SIZE + encrypted.length)
      buf.put(VERSION.byteValue).put(salt).put(iv).put(encrypted)

      java.util.Base64.getEncoder.encodeToString(buf.array)
    } catch {
      case e: Exception ⇒
        throw new RuntimeException(e)
    }

    def decrypt(secret: String, data: String): String = try {
      val encrypted: Array[Byte] = java.util.Base64.getDecoder.decode(data)

      val version: Int = java.util.Arrays.copyOfRange(encrypted, 0, 1)(0)
      if (version != VERSION) throw new RuntimeException("Unknown encryption version")

      val salt: Array[Byte] = java.util.Arrays.copyOfRange(encrypted, 1, 1 + SALT_SIZE)
      val iv: Array[Byte] = java.util.Arrays.copyOfRange(encrypted, 1 + SALT_SIZE, 1 + SALT_SIZE + IV_SIZE)
      val key: SecretKey = new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(secret.toCharArray, salt, ITERATIONS, 256)).getEncoded, "AES")

      val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv))

      val decrypted: Array[Byte] = cipher.doFinal(java.util.Arrays.copyOfRange(encrypted, 1 + SALT_SIZE + IV_SIZE, encrypted.length))

      new String(decrypted, StandardCharsets.UTF_8)
    } catch {
      case e: AEADBadTagException ⇒
        throw new RuntimeException("AES: incorrect password", e)

      case e: Exception ⇒
        throw new RuntimeException(e)
    }
  }
}
