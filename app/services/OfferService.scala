package services

import java.util.Optional

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypes, SphereClient}
import io.sphere.sdk.products.Product
import model.{OfferId, Offer}
import org.elasticsearch.index.query.QueryBuilders
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

class OfferService(elasticsearch: ElasticsearchClient, sphereClient: SphereClient, productTypes: ProductTypes) {
  val offerIndex = IndexName("offers")
  val offerType = TypeName("offers")

  def getOffers: Future[JsValue] = getOffersFromEs.map {
    hits => Json.obj("offers" -> hits.toSeq.map {
      hit => Json.parse(hit.sourceAsString())
    })
  }

  def getOffersFromEs = {
    elasticsearch.search(offerIndex, offerType, QueryBuilders.matchAllQuery()).map(result => result.getHits.getHits)
  }

  def addOffer(offer: Offer): Future[AddOfferResult] = {
    for {
      es <- writeOfferToEs(offer)
      //TODO write data to sphere IO
    } yield es
  }

  def writeOfferToEs(offer: Offer): Future[AddOfferResult] = {
    elasticsearch.indexDocument(offerIndex, offerType, Json.toJson(offer)).map(response =>
      OfferSaved
    ).recover {
      case _ => OfferSaveFailed
    }
  }

  // Todo implementation
  def createOffer(offerDraft: OfferDraft): Future[Option[Offer]] = {
    Future.successful(None)
  }

  // Todo implementation
  def getProductById(id: OfferId): Future[Optional[Product]] = {
    Future.successful(Optional.empty())
  }

  // Todo implementation
  def productToOffer(product: Product): Offer = {
    Offer(OfferId("1"), Version(1L), UserId("1"), "bla", Location(Longitude(1), Latitude(2)), Price(1))
  }

}
