package controllers

import javax.inject.Inject

import models.{BoardReplyAccess, BulletinBoardAccess}
import play.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by leehwangchun on 2017. 5. 22..
  */

//case class WriteForm(idx : Long, title : String, writer : String, contents : String)

class WriteController @Inject()(bc : BulletinBoardAccess, rc : BoardReplyAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends WriteModifyCommon with I18nSupport {

  def write = Action { implicit request =>
    request.session.get("userName")
      .map(userName => Ok(views.html.write(userName, writeForm)).withSession(request.session))
      .getOrElse(Ok(views.html.write("NoName", writeForm)))
  }

  def insertToDB(request: Request[WriteForm]): Future[Result] = {
    val boardData = request.body
    val writerUID = request.session.get("uid").map(_.toLong).getOrElse(-1 : Long)
    bc.insertBoardList(boardData.title, boardData.writer, boardData.contents, writerUID).map {
      case Right(a) => Redirect(routes.BoardController.board())
      case Left(t) => Ok(t.toString + "Failure!!")
    }
  }

  def commit = Action.async(parse.form(writeForm, onErrors = formParseError))(insertToDB)
}
