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

case class WriteForm(idx : Long, title : String, writer : String, contents : String)
case class ReplyForm(boardContentID : Long, content : String)

@Singleton
class BoardController @Inject()(bc : BulletinBoardAccess, rc : BoardReplyAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  val writeForm = Form(
    mapping(
      "idx" -> longNumber,
      "title" -> nonEmptyText,
      "writer" -> nonEmptyText,
      "contents" -> nonEmptyText
    )(WriteForm.apply)(WriteForm.unapply)
  )

  val replyForm = Form(
    mapping(
      "boardContentID" -> longNumber,
      "content" -> nonEmptyText
    )(ReplyForm.apply)(ReplyForm.unapply)
  )

  def board(page : Int) = Action.async { implicit request =>
    bc.getBoardTitleList(0, page).map {
      case Success(a) => Ok(views.html.board(a))
      case Failure(a) => BadRequest(a.toString)
    }
  }

  def boardContents(id: Long) = Action.async { implicit request =>
    //현재는 scala 함수 안에서 같이 읽어들이도록 했지만
    //차후에는 iframe 을 사용해서 읽어들일 수 있도록 수정 필요하다.
    bc.getBoardContents(id).flatMap {
      case Some(a) => {
        rc.getReplies(id).map{
          case Success(aa) => {
            val canModify = request.session.get("uid").exists(_.toLong == a.writerUID)
            Ok(views.html.boardContents(a, aa, canModify, replyForm))
          }
          case Failure(ff) => BadRequest("No Contents")
        }
      }
      case None => Future(BadRequest("No Contents"))
    }
  }

  def write = Action { implicit request =>
    request.session.get("userName")
      .map(userName => Ok(views.html.write(userName, writeForm)).withSession(request.session))
      .getOrElse(Ok(views.html.write("NoName", writeForm)))
  }

  def formParseError(formWithErrors: Form[WriteForm]): Result = {
    BadRequest("form parse error" + formWithErrors.toString)
  }

  def replyFormParseError(formWithErrors: Form[ReplyForm]): Result = {
    BadRequest("form parse error" + formWithErrors.toString)
  }

  def insertToDB(request: Request[WriteForm]): Future[Result] = {
    val boardData = request.body
    val writerUID = request.session.get("uid").map(_.toLong).getOrElse(-1 : Long)
    bc.insertBoardList(boardData.title, boardData.writer, boardData.contents, writerUID).map {
      case Success(a) => Redirect(routes.BoardController.board())
      case Failure(t) => Ok(t.toString + "Failure!!")
    }
  }

  def commit = Action.async(parse.form(writeForm, onErrors = formParseError))(insertToDB)

  def modify(id : Long) = Action.async { implicit request =>
    request.session.get("uid").map{ uid =>
      bc.getBoardContentsForModify(id, uid.toLong).map {
        case Some(a) => Ok(views.html.modify(a, writeForm))
        case None => BadRequest("No Contents")
      }
    }.getOrElse {
      Future(BadRequest("No Contents"))
    }
  }

  def modifyCommit = Action.async(parse.form(writeForm, onErrors = formParseError)) { implicit request =>
    val boardData = request.body
    bc.updateBoardContents(boardData.idx, boardData.contents).map {
      case Success(a) => Redirect(routes.BoardController.board())
      case Failure(t) => Ok(t.toString + "Failure!!")
    }
  }

  def replyCommit = Action.async(parse.form(replyForm, onErrors = replyFormParseError)) { implicit request =>
    val replyData = request.body
    val userName = request.session.get("userName").getOrElse("NoName")
    val writerUID = request.session.get("uid").map(_.toLong).getOrElse(-1 : Long)
    rc.insertReply(replyData.boardContentID, writerUID, userName, replyData.content).map {
      case Success(a) => Redirect(routes.BoardController.boardContents(replyData.boardContentID))
      case Failure(t) => Ok(t.toString + "Failure!!")
    }
  }
}
