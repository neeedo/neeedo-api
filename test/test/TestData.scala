package test

import common.domain._
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.DefaultCurrencyUnits
import io.sphere.sdk.utils.MoneyImpl
import model._
import play.api.libs.json.{Json, JsObject}
import scala.collection.JavaConverters._

object TestData {

  val basicAuthToken = "Basic dGVzdDp0ZXN0"

  // Model
  val demandId = DemandId("testDemandId")
  val offerId = OfferId("testOfferId")
  val version = Version(1L)
  val userId = UserId("123")
  val username = Username("test")
  val tags = Set("TV Regal", "Oppli", "Birkenoptik")
  val tagsWithWhitespaces = Set("  TV Regal", "Oppli   ", " Birkenoptik")
  val shouldTags = Set("Buche", "Kiefer", "Schwarz")
  val shouldTagsWithWhitespaces = Set(" Buche", "Kiefer  ", "  Schwarz")
  val location = Location( Longitude(52.5075419), Latitude(13.4251364) )
  val distance = Distance(10000)
  val price = Price(50)
  val priceMin = Price(0)
  val priceMax = Price(500)
  val images = Set.empty[String]

  val demand = Demand(
    demandId,
    version,
    UserIdAndName(
      userId,
      username
    ),
    tags,
    shouldTags,
    location,
    distance,
    priceMin,
    priceMax)

  val demandJson: JsObject = Json.obj(
    "id" -> demandId.value,
    "version" -> version.value,
    "user" -> Json.obj(
      "id" -> userId.value,
      "name" -> username.value
    ),
    "mustTags" -> tags,
    "shouldTags" -> shouldTags,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "distance" -> distance.value,
    "price" -> Json.obj(
      "min" -> priceMin.value,
      "max" -> priceMax.value
    )
  )

  val demandJsonWithCompletionTags: JsObject = Json.obj(
    "id" -> demandId.value,
    "version" -> version.value,
    "user" -> Json.obj(
      "id" -> userId.value,
      "name" -> username.value
    ),
    "mustTags" -> tags,
    "shouldTags" -> shouldTags,
    "completionTags" -> (tags ++ shouldTags),
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "distance" -> distance.value,
    "price" -> Json.obj(
      "min" -> priceMin.value,
      "max" -> priceMax.value
    )
  )

  val demandJsonWithWhitespaces: JsObject = Json.obj(
    "id" -> demandId.value,
    "version" -> version.value,
    "user" -> Json.obj(
      "id" -> userId.value,
      "name" -> username.value
    ),
    "mustTags" -> tagsWithWhitespaces,
    "shouldTags" -> shouldTagsWithWhitespaces,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "distance" -> distance.value,
    "price" -> Json.obj(
      "min" -> priceMin.value,
      "max" -> priceMax.value
    )
  )

  val demandDraft = DemandDraft(
    userId,
    tags,
    shouldTags,
    location,
    distance,
    priceMin,
    priceMax)

  val demandDraftJson: JsObject = Json.obj(
    "userId" -> userId.value,
    "mustTags" -> tags,
    "shouldTags" -> shouldTags,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "distance" -> distance.value,
    "price" -> Json.obj(
      "min" -> priceMin.value,
      "max" -> priceMax.value
    )
  )

  val demandDraftJsonWithWhitespaces: JsObject = Json.obj(
    "userId" -> userId.value,
    "mustTags" -> tagsWithWhitespaces,
    "shouldTags" -> shouldTagsWithWhitespaces,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "distance" -> distance.value,
    "price" -> Json.obj(
      "min" -> priceMin.value,
      "max" -> priceMax.value
    )
  )

  val demandProductAttributeList = List(
    Attribute.of("userId", userId.value),
    Attribute.of("userName", username.value),
    Attribute.of("mustTags", tags.asJava),
    Attribute.of("shouldTags", shouldTags.asJava),
    Attribute.of("longitude", location.lon.value),
    Attribute.of("latitude", location.lat.value),
    Attribute.of("distance", distance.value),
    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(priceMin.value).bigDecimal, DefaultCurrencyUnits.EUR)),
    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(priceMax.value).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  val offer = Offer(
    offerId,
    version,
    UserIdAndName(
      userId,
      username
    ),
    tags,
    location,
    price,
    images)

  val offerJson: JsObject = Json.obj(
    "id" -> offerId.value,
    "version" -> version.value,
    "user" -> Json.obj(
      "id" -> userId.value,
      "name" -> username.value
    ),
    "tags" -> tags,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "price" -> price.value,
    "images" -> images
  )

  val offerJsonWithCompletionTags: JsObject = Json.obj(
    "id" -> offerId.value,
    "version" -> version.value,
    "user" -> Json.obj(
      "id" -> userId.value,
      "name" -> username.value
    ),
    "tags" -> tags,
    "completionTags" -> tags,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "price" -> price.value,
    "images" -> images
  )

  val offerJsonWithWhitespaces: JsObject = Json.obj(
    "id" -> offerId.value,
    "version" -> version.value,
    "user" -> Json.obj(
      "id" -> userId.value,
      "name" -> username.value
    ),
    "tags" -> tagsWithWhitespaces,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "price" -> price.value,
    "images" -> images
  )

  val offerDraft = OfferDraft(
    userId,
    tags.toList,
    location,
    price,
    images.toList)

  val offerDraftJson: JsObject = Json.obj(
    "userId" -> userId.value,
    "tags" -> tags,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "price" -> price.value,
    "images" -> images
  )

  val offerDraftJsonWithWhitespaces: JsObject = Json.obj(
    "userId" -> userId.value,
    "tags" -> tagsWithWhitespaces,
    "location" -> Json.obj(
      "lat" -> location.lat.value,
      "lon" -> location.lon.value
    ),
    "price" -> price.value,
    "images" -> images
  )

  val offerProductAttributeList = List(
    Attribute.of("userId", userId.value),
    Attribute.of("userName", userId.value),
    Attribute.of("tags", tags.asJava),
    Attribute.of("longitude", location.lon.value),
    Attribute.of("latitude", location.lat.value),
    Attribute.of("price", MoneyImpl.of(BigDecimal(price.value).bigDecimal, DefaultCurrencyUnits.EUR)),
    Attribute.of("images", images.asJava)
  ).asJava

}
