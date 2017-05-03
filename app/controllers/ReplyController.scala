package controllers

import javax.inject.{Inject, Singleton}

import models.{BoardReplyAccess, BulletinBoardAccess}
import play.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class ReplyForm(boardContentID : Long, content : String)

/**
  * Created by leehwangchun on 2017. 5. 3..
  */
@Singleton
class ReplyController @Inject()(rc : BoardReplyAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends Controller with I18nSupport  {

  val replyForm = Form(
    mapping(
      "boardContentID" -> longNumber,
      "content" -> nonEmptyText
    )(ReplyForm.apply)(ReplyForm.unapply)
  )

  def replyFormParseError(formWithErrors: Form[ReplyForm]): Result = {
    BadRequest("form parse error" + formWithErrors.toString)
  }

  def replies(id : Long) = Action.async {
    rc.getReplies(id).map { replyList =>
      Ok(views.html.replies(replyList, replyForm, id))
    }
  }

  def replyCommit = Action.async(parse.form(replyForm, onErrors = replyFormParseError)) { implicit request =>
    val replyData = request.body
    val userName = request.session.get("userName").getOrElse("NoName")
    val writerUID = request.session.get("uid").map(_.toLong).getOrElse(-1 : Long)
    rc.insertReply(replyData.boardContentID, writerUID, userName, replyData.content).map {
      case Success(a) => Redirect(routes.ReplyController.replies(replyData.boardContentID))
      case Failure(t) => Ok(t.toString + "Failure!!")
    }
  }
}
