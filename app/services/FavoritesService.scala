package services

import common.domain.UserId
import model.{CardId, Card}

import scala.concurrent.Future

class FavoritesService {

  def getFavoritesByUser(id: UserId): Future[List[CardId]] = ???

  def addFavorite(id: CardId): Future[Option[CardId]] = ???

  def removeFavorite(id: CardId): Future[Option[CardId]] = ???
}
