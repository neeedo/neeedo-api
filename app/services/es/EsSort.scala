package services.es

import common.domain.Location
import common.helper.TimeHelper
import org.elasticsearch.common.geo.GeoPoint
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders

abstract class EsSort(timeHelper: TimeHelper) {
  def buildFunctionScoredQuery(location: Option[Location]) = {
    val baseQuery = QueryBuilders.functionScoreQuery(
      ScoreFunctionBuilders.gaussDecayFunction("createdAt", timeHelper.now, "8h")
        .setDecay(0.75)
        .setOffset("4h"))

    location match {
      case Some(loc) =>
        baseQuery.add(ScoreFunctionBuilders.gaussDecayFunction("location", new GeoPoint(loc.lat.value, loc.lon.value), "10km")
          .setDecay(0.9)
          .setOffset("30km"))
      case None => baseQuery
    }
  }
}
