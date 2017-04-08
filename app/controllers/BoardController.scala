package controllers

import javax.inject.{Inject, Singleton}

import models.{AccountDataAccess, BulletinBoardAccess}
import play.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by leehwangchun on 2017. 4. 8..
  */
@Singleton
class BoardController @Inject()(bc : BulletinBoardAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  def board = Action.async { implicit request =>
    bc.getBoardList(0).map {
      case Success(a) => Ok(views.html.board(a))
      case Failure(a) => BadRequest(a.toString)
    }
  }
}
