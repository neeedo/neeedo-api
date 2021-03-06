//package common.elasticsearch
//
//import common.helper.ConfigLoader
//import org.specs2.mutable.Specification
//import play.api.Play
//import test.TestApplications
//
//class RemoteEsClientSpec extends Specification {
//
//  "RemoteEsClient" should {
//    "read valid hosts from config" in TestApplications.configOffApp(Map("elasticsearch.hosts" -> "localhost1:9200,localhost2:9200, localhost3:9200")) {
//      val configLoader = new ConfigLoader
//      val client = new RemoteEsClient(configLoader)
//      val hosts = client.readHostsFromConfig
//
//      hosts must beEqualTo(List(HostWithPort("localhost1", 9200), HostWithPort("localhost2", 9200), HostWithPort("localhost3", 9200)))
//    }
//
//    "readHostsFromConfig must trim whitespaces" in TestApplications.configOffApp(Map("elasticsearch.hosts" -> "localhost1  :9200   ")) {
//      val configLoader = new ConfigLoader
//      val client = new RemoteEsClient(configLoader)
//      val hosts = client.readHostsFromConfig
//
//      hosts must beEqualTo(List(HostWithPort("localhost1", 9200)))
//    }
//
//    "readHostsFromConfig must parse hosts with more than one colon correctly" in TestApplications.configOffApp(Map("elasticsearch.hosts" -> "lo:cal:ho:st1:9200")) {
//      val configLoader = new ConfigLoader
//      val client = new RemoteEsClient(configLoader)
//      val hosts = client.readHostsFromConfig
//
//      hosts must beEqualTo(List(HostWithPort("lo:cal:ho:st1", 9200)))
//    }
//
//    "HostWithPort toString should return 'host:port' format" in {
//      List(HostWithPort("bla", 9200)).mkString must beEqualTo("bla:9200")
//    }
//  }
//
//}
