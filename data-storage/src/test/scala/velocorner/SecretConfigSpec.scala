package velocorner

import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification


class SecretConfigSpec extends Specification {

  "config" should {

    "read proxy settings" in {

      val testConfig =
        """
          |proxy.host=localhost
          |proxy.port=9999
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.getProxyHost must beSome("localhost")
      conf.getProxyPort must beSome(9999)
    }

    "ignore proxy settings" in {
      val testConfig =
        """
          |key=value
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.getProxyHost must beNone
      conf.getProxyPort must beNone
    }

  }
}