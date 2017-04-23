package controllers

import javax.inject.{Inject, Singleton}

import models.{AccountDataAccess, BulletinBoardAccess}
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

case class WriteForm(title : String, writer : String, contents : String)

@Singleton
class BoardController @Inject()(bc : BulletinBoardAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  val writeForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "writer" -> nonEmptyText,
      "contents" -> nonEmptyText
    )(WriteForm.apply)(WriteForm.unapply)
  )

  def board(page : Int) = Action.async { implicit request =>
    bc.getBoardTitleList(0, page).map {
      case Success(a) => Ok(views.html.board(a))
      case Failure(a) => BadRequest(a.toString)
    }
  }

  def boardContents(id: Long) = Action.async { implicit request =>
    bc.getBoardContents(id).map {
      case Some(a) => Ok(views.html.boardContents(a))
      case None => BadRequest("No Contents")
    }
  }

  def write = Action { implicit request =>
    request.session.get("userName")
      .map(userName => Ok(views.html.write(userName, writeForm)).withSession(request.session))
      .getOrElse(Ok(views.html.write("NoName", writeForm)))
  }

  def formParseError(formWithErrors: Form[WriteForm]): Result = {
    BadRequest("form parse error")
  }

  def insertToDB(request: Request[WriteForm]): Future[Result] = {
    val boardData = request.body
    val writerUID = request.session.get("uid").map(_.toInt).getOrElse(-1)
    bc.insertBoardList(boardData.title, boardData.writer, boardData.contents, writerUID).map {
      case Success(a) => Redirect(routes.BoardController.board())
      case Failure(t) => Ok(t.toString + "Failure!!")
    }
  }

  def commit = Action.async(parse.form(writeForm, onErrors = formParseError))(insertToDB)
}
