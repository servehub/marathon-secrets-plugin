package servehub.marathon.plugin

import scala.collection.JavaConversions._
import scala.collection.immutable
import scala.util.Success
import scalaj.http.{HttpRequest, HttpResponse}

import mesosphere.marathon.plugin.{ApplicationSpec, EnvVarValue, PathId}
import org.apache.mesos.Protos.TaskInfo
import org.mockito.Mockito._
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json._


class MarathonSecretsPluginSpec extends FreeSpec with MustMatchers {

  private val config = Json.parse("""
    {
      "varPrefix": "SECRET_",
      "privateKey": "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlKS1FJQkFBS0NBZ0VBMW9Od0l1Z1lsdEJaMnYycVhCNzZ2YW11VlVYU0ZkamdwUkd2VnBjODlVOU91OHdBCndGV3MrQWhhSDhmTkZsZVgvNE9zNEUzTDJUNWg1dUdRMDNROUROTmlLdWh0bDVZdVMvUXhyOHRTd3hYaTRFdUYKVTJmZmIyc1hLVDNZSWk5cHVBejFUUnVCVHVlMzV2cnUyampROUtyeXRoUC92eVlpQjNJaUQ0c0lITFlFNmhrVApyN2w3N3EzR2FldGFORHcxWWNoaWg5c0xhQTQrWGtLK3ZVVkpJY08vT0JFL21mM09NalZHRkM5QU1ZNkIxZWtNClFLa21RaHpJYlEweDlNK2FpQkxhK2pOaFZMVHhYYStncTZOa1Qxd3EwZktKdk9hT05VQW05NXlKRFZDZDVTclUKUmFaL0oyMEU0WWRwTmVKZ3hkdmhhdjVqQ0VhbDhObGJiUlZtbG5DM3o5M3VMbEFvb0x2bWREdzNvL2FQcVhSLwpFQzRPdzJQYW1sQkxXdTZxZm5temRrd004blliR0s5YUdrU3lBLzRVL3FYTUJacEZENW9nQlM3RStHb2doeWVwCkNvaHc1M0wvMmtpWE5ybVFRaVJkQ0JveFRIdW1TYnFwSlQxN0g0cXJ4VjFaMVZ4amNWRVhSVU5SRjkwWXBjOFIKSDBtZW5vQnQwYjY4Tkp3Zlp2V2FxOUx1TkYxRGVGM2l1VkFBald6NjFyVzVlK29BTkhmaGxNY2txZXNHL2ZIZgpHejJMak9URmt2TktnU0lRa3JKOENJOW1WUVczOVRhWmxUbDQ3bzdTWnYwQkkvMFowbHhjRHdjUC80QVZ6L0JwCm11TlliNkNTUUZPZ3BxTE56cUJXcVRBWThYbENHeGdPekNkemZUSFZUOFgvTHRFMCtCem9mbGZCTzgwQ0F3RUEKQVFLQ0FnQkV4QlBoak1lOUtRTjFHVlpRZTduanJRNXUxWmdZV3RNQTk1OWV3ZExQek9HdmdDRkpodTJSQVpScQpsYkZnRlNwcnhibFltbmZicDR6WTU3eDJuTStwV2VZaFhTc3NxZjhqYUIrNWZCT0c2SEEzZU40L3M0L0Q5dStRCnovdUdVZTZ4Q3E1a1RvbndVb1g0ckw1L0JER3dEVkVMSDNRZndjaXMzR0NnM08vM3pEM2RFYmJtNFM0aWdHQ1gKdWRQTDVQVXo2SG4xbFRtZDNOc1BlcFFNdHJaUDlhRktCdlY5bHdoUHZoWStDRHVxNTBVbDNyU0gwZkdxeGZGVwpUQ0dVbWl5dXJHUzRTbHBNUGtrVndHUmdRVnU1dVJFZEJvUXpJekc1VGx4dmlpWEJOL08xOERubUxnNmJBKzdKCm9aQVByd2RZeGprSUJJNllxNVVzbFhFaWtmYTFjdExPdlpnUjhtS1BnNVF0WlNYWWNkblo4MWpRMW93RmZjZ0IKbVhKVlpTeDV3TjJwbDduemVlL2tWdGlQYjlrYURyNVNvbktHcGhVd0xKTDRJUFQrQ3NtYXdlZEZDcEEyemg1Tgp3TFdBelY3ekhSK0hjNFV0Sm1HeSt1d3hxUys3Q25rKzIzNkh2bU9DS05YcFFKaHo3eGxiRTdSamFSbk5XYXVCCmgyYkkrY1doWVVBVU03aDZLSVBGVG1ubFZIVzJoS0xaQmRZWEdmZEd4aWp0U1NVVFFnZUNqcnM3VldCRENvaEkKZGZLZ25VaGhoRy9jMzVTSlBRcyt6RlRFOXo5S0tSTmF1K0gvVkljbWVjeGdmbWw4WXJSZTJ5M2hya1Y0bnZPNQp4OENhL1oycC8xbTFnSGF1Q0I3M3ZtOGJoQThyYVZMTHErcEo4TEdjVkRXaHpSVkZuUUtDQVFFQStoREkwK09aCjZqN3VXQlpMMDRIYWl5YXB3RHVpaDNhYk5QcnB0VEZtaWQrOVZGdUVieXpCMmdLM2pIQUpJSmlsaEEwZkpieTIKK1d1bm52dUtNNjR3d2gxalpDT2NIKzZWRjk3MzdKODBQMXJnbUlLdC84UWErY3U4UlNkVUo3Sy9yVWNxeWxUdQpuc3BXbGdJTFJpYUszRk45N3JoS3ZMVC8xZzZUT3dVQXprdVhsdkgwUmgxanpoQW1aZ0NaL1ozaGhDdC9lTWRQCmFQZEdvTnZ5cnFiT3VXT0w5SVJ2WmoxUDJLd3d2bmFJdWZnR0YySEVtWWhmY1pxczBNOFJxaTYyc1FKSnlqd1cKNDJHYnlibHJBNDh6Y213dlJCL2dlSnFkYlBCcDlDeWlQTkMrVUxGaEo5YUlLWkdsTlZQSFgyR2l3a05PcUxNRwpTb2pYb1pKM3FlNmFBd0tDQVFFQTI1cXFMc1VkL1FwWHBRVXAvVGlXbVR3N2prS3JxMVlOTHRmbUd4VjhEdU0yCkR5bExSZ0RrZzZLTGIwR1QrQmR4NXVDQ2xBcXAwOWkxWDFvdk5ydjgwc0FRdjZKQUJXRUFzdE1MMzg5ajFzNnEKWnFyN0d6dzhyNzdIS1NvVFo2QlV1Wm4xZ1ByeGJaWXNXOUZYWmVDd2hDd2hsb2tGaEZzZXM5RS9QQXppYkpISgoyZTZDTkprdHBJTnFnZFNBbXNQc0cvaXFSYkdSZ2NZSkFmcUtCMmRMNGNFNENKMnRXMVo0ZWkxQ0FwWk9YTXpiClhJTzJmeFNHN2UwT05MME95Y25IMnVzMFJqaDkzbHYrd0xXbFo4YWIzdHl0VUlsR3A3b1J3VHVZSE5ITVMzY24KQ1ZQK3p4aWZRZ0ZaRE9ydWhGd3dsRjRGcWI3R1Nld1RKejcrN3NIUjd3S0NBUUVBb25wQllwdGhxRGZYZVZpVApVQXZ4U2JTSFd5WGtSb3dqOEZxUGUzRnJwRzNCZ0l3dUVtWk1WakNwdmhBVmZoNXdmTXBoMEkwN2ZmZ2E0MHVnCjBrOTkwc0p4aitFclVmd2MxM1BDTnZhQ1poL1FDb2Z1TWw3akFDY0dYeEJjdDZhR3NEbXJiZWVkVzRNd215c2MKMUNtNjA4bFQ2OFhHbWJJcEFKWmJ4MTlLMUw5ODhWQ29YTjQvdU9YNWJSRTYvUFNXVU9CS1pObHJtVklCakFjZApPYW1jYkF4b0pTbmZqOWlLaHNmcXEvWXNoRmpJN0d4VDUwWWFiYUFJWld3Rm1FUGQ1RWtPVk42U0tJK0ZQNTdBCkQ0SDk5WHloZ1d3TTE0L3VkWmdIMytVWVROZDhzTVZ2MFFpdFZBVVVDUUEwYko3dHQ3Y3Y1cURibDVZdU5RY0wKeFZnSXJRS0NBUUVBdG9SY2IxVExNTlVZdFFHT3ZYUDd6czM5M2FYUjBpeEo5cVhnREtDVnBEWDVaeTUxN20rdApmUzVxWHdTTSs4UlltWC85WnhkWndNVzdNTTNlc2NvYVBmcjlzSmdrUjVRd3BXKzh6YjRyaDR2cW1qdi9TY3FOCjBhOXBEZkIzZ2tnQ1F1UVA1S3dzWGwxUzd0a1ZuNG5ZaTNHMlZNdjdOdDhZckhENzhtZnZCRGRESVlzVzBxcUIKQWR0cmVRNXArTGRmbEh2ZTROL05SS09ZTnZuSklKQWN4RHYzMjBsWm5MRklucHJnNHcrVGY1T1B1ZHVLb1NJcApaclFBZTVxcUlOSkE5ZFZJZEJoZG5LS3ZIL0dZMEtDRVNmejFXeHROQ3ZnZDY1RWhRc3FuMWd6bzk5ckV5OUQyCkJFRGtoMVQyK2pFNCt3Y09BOStZSkRZK1VQOE5tTmlrSndLQ0FRQS96OHE2RWpsOEhUQkhnbzBQWVFSUkQ2ZFgKcWU3dytIbkI1WmhUQTJRUWpJOStUbmZDQ1dsMFFmN1NnbVFIcm1vSzhadHBOTmtzRFNIdndrRXg1d1g4OXd4Kwoyb1lKTHQ4bldpZ1VMM1dCbnBZbzdCazliZWtvcktGTTZiRnBwTUVTMTJzT0ZSSk5WWmw4Q1hrczUvUlkwSHZTCnVsc0ZLdkhSKzJyNmxIWjg0cWgrODBvZmlSTjRNQ0pZK2dyd2ZRZ09PU2g1S3ZGZjF1R3NYYnB5RDc3Z2xMR3YKSTlKK3V5VzN6QmdoRlBlamtJRDdrZ2VBWXZzRlIwNW9UNmo1a3dYeUZmeGYxU1BNMEk0eFJHK2k5UjkyNG9DVwpCbE5tR3RaMXB2bUlaN1FGOUY0b0ZuMmRWTlZnNUNXSXB5aUJpUlpjRk1UOG80bU9hRW1SWDQ0NlZ4T28KLS0tLS1FTkQgUlNBIFBSSVZBVEUgS0VZLS0tLS0K",
      "secretsUrl": "http://127.0.0.1:8500/v1/kv/services/secrets?raw=true"
    }
  """).as[JsObject]

  val runSpec: ApplicationSpec = new ApplicationSpec {
    val user: Option[String] = Some("root")
    val env: Map[String, EnvVarValue] = Map.empty
    val labels: Map[String, String] = Map.empty
    val id: PathId = new PathId {
      def path: immutable.Seq[String] = immutable.Seq("some", "test", "application-v1.2.4")
      override def toString = "/some/test/application-v1.2.4"
    }
    val acceptedResourceRoles: Set[String] = Set.empty
    val secrets: Map[String, mesosphere.marathon.plugin.Secret] = Map.empty
  }

  "initialization with a configuration" in {
    val plugin = new MarathonSecretsPlugin()
    plugin.initialize(Map.empty, config)

    plugin.privateKey mustBe "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlKS1FJQkFBS0NBZ0VBMW9Od0l1Z1lsdEJaMnYycVhCNzZ2YW11VlVYU0ZkamdwUkd2VnBjODlVOU91OHdBCndGV3MrQWhhSDhmTkZsZVgvNE9zNEUzTDJUNWg1dUdRMDNROUROTmlLdWh0bDVZdVMvUXhyOHRTd3hYaTRFdUYKVTJmZmIyc1hLVDNZSWk5cHVBejFUUnVCVHVlMzV2cnUyampROUtyeXRoUC92eVlpQjNJaUQ0c0lITFlFNmhrVApyN2w3N3EzR2FldGFORHcxWWNoaWg5c0xhQTQrWGtLK3ZVVkpJY08vT0JFL21mM09NalZHRkM5QU1ZNkIxZWtNClFLa21RaHpJYlEweDlNK2FpQkxhK2pOaFZMVHhYYStncTZOa1Qxd3EwZktKdk9hT05VQW05NXlKRFZDZDVTclUKUmFaL0oyMEU0WWRwTmVKZ3hkdmhhdjVqQ0VhbDhObGJiUlZtbG5DM3o5M3VMbEFvb0x2bWREdzNvL2FQcVhSLwpFQzRPdzJQYW1sQkxXdTZxZm5temRrd004blliR0s5YUdrU3lBLzRVL3FYTUJacEZENW9nQlM3RStHb2doeWVwCkNvaHc1M0wvMmtpWE5ybVFRaVJkQ0JveFRIdW1TYnFwSlQxN0g0cXJ4VjFaMVZ4amNWRVhSVU5SRjkwWXBjOFIKSDBtZW5vQnQwYjY4Tkp3Zlp2V2FxOUx1TkYxRGVGM2l1VkFBald6NjFyVzVlK29BTkhmaGxNY2txZXNHL2ZIZgpHejJMak9URmt2TktnU0lRa3JKOENJOW1WUVczOVRhWmxUbDQ3bzdTWnYwQkkvMFowbHhjRHdjUC80QVZ6L0JwCm11TlliNkNTUUZPZ3BxTE56cUJXcVRBWThYbENHeGdPekNkemZUSFZUOFgvTHRFMCtCem9mbGZCTzgwQ0F3RUEKQVFLQ0FnQkV4QlBoak1lOUtRTjFHVlpRZTduanJRNXUxWmdZV3RNQTk1OWV3ZExQek9HdmdDRkpodTJSQVpScQpsYkZnRlNwcnhibFltbmZicDR6WTU3eDJuTStwV2VZaFhTc3NxZjhqYUIrNWZCT0c2SEEzZU40L3M0L0Q5dStRCnovdUdVZTZ4Q3E1a1RvbndVb1g0ckw1L0JER3dEVkVMSDNRZndjaXMzR0NnM08vM3pEM2RFYmJtNFM0aWdHQ1gKdWRQTDVQVXo2SG4xbFRtZDNOc1BlcFFNdHJaUDlhRktCdlY5bHdoUHZoWStDRHVxNTBVbDNyU0gwZkdxeGZGVwpUQ0dVbWl5dXJHUzRTbHBNUGtrVndHUmdRVnU1dVJFZEJvUXpJekc1VGx4dmlpWEJOL08xOERubUxnNmJBKzdKCm9aQVByd2RZeGprSUJJNllxNVVzbFhFaWtmYTFjdExPdlpnUjhtS1BnNVF0WlNYWWNkblo4MWpRMW93RmZjZ0IKbVhKVlpTeDV3TjJwbDduemVlL2tWdGlQYjlrYURyNVNvbktHcGhVd0xKTDRJUFQrQ3NtYXdlZEZDcEEyemg1Tgp3TFdBelY3ekhSK0hjNFV0Sm1HeSt1d3hxUys3Q25rKzIzNkh2bU9DS05YcFFKaHo3eGxiRTdSamFSbk5XYXVCCmgyYkkrY1doWVVBVU03aDZLSVBGVG1ubFZIVzJoS0xaQmRZWEdmZEd4aWp0U1NVVFFnZUNqcnM3VldCRENvaEkKZGZLZ25VaGhoRy9jMzVTSlBRcyt6RlRFOXo5S0tSTmF1K0gvVkljbWVjeGdmbWw4WXJSZTJ5M2hya1Y0bnZPNQp4OENhL1oycC8xbTFnSGF1Q0I3M3ZtOGJoQThyYVZMTHErcEo4TEdjVkRXaHpSVkZuUUtDQVFFQStoREkwK09aCjZqN3VXQlpMMDRIYWl5YXB3RHVpaDNhYk5QcnB0VEZtaWQrOVZGdUVieXpCMmdLM2pIQUpJSmlsaEEwZkpieTIKK1d1bm52dUtNNjR3d2gxalpDT2NIKzZWRjk3MzdKODBQMXJnbUlLdC84UWErY3U4UlNkVUo3Sy9yVWNxeWxUdQpuc3BXbGdJTFJpYUszRk45N3JoS3ZMVC8xZzZUT3dVQXprdVhsdkgwUmgxanpoQW1aZ0NaL1ozaGhDdC9lTWRQCmFQZEdvTnZ5cnFiT3VXT0w5SVJ2WmoxUDJLd3d2bmFJdWZnR0YySEVtWWhmY1pxczBNOFJxaTYyc1FKSnlqd1cKNDJHYnlibHJBNDh6Y213dlJCL2dlSnFkYlBCcDlDeWlQTkMrVUxGaEo5YUlLWkdsTlZQSFgyR2l3a05PcUxNRwpTb2pYb1pKM3FlNmFBd0tDQVFFQTI1cXFMc1VkL1FwWHBRVXAvVGlXbVR3N2prS3JxMVlOTHRmbUd4VjhEdU0yCkR5bExSZ0RrZzZLTGIwR1QrQmR4NXVDQ2xBcXAwOWkxWDFvdk5ydjgwc0FRdjZKQUJXRUFzdE1MMzg5ajFzNnEKWnFyN0d6dzhyNzdIS1NvVFo2QlV1Wm4xZ1ByeGJaWXNXOUZYWmVDd2hDd2hsb2tGaEZzZXM5RS9QQXppYkpISgoyZTZDTkprdHBJTnFnZFNBbXNQc0cvaXFSYkdSZ2NZSkFmcUtCMmRMNGNFNENKMnRXMVo0ZWkxQ0FwWk9YTXpiClhJTzJmeFNHN2UwT05MME95Y25IMnVzMFJqaDkzbHYrd0xXbFo4YWIzdHl0VUlsR3A3b1J3VHVZSE5ITVMzY24KQ1ZQK3p4aWZRZ0ZaRE9ydWhGd3dsRjRGcWI3R1Nld1RKejcrN3NIUjd3S0NBUUVBb25wQllwdGhxRGZYZVZpVApVQXZ4U2JTSFd5WGtSb3dqOEZxUGUzRnJwRzNCZ0l3dUVtWk1WakNwdmhBVmZoNXdmTXBoMEkwN2ZmZ2E0MHVnCjBrOTkwc0p4aitFclVmd2MxM1BDTnZhQ1poL1FDb2Z1TWw3akFDY0dYeEJjdDZhR3NEbXJiZWVkVzRNd215c2MKMUNtNjA4bFQ2OFhHbWJJcEFKWmJ4MTlLMUw5ODhWQ29YTjQvdU9YNWJSRTYvUFNXVU9CS1pObHJtVklCakFjZApPYW1jYkF4b0pTbmZqOWlLaHNmcXEvWXNoRmpJN0d4VDUwWWFiYUFJWld3Rm1FUGQ1RWtPVk42U0tJK0ZQNTdBCkQ0SDk5WHloZ1d3TTE0L3VkWmdIMytVWVROZDhzTVZ2MFFpdFZBVVVDUUEwYko3dHQ3Y3Y1cURibDVZdU5RY0wKeFZnSXJRS0NBUUVBdG9SY2IxVExNTlVZdFFHT3ZYUDd6czM5M2FYUjBpeEo5cVhnREtDVnBEWDVaeTUxN20rdApmUzVxWHdTTSs4UlltWC85WnhkWndNVzdNTTNlc2NvYVBmcjlzSmdrUjVRd3BXKzh6YjRyaDR2cW1qdi9TY3FOCjBhOXBEZkIzZ2tnQ1F1UVA1S3dzWGwxUzd0a1ZuNG5ZaTNHMlZNdjdOdDhZckhENzhtZnZCRGRESVlzVzBxcUIKQWR0cmVRNXArTGRmbEh2ZTROL05SS09ZTnZuSklKQWN4RHYzMjBsWm5MRklucHJnNHcrVGY1T1B1ZHVLb1NJcApaclFBZTVxcUlOSkE5ZFZJZEJoZG5LS3ZIL0dZMEtDRVNmejFXeHROQ3ZnZDY1RWhRc3FuMWd6bzk5ckV5OUQyCkJFRGtoMVQyK2pFNCt3Y09BOStZSkRZK1VQOE5tTmlrSndLQ0FRQS96OHE2RWpsOEhUQkhnbzBQWVFSUkQ2ZFgKcWU3dytIbkI1WmhUQTJRUWpJOStUbmZDQ1dsMFFmN1NnbVFIcm1vSzhadHBOTmtzRFNIdndrRXg1d1g4OXd4Kwoyb1lKTHQ4bldpZ1VMM1dCbnBZbzdCazliZWtvcktGTTZiRnBwTUVTMTJzT0ZSSk5WWmw4Q1hrczUvUlkwSHZTCnVsc0ZLdkhSKzJyNmxIWjg0cWgrODBvZmlSTjRNQ0pZK2dyd2ZRZ09PU2g1S3ZGZjF1R3NYYnB5RDc3Z2xMR3YKSTlKK3V5VzN6QmdoRlBlamtJRDdrZ2VBWXZzRlIwNW9UNmo1a3dYeUZmeGYxU1BNMEk0eFJHK2k5UjkyNG9DVwpCbE5tR3RaMXB2bUlaN1FGOUY0b0ZuMmRWTlZnNUNXSXB5aUJpUlpjRk1UOG80bU9hRW1SWDQ0NlZ4T28KLS0tLS1FTkQgUlNBIFBSSVZBVEUgS0VZLS0tLS0K"
    plugin.varPrefix mustBe "SECRET_"
  }

  "decrypt values" in {
    val privateKey = """LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlKS1FJQkFBS0NBZ0VBMW9Od0l1Z1lsdEJaMnYycVhCNzZ2YW11VlVYU0ZkamdwUkd2VnBjODlVOU91OHdBCndGV3MrQWhhSDhmTkZsZVgvNE9zNEUzTDJUNWg1dUdRMDNROUROTmlLdWh0bDVZdVMvUXhyOHRTd3hYaTRFdUYKVTJmZmIyc1hLVDNZSWk5cHVBejFUUnVCVHVlMzV2cnUyampROUtyeXRoUC92eVlpQjNJaUQ0c0lITFlFNmhrVApyN2w3N3EzR2FldGFORHcxWWNoaWg5c0xhQTQrWGtLK3ZVVkpJY08vT0JFL21mM09NalZHRkM5QU1ZNkIxZWtNClFLa21RaHpJYlEweDlNK2FpQkxhK2pOaFZMVHhYYStncTZOa1Qxd3EwZktKdk9hT05VQW05NXlKRFZDZDVTclUKUmFaL0oyMEU0WWRwTmVKZ3hkdmhhdjVqQ0VhbDhObGJiUlZtbG5DM3o5M3VMbEFvb0x2bWREdzNvL2FQcVhSLwpFQzRPdzJQYW1sQkxXdTZxZm5temRrd004blliR0s5YUdrU3lBLzRVL3FYTUJacEZENW9nQlM3RStHb2doeWVwCkNvaHc1M0wvMmtpWE5ybVFRaVJkQ0JveFRIdW1TYnFwSlQxN0g0cXJ4VjFaMVZ4amNWRVhSVU5SRjkwWXBjOFIKSDBtZW5vQnQwYjY4Tkp3Zlp2V2FxOUx1TkYxRGVGM2l1VkFBald6NjFyVzVlK29BTkhmaGxNY2txZXNHL2ZIZgpHejJMak9URmt2TktnU0lRa3JKOENJOW1WUVczOVRhWmxUbDQ3bzdTWnYwQkkvMFowbHhjRHdjUC80QVZ6L0JwCm11TlliNkNTUUZPZ3BxTE56cUJXcVRBWThYbENHeGdPekNkemZUSFZUOFgvTHRFMCtCem9mbGZCTzgwQ0F3RUEKQVFLQ0FnQkV4QlBoak1lOUtRTjFHVlpRZTduanJRNXUxWmdZV3RNQTk1OWV3ZExQek9HdmdDRkpodTJSQVpScQpsYkZnRlNwcnhibFltbmZicDR6WTU3eDJuTStwV2VZaFhTc3NxZjhqYUIrNWZCT0c2SEEzZU40L3M0L0Q5dStRCnovdUdVZTZ4Q3E1a1RvbndVb1g0ckw1L0JER3dEVkVMSDNRZndjaXMzR0NnM08vM3pEM2RFYmJtNFM0aWdHQ1gKdWRQTDVQVXo2SG4xbFRtZDNOc1BlcFFNdHJaUDlhRktCdlY5bHdoUHZoWStDRHVxNTBVbDNyU0gwZkdxeGZGVwpUQ0dVbWl5dXJHUzRTbHBNUGtrVndHUmdRVnU1dVJFZEJvUXpJekc1VGx4dmlpWEJOL08xOERubUxnNmJBKzdKCm9aQVByd2RZeGprSUJJNllxNVVzbFhFaWtmYTFjdExPdlpnUjhtS1BnNVF0WlNYWWNkblo4MWpRMW93RmZjZ0IKbVhKVlpTeDV3TjJwbDduemVlL2tWdGlQYjlrYURyNVNvbktHcGhVd0xKTDRJUFQrQ3NtYXdlZEZDcEEyemg1Tgp3TFdBelY3ekhSK0hjNFV0Sm1HeSt1d3hxUys3Q25rKzIzNkh2bU9DS05YcFFKaHo3eGxiRTdSamFSbk5XYXVCCmgyYkkrY1doWVVBVU03aDZLSVBGVG1ubFZIVzJoS0xaQmRZWEdmZEd4aWp0U1NVVFFnZUNqcnM3VldCRENvaEkKZGZLZ25VaGhoRy9jMzVTSlBRcyt6RlRFOXo5S0tSTmF1K0gvVkljbWVjeGdmbWw4WXJSZTJ5M2hya1Y0bnZPNQp4OENhL1oycC8xbTFnSGF1Q0I3M3ZtOGJoQThyYVZMTHErcEo4TEdjVkRXaHpSVkZuUUtDQVFFQStoREkwK09aCjZqN3VXQlpMMDRIYWl5YXB3RHVpaDNhYk5QcnB0VEZtaWQrOVZGdUVieXpCMmdLM2pIQUpJSmlsaEEwZkpieTIKK1d1bm52dUtNNjR3d2gxalpDT2NIKzZWRjk3MzdKODBQMXJnbUlLdC84UWErY3U4UlNkVUo3Sy9yVWNxeWxUdQpuc3BXbGdJTFJpYUszRk45N3JoS3ZMVC8xZzZUT3dVQXprdVhsdkgwUmgxanpoQW1aZ0NaL1ozaGhDdC9lTWRQCmFQZEdvTnZ5cnFiT3VXT0w5SVJ2WmoxUDJLd3d2bmFJdWZnR0YySEVtWWhmY1pxczBNOFJxaTYyc1FKSnlqd1cKNDJHYnlibHJBNDh6Y213dlJCL2dlSnFkYlBCcDlDeWlQTkMrVUxGaEo5YUlLWkdsTlZQSFgyR2l3a05PcUxNRwpTb2pYb1pKM3FlNmFBd0tDQVFFQTI1cXFMc1VkL1FwWHBRVXAvVGlXbVR3N2prS3JxMVlOTHRmbUd4VjhEdU0yCkR5bExSZ0RrZzZLTGIwR1QrQmR4NXVDQ2xBcXAwOWkxWDFvdk5ydjgwc0FRdjZKQUJXRUFzdE1MMzg5ajFzNnEKWnFyN0d6dzhyNzdIS1NvVFo2QlV1Wm4xZ1ByeGJaWXNXOUZYWmVDd2hDd2hsb2tGaEZzZXM5RS9QQXppYkpISgoyZTZDTkprdHBJTnFnZFNBbXNQc0cvaXFSYkdSZ2NZSkFmcUtCMmRMNGNFNENKMnRXMVo0ZWkxQ0FwWk9YTXpiClhJTzJmeFNHN2UwT05MME95Y25IMnVzMFJqaDkzbHYrd0xXbFo4YWIzdHl0VUlsR3A3b1J3VHVZSE5ITVMzY24KQ1ZQK3p4aWZRZ0ZaRE9ydWhGd3dsRjRGcWI3R1Nld1RKejcrN3NIUjd3S0NBUUVBb25wQllwdGhxRGZYZVZpVApVQXZ4U2JTSFd5WGtSb3dqOEZxUGUzRnJwRzNCZ0l3dUVtWk1WakNwdmhBVmZoNXdmTXBoMEkwN2ZmZ2E0MHVnCjBrOTkwc0p4aitFclVmd2MxM1BDTnZhQ1poL1FDb2Z1TWw3akFDY0dYeEJjdDZhR3NEbXJiZWVkVzRNd215c2MKMUNtNjA4bFQ2OFhHbWJJcEFKWmJ4MTlLMUw5ODhWQ29YTjQvdU9YNWJSRTYvUFNXVU9CS1pObHJtVklCakFjZApPYW1jYkF4b0pTbmZqOWlLaHNmcXEvWXNoRmpJN0d4VDUwWWFiYUFJWld3Rm1FUGQ1RWtPVk42U0tJK0ZQNTdBCkQ0SDk5WHloZ1d3TTE0L3VkWmdIMytVWVROZDhzTVZ2MFFpdFZBVVVDUUEwYko3dHQ3Y3Y1cURibDVZdU5RY0wKeFZnSXJRS0NBUUVBdG9SY2IxVExNTlVZdFFHT3ZYUDd6czM5M2FYUjBpeEo5cVhnREtDVnBEWDVaeTUxN20rdApmUzVxWHdTTSs4UlltWC85WnhkWndNVzdNTTNlc2NvYVBmcjlzSmdrUjVRd3BXKzh6YjRyaDR2cW1qdi9TY3FOCjBhOXBEZkIzZ2tnQ1F1UVA1S3dzWGwxUzd0a1ZuNG5ZaTNHMlZNdjdOdDhZckhENzhtZnZCRGRESVlzVzBxcUIKQWR0cmVRNXArTGRmbEh2ZTROL05SS09ZTnZuSklKQWN4RHYzMjBsWm5MRklucHJnNHcrVGY1T1B1ZHVLb1NJcApaclFBZTVxcUlOSkE5ZFZJZEJoZG5LS3ZIL0dZMEtDRVNmejFXeHROQ3ZnZDY1RWhRc3FuMWd6bzk5ckV5OUQyCkJFRGtoMVQyK2pFNCt3Y09BOStZSkRZK1VQOE5tTmlrSndLQ0FRQS96OHE2RWpsOEhUQkhnbzBQWVFSUkQ2ZFgKcWU3dytIbkI1WmhUQTJRUWpJOStUbmZDQ1dsMFFmN1NnbVFIcm1vSzhadHBOTmtzRFNIdndrRXg1d1g4OXd4Kwoyb1lKTHQ4bldpZ1VMM1dCbnBZbzdCazliZWtvcktGTTZiRnBwTUVTMTJzT0ZSSk5WWmw4Q1hrczUvUlkwSHZTCnVsc0ZLdkhSKzJyNmxIWjg0cWgrODBvZmlSTjRNQ0pZK2dyd2ZRZ09PU2g1S3ZGZjF1R3NYYnB5RDc3Z2xMR3YKSTlKK3V5VzN6QmdoRlBlamtJRDdrZ2VBWXZzRlIwNW9UNmo1a3dYeUZmeGYxU1BNMEk0eFJHK2k5UjkyNG9DVwpCbE5tR3RaMXB2bUlaN1FGOUY0b0ZuMmRWTlZnNUNXSXB5aUJpUlpjRk1UOG80bU9hRW1SWDQ0NlZ4T28KLS0tLS1FTkQgUlNBIFBSSVZBVEUgS0VZLS0tLS0K"""

    val result = MarathonSecretsPlugin.decrypt(privateKey, "LS0tLS1CRUdJTiBQS0NTNy0tLS0tCk1JSUNpUVlKS29aSWh2Y05BUWNEb0lJQ2VqQ0NBbllDQVFBeGdnSTZNSUlDTmdJQkFEQWVNQkV4RHpBTkJnTlYKQkFNVUJsQkxRMU1qTndJSkFNWnRZRm1tUlNBck1BMEdDU3FHU0liM0RRRUJBUVVBQklJQ0FDVkNqcHRWaGIzQQo4aDBUS2ZwTVVFQWsxRXdGVkZGWGNqSVUrN1VlZ2J0Q1JjNXVXNGJpMThEcktHQ1VuVEovOTRrWXYxeHVpcUUzCkgySHpmTEEvaUhkMmdtK2tMdFo0Nmx2bDlxM3cwcWNmR2dIL2EvejFwTWF5Z2pKc01leWk4aCtwRldlUEpQYmcKVjlrU2JMYVhnQ1Q4Rjg3aWFZR3NDS0dQMFEvQ1d0YWhnUU50MTlXdWlxS2VhQjBvN0dGdms5Z01OYU0xL2FJeApWRE5LQ2ppejFBSjhiZnZkVTh2dS9JTEg5K3RXRmxWNHdnU2w4QWNKQithanNXYitvRWI0Y3YxV1FnQkppcEYwCjEyUDIraExCcVdzS3oxbFk4bTlBOXRZd2k0Z3Qxa1h1UWxhbTkxL0tPL2RYQVJUa3hxNkRFQ1VTZUcxUU1MSG8KRUQveGdzSjRPdmVkMENoYldvWFJIdWdmS2hPYjR6L0lZRzlpaDVaUkRFbmxGNlNEaW1PN09nNy9iZWU4ODJicgoxRTdOb0Yvc1gxK1VTRHBoUVFleVRmNytnL2FDUE1tV1NuRmtza3JMVzlwOVJtRGdMamkxOWhYaGNQZW9qcGNCCkNkR3FlcmZSVm0raTk3dnBMVVE1S3pmejk4ZFJVdFRNSmlacWNUMlVZQ2NSWS9VcVhNczdDdk1CVTlVdWYxVFUKTFBzekV4cGpKZnY3Wk4vd0NXYkhWNzZVVVROaUoxRDhHVHpPQjh0MFM5b25ESzc5UmNNNFVzZXRaVVQzRGdOaQpkMk8zZWdySHNtQUNBTTJZekN6ZzZVa056TnQ5M2tDQnByN0crY0ZMNUtNSU9aQmNUNmxhOUJiY2NNTUVTNUhmCjZsWW9JUUhlSVU4cDZ6NmZkNVNrcnp0ZlB2a3NBZlhNTURNR0NTcUdTSWIzRFFFSEFUQVVCZ2dxaGtpRzl3MEQKQndRSWZoZ3lIaWtBZWhPQUVEMVF3OElWbnpPdE0vQnozUVFpR2Z3PQotLS0tLUVORCBQS0NTNy0tLS0tCg==")

    result mustBe Success("kulikov-test")
  }

  "applying the plugin" in {
    val plugin = new MarathonSecretsPlugin()
    plugin.initialize(Map.empty, config)

    plugin.httpClient = mock(classOf[HttpRequest])

    when(plugin.httpClient.asString).thenReturn(HttpResponse[String]("""
      {
        "secrets": {
          "some.test.value": {
            "value": "LS0tLS1CRUdJTiBQS0NTNy0tLS0tCk1JSUNpUVlKS29aSWh2Y05BUWNEb0lJQ2VqQ0NBbllDQVFBeGdnSTZNSUlDTmdJQkFEQWVNQkV4RHpBTkJnTlYKQkFNVUJsQkxRMU1qTndJSkFNWnRZRm1tUlNBck1BMEdDU3FHU0liM0RRRUJBUVVBQklJQ0FMUDVsZnVEQ1AvQQo1YjBRTmE0QWs1NEpGMHdMb2E4MnNnQkxZWHhBdkFOL2xLWUxPMnIzdUFVZm5JOHRPWnd1Z2JlK0FvOXY3Q2JyCmpic1JXT2dXdEVUbXRjb2tzdVJoZlVkaGtZSmppcFZlMmlhSGtzQVc4T2JCZXcxY3hjVjVlL3AwSUJPSjVrOVUKM1hLMndyNjJPMThqOHl0UXgzdUYvQVdndW5YaGk0WDZ3aWREVzYrUFRKY1doTy81dlR3by9udjBtQm9IQ2RXWApxZFlKNHUwN0E5TVZ3UmI5L3dtSENVUTFyakJGNXVId2UzblBLdFYwK2lCNU5WV0lvdEpoMnRoenZ6TzIwSVljCmhkWERQd3pUVVRhcmFpZVRVQXF2VVgyWko3MEVLMmFVdmNSVmpZcHExVjlubUZLcTFUT3pzYjFSRmRCYWhoZ1YKd0JiYWprNGdsWTBWVDM4Nm0yWEF5SFJQN1JqYm1EVmsrMUNjL1JxdVJ4bFVxYlpSN01veDhmQWRwYmtkM1FPQQpSV29XbFFqTDNUV0FOVjkybTRXTzVqNzFLbnNjNWRBUWFFSmJES0hDeFhNKzVXdzIrUi9mMS9jQVJ2Y2tBdTRaClBoVjl1R0F4dS9jZEQrQzdmQWhkRnhxWlVrOUlVdHk5eUgvVnJ4TDNJUmoyTWtQNTRtUU9GWkRkOVlQSk5VVW8KQUFYY2xUeXdmN1NlNk14WTR5Vm5xS0I5QmtiSXF0Tm5YUDBYRUFQRHAwclVSMWlVU1dOY3Y2VUZ2MkY1U2ZudApwcUdrb1BlS3lRV2hCZEVkSy81YWliVE5aOHlzUmpra0RSV0w1U3lvd0VHQXlsY21IL29PUGlIQXBGT1RDQjZWCitCRmhGRWhpYnlaMG9PYkFTejJkQlNHaU5yRk1aSERkTURNR0NTcUdTSWIzRFFFSEFUQVVCZ2dxaGtpRzl3MEQKQndRSXp3ZkJDTDkzR2dDQUVIZ09xR0t5MDl4T3VnSThlZVFvbEtNPQotLS0tLUVORCBQS0NTNy0tLS0tCg==",
            "target": [
              { "app": "/some/test/application-v1.2.4" }
            ]
          },
          "seconds.test.value": {
            "value": "ENC:LS0tLS1CRUdJTiBQS0NTNy0tLS0tCk1JSUNpUVlKS29aSWh2Y05BUWNEb0lJQ2VqQ0NBbllDQVFBeGdnSTZNSUlDTmdJQkFEQWVNQkV4RHpBTkJnTlYKQkFNVUJsQkxRMU1qTndJSkFNWnRZRm1tUlNBck1BMEdDU3FHU0liM0RRRUJBUVVBQklJQ0FMUDVsZnVEQ1AvQQo1YjBRTmE0QWs1NEpGMHdMb2E4MnNnQkxZWHhBdkFOL2xLWUxPMnIzdUFVZm5JOHRPWnd1Z2JlK0FvOXY3Q2JyCmpic1JXT2dXdEVUbXRjb2tzdVJoZlVkaGtZSmppcFZlMmlhSGtzQVc4T2JCZXcxY3hjVjVlL3AwSUJPSjVrOVUKM1hLMndyNjJPMThqOHl0UXgzdUYvQVdndW5YaGk0WDZ3aWREVzYrUFRKY1doTy81dlR3by9udjBtQm9IQ2RXWApxZFlKNHUwN0E5TVZ3UmI5L3dtSENVUTFyakJGNXVId2UzblBLdFYwK2lCNU5WV0lvdEpoMnRoenZ6TzIwSVljCmhkWERQd3pUVVRhcmFpZVRVQXF2VVgyWko3MEVLMmFVdmNSVmpZcHExVjlubUZLcTFUT3pzYjFSRmRCYWhoZ1YKd0JiYWprNGdsWTBWVDM4Nm0yWEF5SFJQN1JqYm1EVmsrMUNjL1JxdVJ4bFVxYlpSN01veDhmQWRwYmtkM1FPQQpSV29XbFFqTDNUV0FOVjkybTRXTzVqNzFLbnNjNWRBUWFFSmJES0hDeFhNKzVXdzIrUi9mMS9jQVJ2Y2tBdTRaClBoVjl1R0F4dS9jZEQrQzdmQWhkRnhxWlVrOUlVdHk5eUgvVnJ4TDNJUmoyTWtQNTRtUU9GWkRkOVlQSk5VVW8KQUFYY2xUeXdmN1NlNk14WTR5Vm5xS0I5QmtiSXF0Tm5YUDBYRUFQRHAwclVSMWlVU1dOY3Y2VUZ2MkY1U2ZudApwcUdrb1BlS3lRV2hCZEVkSy81YWliVE5aOHlzUmpra0RSV0w1U3lvd0VHQXlsY21IL29PUGlIQXBGT1RDQjZWCitCRmhGRWhpYnlaMG9PYkFTejJkQlNHaU5yRk1aSERkTURNR0NTcUdTSWIzRFFFSEFUQVVCZ2dxaGtpRzl3MEQKQndRSXp3ZkJDTDkzR2dDQUVIZ09xR0t5MDl4T3VnSThlZVFvbEtNPQotLS0tLUVORCBQS0NTNy0tLS0tCg==",
            "target": [
              { "app": "/some/test/" }
            ]
          },
          "third.test.value": {
            "value": "ENC:LS0tLS1CRUdJTiBQS0NTNy0tLS0tCk1JSUNpUVlKS29aSWh2Y05BUWNEb0lJQ2VqQ0NBbllDQVFBeGdnSTZNSUlDTmdJQkFEQWVNQkV4RHpBTkJnTlYKQkFNVUJsQkxRMU1qTndJSkFNWnRZRm1tUlNBck1BMEdDU3FHU0liM0RRRUJBUVVBQklJQ0FMUDVsZnVEQ1AvQQo1YjBRTmE0QWs1NEpGMHdMb2E4MnNnQkxZWHhBdkFOL2xLWUxPMnIzdUFVZm5JOHRPWnd1Z2JlK0FvOXY3Q2JyCmpic1JXT2dXdEVUbXRjb2tzdVJoZlVkaGtZSmppcFZlMmlhSGtzQVc4T2JCZXcxY3hjVjVlL3AwSUJPSjVrOVUKM1hLMndyNjJPMThqOHl0UXgzdUYvQVdndW5YaGk0WDZ3aWREVzYrUFRKY1doTy81dlR3by9udjBtQm9IQ2RXWApxZFlKNHUwN0E5TVZ3UmI5L3dtSENVUTFyakJGNXVId2UzblBLdFYwK2lCNU5WV0lvdEpoMnRoenZ6TzIwSVljCmhkWERQd3pUVVRhcmFpZVRVQXF2VVgyWko3MEVLMmFVdmNSVmpZcHExVjlubUZLcTFUT3pzYjFSRmRCYWhoZ1YKd0JiYWprNGdsWTBWVDM4Nm0yWEF5SFJQN1JqYm1EVmsrMUNjL1JxdVJ4bFVxYlpSN01veDhmQWRwYmtkM1FPQQpSV29XbFFqTDNUV0FOVjkybTRXTzVqNzFLbnNjNWRBUWFFSmJES0hDeFhNKzVXdzIrUi9mMS9jQVJ2Y2tBdTRaClBoVjl1R0F4dS9jZEQrQzdmQWhkRnhxWlVrOUlVdHk5eUgvVnJ4TDNJUmoyTWtQNTRtUU9GWkRkOVlQSk5VVW8KQUFYY2xUeXdmN1NlNk14WTR5Vm5xS0I5QmtiSXF0Tm5YUDBYRUFQRHAwclVSMWlVU1dOY3Y2VUZ2MkY1U2ZudApwcUdrb1BlS3lRV2hCZEVkSy81YWliVE5aOHlzUmpra0RSV0w1U3lvd0VHQXlsY21IL29PUGlIQXBGT1RDQjZWCitCRmhGRWhpYnlaMG9PYkFTejJkQlNHaU5yRk1aSERkTURNR0NTcUdTSWIzRFFFSEFUQVVCZ2dxaGtpRzl3MEQKQndRSXp3ZkJDTDkzR2dDQUVIZ09xR0t5MDl4T3VnSThlZVFvbEtNPQotLS0tLUVORCBQS0NTNy0tLS0tCg==",
            "target": [
              { "app": "/other/apps-1.4.5" }
            ]
          },
          "unexpected-value-format": {
            "value": "plain text value"
          },
          "key-without-targets": {
            "value": "ENC:LS0tLS1CRUdJTiBQS0NTNy0tLS0tCk1JSUNpUVlKS29aSWh2Y05BUWNEb0lJQ2VqQ0NBbllDQVFBeGdnSTZNSUlDTmdJQkFEQWVNQkV4RHpBTkJnTlYKQkFNVUJsQkxRMU1qTndJSkFNWnRZRm1tUlNBck1BMEdDU3FHU0liM0RRRUJBUVVBQklJQ0FMUDVsZnVEQ1AvQQo1YjBRTmE0QWs1NEpGMHdMb2E4MnNnQkxZWHhBdkFOL2xLWUxPMnIzdUFVZm5JOHRPWnd1Z2JlK0FvOXY3Q2JyCmpic1JXT2dXdEVUbXRjb2tzdVJoZlVkaGtZSmppcFZlMmlhSGtzQVc4T2JCZXcxY3hjVjVlL3AwSUJPSjVrOVUKM1hLMndyNjJPMThqOHl0UXgzdUYvQVdndW5YaGk0WDZ3aWREVzYrUFRKY1doTy81dlR3by9udjBtQm9IQ2RXWApxZFlKNHUwN0E5TVZ3UmI5L3dtSENVUTFyakJGNXVId2UzblBLdFYwK2lCNU5WV0lvdEpoMnRoenZ6TzIwSVljCmhkWERQd3pUVVRhcmFpZVRVQXF2VVgyWko3MEVLMmFVdmNSVmpZcHExVjlubUZLcTFUT3pzYjFSRmRCYWhoZ1YKd0JiYWprNGdsWTBWVDM4Nm0yWEF5SFJQN1JqYm1EVmsrMUNjL1JxdVJ4bFVxYlpSN01veDhmQWRwYmtkM1FPQQpSV29XbFFqTDNUV0FOVjkybTRXTzVqNzFLbnNjNWRBUWFFSmJES0hDeFhNKzVXdzIrUi9mMS9jQVJ2Y2tBdTRaClBoVjl1R0F4dS9jZEQrQzdmQWhkRnhxWlVrOUlVdHk5eUgvVnJ4TDNJUmoyTWtQNTRtUU9GWkRkOVlQSk5VVW8KQUFYY2xUeXdmN1NlNk14WTR5Vm5xS0I5QmtiSXF0Tm5YUDBYRUFQRHAwclVSMWlVU1dOY3Y2VUZ2MkY1U2ZudApwcUdrb1BlS3lRV2hCZEVkSy81YWliVE5aOHlzUmpra0RSV0w1U3lvd0VHQXlsY21IL29PUGlIQXBGT1RDQjZWCitCRmhGRWhpYnlaMG9PYkFTejJkQlNHaU5yRk1aSERkTURNR0NTcUdTSWIzRFFFSEFUQVVCZ2dxaGtpRzl3MEQKQndRSXp3ZkJDTDkzR2dDQUVIZ09xR0t5MDl4T3VnSThlZVFvbEtNPQotLS0tLUVORCBQS0NTNy0tLS0tCg=="
          }
        }
      }
    """, 200, Map.empty))

    val builder = TaskInfo.newBuilder()

    plugin.taskInfo(runSpec, builder)

    builder.getCommand.getEnvironment.getVariablesList.toList.map(v ⇒ v.getName → v.getValue).toMap mustBe Map(
      "SECRET_SOME_TEST_VALUE" → "kulikov-test",
      "SECRET_KEY_WITHOUT_TARGETS" → "kulikov-test",
      "SECRET_SECONDS_TEST_VALUE" → "kulikov-test"
    )
  }

  "applying for recurse consul kv response" in {
    val plugin = new MarathonSecretsPlugin()
    plugin.initialize(Map.empty, config)

    plugin.httpClient = mock(classOf[HttpRequest])

    when(plugin.httpClient.asString).thenReturn(HttpResponse[String]("""
      [
        {
          "CreateIndex": 10298502,
          "Flags": 0,
          "Key": "services/secrets",
          "LockIndex": 0,
          "ModifyIndex": 10298563,
          "Value":"ewogICAgICAgICJzZWNyZXRzIjogewogICAgICAgICAgIm9uZSI6IHsKICAgICAgICAgICAgInZhbHVlIjogIkxTMHRMUzFDUlVkSlRpQlFTME5UTnkwdExTMHRDazFKU1VOcFVWbEtTMjlhU1doMlkwNUJVV05FYjBsSlEyVnFRME5CYmxsRFFWRkJlR2RuU1RaTlNVbERUbWRKUWtGRVFXVk5Ra1Y0UkhwQlRrSm5UbFlLUWtGTlZVSnNRa3hSTVUxcVRuZEpTa0ZOV25SWlJtMXRVbE5CY2sxQk1FZERVM0ZIVTBsaU0wUlJSVUpCVVZWQlFrbEpRMEZNVURWc1puVkVRMUF2UVFvMVlqQlJUbUUwUVdzMU5FcEdNSGRNYjJFNE1uTm5Ra3haV0hoQmRrRk9MMnhMV1V4UE1uSXpkVUZWWm01Sk9IUlBXbmQxWjJKbEswRnZPWFkzUTJKeUNtcGljMUpYVDJkWGRFVlViWFJqYjJ0emRWSm9abFZrYUd0WlNtcHBjRlpsTW1saFNHdHpRVmM0VDJKQ1pYY3hZM2hqVmpWbEwzQXdTVUpQU2pWck9WVUtNMWhMTW5keU5qSlBNVGhxT0hsMFVYZ3pkVVl2UVZkbmRXNVlhR2swV0RaM2FXUkVWellyVUZSS1kxZG9UeTgxZGxSM2J5OXVkakJ0UW05SVEyUlhXQXB4WkZsS05IVXdOMEU1VFZaM1VtSTVMM2R0U0VOVlVURnlha0pHTlhWSWQyVXpibEJMZEZZd0sybENOVTVXVjBsdmRFcG9NblJvZW5aNlR6SXdTVmxqQ21oa1dFUlFkM3BVVlZSaGNtRnBaVlJWUVhGMlZWZ3lXa28zTUVWTE1tRlZkbU5TVm1wWmNIRXhWamx1YlVaTGNURlVUM3B6WWpGU1JtUkNZV2hvWjFZS2QwSmlZV3ByTkdkc1dUQldWRE00Tm0weVdFRjVTRkpRTjFKcVltMUVWbXNyTVVOakwxSnhkVko0YkZWeFlscFNOMDF2ZURobVFXUndZbXRrTTFGUFFRcFNWMjlYYkZGcVRETlVWMEZPVmpreWJUUlhUelZxTnpGTGJuTmpOV1JCVVdGRlNtSkVTMGhEZUZoTkt6VlhkeklyVWk5bU1TOWpRVkoyWTJ0QmRUUmFDbEJvVmpsMVIwRjRkUzlqWkVRclF6ZG1RV2hrUm5oeFdsVnJPVWxWZEhrNWVVZ3ZWbko0VEROSlVtb3lUV3RRTlRSdFVVOUdXa1JrT1ZsUVNrNVZWVzhLUVVGWVkyeFVlWGRtTjFObE5rMTRXVFI1Vm01eFMwSTVRbXRpU1hGMFRtNVlVREJZUlVGUVJIQXdjbFZTTVdsVlUxZE9ZM1kyVlVaMk1rWTFVMlp1ZEFwd2NVZHJiMUJsUzNsUlYyaENaRVZrU3k4MVlXbGlWRTVhT0hselVtcHJhMFJTVjB3MVUzbHZkMFZIUVhsc1kyMUlMMjlQVUdsSVFYQkdUMVJEUWpaV0NpdENSbWhHUldocFlubGFNRzlQWWtGVGVqSmtRbE5IYVU1eVJrMWFTRVJrVFVSTlIwTlRjVWRUU1dJelJGRkZTRUZVUVZWQ1oyZHhhR3RwUnpsM01FUUtRbmRSU1hwM1prSkRURGt6UjJkRFFVVklaMDl4UjB0NU1EbDRUM1ZuU1RobFpWRnZiRXROUFFvdExTMHRMVVZPUkNCUVMwTlROeTB0TFMwdENnPT0iCiAgICAgICAgICB9LAogICAgICAgICAgInR3byI6IHsKICAgICAgICAgICAgInZhbHVlIjogIkxTMHRMUzFDUlVkSlRpQlFTME5UTnkwdExTMHRDazFKU1VOcFVWbEtTMjlhU1doMlkwNUJVV05FYjBsSlEyVnFRME5CYmxsRFFWRkJlR2RuU1RaTlNVbERUbWRKUWtGRVFXVk5Ra1Y0UkhwQlRrSm5UbFlLUWtGTlZVSnNRa3hSTVUxcVRuZEpTa0ZOV25SWlJtMXRVbE5CY2sxQk1FZERVM0ZIVTBsaU0wUlJSVUpCVVZWQlFrbEpRMEZNVURWc1puVkVRMUF2UVFvMVlqQlJUbUUwUVdzMU5FcEdNSGRNYjJFNE1uTm5Ra3haV0hoQmRrRk9MMnhMV1V4UE1uSXpkVUZWWm01Sk9IUlBXbmQxWjJKbEswRnZPWFkzUTJKeUNtcGljMUpYVDJkWGRFVlViWFJqYjJ0emRWSm9abFZrYUd0WlNtcHBjRlpsTW1saFNHdHpRVmM0VDJKQ1pYY3hZM2hqVmpWbEwzQXdTVUpQU2pWck9WVUtNMWhMTW5keU5qSlBNVGhxT0hsMFVYZ3pkVVl2UVZkbmRXNVlhR2swV0RaM2FXUkVWellyVUZSS1kxZG9UeTgxZGxSM2J5OXVkakJ0UW05SVEyUlhXQXB4WkZsS05IVXdOMEU1VFZaM1VtSTVMM2R0U0VOVlVURnlha0pHTlhWSWQyVXpibEJMZEZZd0sybENOVTVXVjBsdmRFcG9NblJvZW5aNlR6SXdTVmxqQ21oa1dFUlFkM3BVVlZSaGNtRnBaVlJWUVhGMlZWZ3lXa28zTUVWTE1tRlZkbU5TVm1wWmNIRXhWamx1YlVaTGNURlVUM3B6WWpGU1JtUkNZV2hvWjFZS2QwSmlZV3ByTkdkc1dUQldWRE00Tm0weVdFRjVTRkpRTjFKcVltMUVWbXNyTVVOakwxSnhkVko0YkZWeFlscFNOMDF2ZURobVFXUndZbXRrTTFGUFFRcFNWMjlYYkZGcVRETlVWMEZPVmpreWJUUlhUelZxTnpGTGJuTmpOV1JCVVdGRlNtSkVTMGhEZUZoTkt6VlhkeklyVWk5bU1TOWpRVkoyWTJ0QmRUUmFDbEJvVmpsMVIwRjRkUzlqWkVRclF6ZG1RV2hrUm5oeFdsVnJPVWxWZEhrNWVVZ3ZWbko0VEROSlVtb3lUV3RRTlRSdFVVOUdXa1JrT1ZsUVNrNVZWVzhLUVVGWVkyeFVlWGRtTjFObE5rMTRXVFI1Vm01eFMwSTVRbXRpU1hGMFRtNVlVREJZUlVGUVJIQXdjbFZTTVdsVlUxZE9ZM1kyVlVaMk1rWTFVMlp1ZEFwd2NVZHJiMUJsUzNsUlYyaENaRVZrU3k4MVlXbGlWRTVhT0hselVtcHJhMFJTVjB3MVUzbHZkMFZIUVhsc1kyMUlMMjlQVUdsSVFYQkdUMVJEUWpaV0NpdENSbWhHUldocFlubGFNRzlQWWtGVGVqSmtRbE5IYVU1eVJrMWFTRVJrVFVSTlIwTlRjVWRUU1dJelJGRkZTRUZVUVZWQ1oyZHhhR3RwUnpsM01FUUtRbmRSU1hwM1prSkRURGt6UjJkRFFVVklaMDl4UjB0NU1EbDRUM1ZuU1RobFpWRnZiRXROUFFvdExTMHRMVVZPUkNCUVMwTlROeTB0TFMwdENnPT0iCiAgICAgICAgICB9CiAgICAgICAgfQogICAgICB9"
        },
        {
          "CreateIndex": 10298502,
          "Flags": 0,
          "Key": "services/secrets/some/test/service",
          "LockIndex": 0,
          "ModifyIndex": 10298563,
          "Value":"ewogICAgICAgICJzZWNyZXRzIjogewogICAgICAgICAgInRocmVlIjogewogICAgICAgICAgICAidmFsdWUiOiAiTFMwdExTMUNSVWRKVGlCUVMwTlROeTB0TFMwdENrMUpTVU5wVVZsS1MyOWFTV2gyWTA1QlVXTkViMGxKUTJWcVEwTkJibGxEUVZGQmVHZG5TVFpOU1VsRFRtZEpRa0ZFUVdWTlFrVjRSSHBCVGtKblRsWUtRa0ZOVlVKc1FreFJNVTFxVG5kSlNrRk5XblJaUm0xdFVsTkJjazFCTUVkRFUzRkhVMGxpTTBSUlJVSkJVVlZCUWtsSlEwRk1VRFZzWm5WRVExQXZRUW8xWWpCUlRtRTBRV3MxTkVwR01IZE1iMkU0TW5OblFreFpXSGhCZGtGT0wyeExXVXhQTW5JemRVRlZabTVKT0hSUFduZDFaMkpsSzBGdk9YWTNRMkp5Q21waWMxSlhUMmRYZEVWVWJYUmpiMnR6ZFZKb1psVmthR3RaU21wcGNGWmxNbWxoU0d0elFWYzRUMkpDWlhjeFkzaGpWalZsTDNBd1NVSlBTalZyT1ZVS00xaExNbmR5TmpKUE1UaHFPSGwwVVhnemRVWXZRVmRuZFc1WWFHazBXRFozYVdSRVZ6WXJVRlJLWTFkb1R5ODFkbFIzYnk5dWRqQnRRbTlJUTJSWFdBcHhaRmxLTkhVd04wRTVUVlozVW1JNUwzZHRTRU5WVVRGeWFrSkdOWFZJZDJVemJsQkxkRll3SzJsQ05VNVdWMGx2ZEVwb01uUm9lblo2VHpJd1NWbGpDbWhrV0VSUWQzcFVWVlJoY21GcFpWUlZRWEYyVlZneVdrbzNNRVZMTW1GVmRtTlNWbXBaY0hFeFZqbHViVVpMY1RGVVQzcHpZakZTUm1SQ1lXaG9aMVlLZDBKaVlXcHJOR2RzV1RCV1ZETTRObTB5V0VGNVNGSlFOMUpxWW0xRVZtc3JNVU5qTDFKeGRWSjRiRlZ4WWxwU04wMXZlRGhtUVdSd1ltdGtNMUZQUVFwU1YyOVhiRkZxVEROVVYwRk9Wamt5YlRSWFR6VnFOekZMYm5Oak5XUkJVV0ZGU21KRVMwaERlRmhOS3pWWGR6SXJVaTltTVM5alFWSjJZMnRCZFRSYUNsQm9WamwxUjBGNGRTOWpaRVFyUXpkbVFXaGtSbmh4V2xWck9VbFZkSGs1ZVVndlZuSjRURE5KVW1veVRXdFFOVFJ0VVU5R1drUmtPVmxRU2s1VlZXOEtRVUZZWTJ4VWVYZG1OMU5sTmsxNFdUUjVWbTV4UzBJNVFtdGlTWEYwVG01WVVEQllSVUZRUkhBd2NsVlNNV2xWVTFkT1kzWTJWVVoyTWtZMVUyWnVkQXB3Y1VkcmIxQmxTM2xSVjJoQ1pFVmtTeTgxWVdsaVZFNWFPSGx6VW1wcmEwUlNWMHcxVTNsdmQwVkhRWGxzWTIxSUwyOVBVR2xJUVhCR1QxUkRRalpXQ2l0Q1JtaEdSV2hwWW5sYU1HOVBZa0ZUZWpKa1FsTkhhVTV5UmsxYVNFUmtUVVJOUjBOVGNVZFRTV0l6UkZGRlNFRlVRVlZDWjJkeGFHdHBSemwzTUVRS1FuZFJTWHAzWmtKRFREa3pSMmREUVVWSVowOXhSMHQ1TURsNFQzVm5TVGhsWlZGdmJFdE5QUW90TFMwdExVVk9SQ0JRUzBOVE55MHRMUzB0Q2c9PSIKICAgICAgICAgIH0KICAgICAgICB9CiAgICAgIH0="
        }
      ]
    """, 200, Map.empty))

    val builder = TaskInfo.newBuilder()

    plugin.taskInfo(runSpec, builder)

    builder.getCommand.getEnvironment.getVariablesList.toList.map(v ⇒ v.getName → v.getValue).toMap mustBe Map(
      "SECRET_ONE" → "kulikov-test",
      "SECRET_TWO" → "kulikov-test",
      "SECRET_THREE" → "kulikov-test"
    )
  }
}
