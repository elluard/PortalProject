package controllers

import javax.inject.{Inject, Singleton}

import models._
import play.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by leehwangchun on 2017. 4. 8..
  */

@Singleton
class BoardController @Inject()(bc : BulletinBoardAccess, rc : BoardReplyAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  def board(page : Int) = Action.async { implicit request =>
    bc.getBoardTitleList(0, page).map {
      case Right(a) => Ok(views.html.board(a))
      case Left(a) => BadRequest(a.toString)
    }
  }

  def boardContents(id: Long) = Action.async { implicit request =>
    //현재는 scala 함수 안에서 같이 읽어들이도록 했지만
    //차후에는 iframe 을 사용해서 읽어들일 수 있도록 수정 필요하다.
    bc.getBoardContents(id).map {
      case Right(a) => {
        val canModify = request.session.get("uid").exists(_.toLong == a.writerUID)
        Ok(views.html.boardContents(a,canModify))
      }
      case Left(t) => BadRequest(t)
    }
  }
}
