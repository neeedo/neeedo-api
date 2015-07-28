package services.es

import common.domain._
import common.elasticsearch.{EsSettings, EsMapping, TestEsClient, ElasticsearchClient}
import common.helper.{TimeHelper, ConfigLoader}
import model.{OfferId, Offer, DemandId, Demand}
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.joda.time.{DateTimeZone, DateTime}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.WithApplication

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

class EsMatchingServiceSpec extends Specification with Mockito {
  trait EsMatchingServiceContext extends WithApplication {
    val esMock = mock[ElasticsearchClient]
    val config = Map("demand.typeName" -> "demand",
      "offer.typeName" -> "offer",
      "completionsIndexName" -> "completion")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val timeStamp = 1434272348084L
    val timeHelperMock = mock[TimeHelper]
    timeHelperMock.now returns new DateTime(DateTimeZone.forID("Europe/Berlin")).withMillis(timeStamp)
    val service = new EsMatchingService(esMock, configLoader, timeHelperMock)
    val demand = Demand(
      DemandId("123-demand"),
      Version(1L),
      UserIdAndName(UserId("123-user"), Username("Blub")),
      Set("Fahrrad"),
      Set("Merida"),
      Location(Longitude(13.37722), Latitude(52.50838)),
      Distance(150),
      Price(100),
      Price(800))

    val fittingOffer1 = Offer(
      OfferId("123-offer"),
      Version(1L),
      UserIdAndName(UserId("1234-user"), Username("Blubbablase")),
      Set("Fahrrad", "Merida"),
      Location(Longitude(13.37222), Latitude(52.5438)),
      Price(600),
      Set()
    )

    val fittingOffer2 = Offer(
      OfferId("12345-offer"),
      Version(1L),
      UserIdAndName(UserId("meepmeep"), Username("roadrunner")),
      Set("Fahrrad", "Giant"),
      Location(Longitude(13.37222), Latitude(52.5438)),
      Price(600),
      Set()
    )

    val nonFittingOffer1 = Offer(
      OfferId("1234-offer"),
      Version(1L),
      UserIdAndName(UserId("123-user"), Username("Blub")),
      Set("Fahrrad", "Merida"),
      Location(Longitude(13.37222), Latitude(52.5438)),
      Price(50),
      Set()
    )

    val nonFittingOffer2 = Offer(
      OfferId("1235-offer"),
      Version(1L),
      UserIdAndName(UserId("123-user"), Username("Blub")),
      Set("Kinderwagen", "Schwarz"),
      Location(Longitude(13.37222), Latitude(52.5438)),
      Price(650),
      Set()
    )

    val nonFittingOffer3 = Offer(
      OfferId("1236-offer"),
      Version(1L),
      UserIdAndName(UserId("123-user"), Username("Blub")),
      Set("Fahrrad", "Merida"),
      Location(Longitude(-0.14453), Latitude(51.50671)),
      Price(650),
      Set()
    )

    val nonFittingOffer4 = Offer(
      OfferId("1237-offer"),
      Version(1L),
      UserIdAndName(UserId("123-user"), Username("Blub")),
      Set("Fahrrad", "Merida"),
      Location(Longitude(13.37222), Latitude(52.5438)),
      Price(600),
      Set()
    )
  }

  trait EsMatchingServiceIntegrationContext extends WithApplication with EsMatchingServiceContext {
    val esClient = new TestEsClient()
    val integrationService = new EsMatchingService(esClient, configLoader, new TimeHelper)
    val esCompletionService = mock[EsCompletionService]
    esCompletionService.upsertCompletions(any[List[CompletionTag]]) returns Future(List())
    val demandService = new EsDemandService(esClient, configLoader, esCompletionService, timeHelperMock)
    val offerService = new EsOfferService(esClient, configLoader, esCompletionService, timeHelperMock)
  }

  "EsMatchingService" should {

    "matchDemand must find fitting offers from all other users except me" in new EsMatchingServiceIntegrationContext {
      Await.result(esClient.createIndex(configLoader.demandIndex, esClient.buildIndexRequest(
        configLoader.demandIndex, EsMapping(configLoader.demandIndex.toTypeName, "migrations/demand-mapping.json"),
        EsSettings("migrations/offer-demand-settings.json"))), Duration.Inf) must be equalTo true
      Await.result(esClient.createIndex(configLoader.offerIndex, esClient.buildIndexRequest(
        configLoader.offerIndex, EsMapping(configLoader.offerIndex.toTypeName, "migrations/offer-mapping.json"),
        EsSettings("migrations/offer-demand-settings.json"))), Duration.Inf) must be equalTo true

      Await.result(demandService.createDemand(demand), Duration.Inf) must be equalTo demand
      Await.result(integrationService.matchDemand(Pager(10, 0), demand), Duration.Inf) must be equalTo List()
      Await.result(offerService.createOffer(fittingOffer1), Duration.Inf) must be equalTo fittingOffer1
      Await.result(offerService.createOffer(fittingOffer2), Duration.Inf) must be equalTo fittingOffer2
      Await.result(offerService.createOffer(nonFittingOffer1), Duration.Inf) must be equalTo nonFittingOffer1
      Await.result(offerService.createOffer(nonFittingOffer2), Duration.Inf) must be equalTo nonFittingOffer2
      Await.result(offerService.createOffer(nonFittingOffer3), Duration.Inf) must be equalTo nonFittingOffer3
      Await.result(offerService.createOffer(nonFittingOffer4), Duration.Inf) must be equalTo nonFittingOffer4
      esClient.client.admin().indices()
        .refresh(new RefreshRequest(configLoader.demandIndex.value, configLoader.offerIndex.value)).actionGet()
      Await.result(integrationService.matchDemand(Pager(10, 0), demand), Duration.Inf) must be equalTo List(fittingOffer1, fittingOffer2)
    }

    "buildMatchDemandQuery must return correct matchingQuery" in new EsMatchingServiceContext {
      Json.parse(service.buildMatchDemandQuery(demand).toString) must be equalTo
        Json.obj(
          "filtered" -> Json.obj(
            "query" -> Json.obj(
              "function_score" -> Json.obj(
                "query" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" -> Json.obj(
                      "match_all" -> Json.obj()
                    ),
                    "should" -> Json.obj(
                      "match" -> Json.obj(
                        "tags" -> Json.obj(
                          "query" -> "Merida",
                          "type" -> "boolean"
                        )
                      )
                    )
                  )
                ),
                "functions" -> Json.arr(
                  Json.obj(
                    "gauss" -> Json.obj(
                      "createdAt" -> Json.obj(
                        "origin" -> "2015-06-14T10:59:08.084+02:00",
                        "scale" -> "8h",
                        "decay" -> 0.75,
                        "offset" -> "4h"
                      )
                    )
                  ),
                  Json.obj(
                    "gauss" -> Json.obj(
                      "location" -> Json.obj(
                        "origin" -> Json.obj(
                          "lat" -> 52.50838,
                          "lon" -> 13.37722
                        ),
                        "scale" -> "37.5km",
                        "decay" -> 0.75,
                        "offset" -> "15.0km"
                      )
                    )
                  )
                ),
                "score_mode" -> "avg"
              )
            ),
            "filter" -> Json.obj(
              "and" -> Json.obj(
                "filters" -> Json.arr(
                  Json.obj(
                    "geo_distance" -> Json.obj(
                      "location" -> Json.arr(13.37722,52.50838),
                      "distance" -> "150.0km"
                    )
                  ),
                  Json.obj(
                    "query" -> Json.obj(
                      "bool" -> Json.obj(
                        "must" -> Json.obj("match" -> Json.obj(
                            "tags" -> Json.obj(
                              "query" -> "Fahrrad",
                              "type" -> "boolean",
                              "operator" -> "AND"
                            )
                          )
                        ),
                        "must_not" -> Json.obj(
                          "term" -> Json.obj("user.id" -> "123-user")
                        )
                      )
                    )
                  ),
                  Json.obj(
                    "range" -> Json.obj(
                      "price" -> Json.obj(
                        "from" -> 100.0,
                        "to" -> 800.0,
                        "include_lower" -> true,
                        "include_upper" -> true
                      )
                    )
                  )
                )
              )
            )
          )
        )
    }
  }
}
